package com.anonymous.SolanaMobileApp.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MobileWalletAdapterService(private val context: Context) {

    companion object {
        private const val TAG = "MobileWalletAdapter"
        private const val RPC_ENDPOINT = "https://api.devnet.solana.com"
        private const val PHANTOM_PACKAGE = "app.phantom"
        private const val SOLFLARE_PACKAGE = "com.solflare.mobile"
        private const val BACKPACK_PACKAGE = "app.backpack.mobile"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Disconnected)
    val walletState: StateFlow<WalletState> = _walletState

    private var connectedWallet: String? = null
    private var publicKey: String? = null

    sealed class WalletState {
        object Disconnected : WalletState()
        object Connecting : WalletState()
        data class Connected(
            val publicKey: String,
            val balance: Double = 0.0,
            val walletName: String = "Unknown"
        ) : WalletState()
        data class Error(val message: String) : WalletState()
    }

    fun connectPhantom() {
        initiateWalletConnection("Phantom", PHANTOM_PACKAGE, "phantom://connect")
    }

    fun connectSolflare() {
        initiateWalletConnection("Solflare", SOLFLARE_PACKAGE, "solflare://connect")
    }

    fun connectBackpack() {
        initiateWalletConnection("Backpack", BACKPACK_PACKAGE, "backpack://connect")
    }

    private fun initiateWalletConnection(walletName: String, packageName: String, deepLink: String) {
        try {
            _walletState.value = WalletState.Connecting
            Log.d(TAG, "Attempting to connect to $walletName")

            // Check if wallet is installed
            if (!isWalletInstalled(packageName)) {
                promptWalletInstall(walletName, packageName)
                return
            }

            // Create connection deep link
            val connectionUri = Uri.parse(deepLink).buildUpon()
                .appendQueryParameter("dapp_name", "SwipeLaunch")
                .appendQueryParameter("dapp_url", "https://swipelaunch.fun")
                .appendQueryParameter("redirect_uri", "swipelaunch://wallet-connect")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, connectionUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage(packageName)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Log.d(TAG, "Launched $walletName connection")
            } else {
                _walletState.value = WalletState.Error("Failed to launch $walletName")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to $walletName", e)
            _walletState.value = WalletState.Error("Connection failed: ${e.message}")
        }
    }

    private fun isWalletInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun promptWalletInstall(walletName: String, packageName: String) {
        val playStoreUri = Uri.parse("market://details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, playStoreUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) == null) {
            // Fallback to web version of Play Store
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        }

        try {
            context.startActivity(intent)
            _walletState.value = WalletState.Error("$walletName not installed. Redirecting to Play Store...")
        } catch (e: Exception) {
            _walletState.value = WalletState.Error("Failed to open Play Store for $walletName")
        }
    }

    fun handleWalletResponse(intent: Intent?) {
        intent?.data?.let { uri ->
            Log.d(TAG, "Received wallet response: $uri")

            val publicKeyParam = uri.getQueryParameter("public_key") 
                ?: uri.getQueryParameter("publicKey")
                ?: uri.getQueryParameter("address")

            val walletName = uri.getQueryParameter("wallet_name") 
                ?: uri.getQueryParameter("source")
                ?: "Connected Wallet"

            if (publicKeyParam != null) {
                publicKey = publicKeyParam
                connectedWallet = walletName
                
                _walletState.value = WalletState.Connected(
                    publicKey = publicKeyParam,
                    walletName = walletName
                )

                // Fetch balance in background
                fetchBalance(publicKeyParam)
                
                Log.d(TAG, "Wallet connected successfully: $publicKeyParam")
            } else {
                Log.e(TAG, "No public key found in wallet response")
                _walletState.value = WalletState.Error("Invalid wallet response")
            }
        }
    }

    private fun fetchBalance(publicKey: String) {
        Thread {
            try {
                val requestBody = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("id", 1)
                    put("method", "getBalance")
                    put("params", org.json.JSONArray().put(publicKey))
                }.toString()

                val request = Request.Builder()
                    .url(RPC_ENDPOINT)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val json = JSONObject(responseBody ?: "{}")
                        
                        if (json.has("result")) {
                            val lamports = json.getJSONObject("result").getLong("value")
                            val sol = lamports / 1_000_000_000.0

                            _walletState.value = WalletState.Connected(
                                publicKey = publicKey,
                                balance = sol,
                                walletName = connectedWallet ?: "Connected Wallet"
                            )
                            
                            Log.d(TAG, "Balance updated: $sol SOL")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch balance", e)
                // Keep the connected state but with 0 balance
            }
        }.start()
    }

    suspend fun signAndSendTransaction(transactionData: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val currentWallet = connectedWallet ?: return@withContext null
            val currentPublicKey = publicKey ?: return@withContext null

            Log.d(TAG, "Attempting to sign transaction with $currentWallet")

            // Create transaction signing deep link based on wallet
            val signUri = when {
                currentWallet.contains("Phantom", ignoreCase = true) -> 
                    "phantom://sign_transaction"
                currentWallet.contains("Solflare", ignoreCase = true) -> 
                    "solflare://sign_transaction"
                currentWallet.contains("Backpack", ignoreCase = true) -> 
                    "backpack://sign_transaction"
                else -> "phantom://sign_transaction" // Default fallback
            }

            val transactionBase64 = android.util.Base64.encodeToString(
                transactionData, 
                android.util.Base64.NO_WRAP
            )

            val uri = Uri.parse(signUri).buildUpon()
                .appendQueryParameter("transaction", transactionBase64)
                .appendQueryParameter("redirect_uri", "swipelaunch://transaction-result")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Log.d(TAG, "Transaction sent to wallet for signing")
                return@withContext "pending" // Return pending status
            } else {
                Log.e(TAG, "Failed to launch wallet for transaction signing")
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign transaction", e)
            return@withContext null
        }
    }

    fun disconnect() {
        connectedWallet = null
        publicKey = null
        _walletState.value = WalletState.Disconnected
        Log.d(TAG, "Wallet disconnected")
    }

    fun isConnected(): Boolean {
        return _walletState.value is WalletState.Connected
    }

    fun getPublicKey(): String? = publicKey

    fun getConnectedWalletName(): String? = connectedWallet

    // SMS sharing functionality for wallet addresses and transactions
    fun shareWalletAddress() {
        val currentPublicKey = publicKey ?: return
        val walletName = connectedWallet ?: "My Wallet"
        
        val shareText = """
🎯 Check out my SwipeLaunch wallet!

💎 Wallet: $walletName
🔑 Address: $currentPublicKey
🌐 Network: Devnet

Join me on SwipeLaunch to discover the hottest Solana tokens!

Download: https://swipelaunch.fun/app
        """.trimIndent()
        
        ShareService(context).shareViaSMS(shareText)
    }

    fun shareTransactionSuccess(signature: String) {
        val shareText = """
🚀 Transaction Complete on SwipeLaunch!

✅ Status: Confirmed
📝 Signature: ${signature.take(8)}...${signature.takeLast(8)}
🌐 Network: Devnet

View on Solscan: https://solscan.io/tx/$signature?cluster=devnet

Join SwipeLaunch: https://swipelaunch.fun/app
        """.trimIndent()
        
        ShareService(context).shareViaSMS(shareText)
    }
}

// Extension for SMS sharing
class ShareService(private val context: Context) {
    fun shareViaSMS(message: String, phoneNumber: String? = null) {
        val smsUri = if (phoneNumber != null) {
            Uri.parse("smsto:$phoneNumber")
        } else {
            Uri.parse("smsto:")
        }
        
        val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        if (smsIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(smsIntent)
        }
    }
}