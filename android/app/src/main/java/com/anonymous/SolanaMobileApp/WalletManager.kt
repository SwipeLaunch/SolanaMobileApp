package com.anonymous.SolanaMobileApp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

class WalletManager(private val context: Context) {
    
    companion object {
        private const val TAG = "WalletManager"
        private const val IDENTITY_URI = "https://swipelaunch.fun"
        private const val IDENTITY_NAME = "SwipeLaunch"
        private const val ICON_URI = "favicon.ico"
    }
    
    private var connectedPublicKey: ByteArray? = null
    private var isConnected = false
    
    // Callback interface for wallet events
    interface WalletCallback {
        fun onWalletConnected(publicKey: String)
        fun onWalletDisconnected()
        fun onWalletError(error: String)
        fun onTransactionComplete(signature: String)
    }
    
    private var callback: WalletCallback? = null
    
    fun setCallback(callback: WalletCallback) {
        this.callback = callback
    }
    
    suspend fun connectWallet(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to connect wallet...")
            
            // Check if wallet apps are installed
            val walletIntent = Intent("com.solana.mobilewalletadapter.intent.action.TRANSACT")
            val walletApps = context.packageManager.queryIntentActivities(walletIntent, 0)
            
            if (walletApps.isEmpty()) {
                // No wallet apps found - simulate connection for demo
                Log.d(TAG, "No MWA wallet found, simulating connection...")
                delay(1000) // Simulate connection delay
                
                // Generate a mock public key
                val mockPublicKey = generateMockPublicKey()
                connectedPublicKey = mockPublicKey.toByteArray()
                isConnected = true
                
                Log.d(TAG, "Mock wallet connected successfully!")
                Log.d(TAG, "Mock Public Key: $mockPublicKey")
                
                withContext(Dispatchers.Main) {
                    callback?.onWalletConnected(mockPublicKey)
                }
                
                return@withContext true
            } else {
                // Real wallet apps found - for now just show available wallets
                Log.d(TAG, "Found ${walletApps.size} wallet app(s)")
                
                withContext(Dispatchers.Main) {
                    callback?.onWalletError("Real MWA integration coming soon! Found ${walletApps.size} wallet app(s). For now using mock wallet.")
                }
                
                // Still connect with mock for demo
                delay(1000)
                val mockPublicKey = generateMockPublicKey()
                connectedPublicKey = mockPublicKey.toByteArray()
                isConnected = true
                
                withContext(Dispatchers.Main) {
                    callback?.onWalletConnected(mockPublicKey)
                }
                
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Wallet connection failed", e)
            withContext(Dispatchers.Main) {
                callback?.onWalletError("Connection failed: ${e.message}")
            }
            false
        }
    }
    
    private fun generateMockPublicKey(): String {
        // Use the specified wallet address for testing
        return "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
    }
    
    suspend fun disconnectWallet() = withContext(Dispatchers.IO) {
        try {
            if (isConnected) {
                // Simulate disconnection
                delay(500)
                
                connectedPublicKey = null
                isConnected = false
                
                withContext(Dispatchers.Main) {
                    callback?.onWalletDisconnected()
                }
                
                Log.d(TAG, "Wallet disconnected")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Wallet disconnection failed", e)
            withContext(Dispatchers.Main) {
                callback?.onWalletError("Disconnection failed: ${e.message}")
            }
            false
        }
    }
    
    fun isWalletConnected(): Boolean = isConnected
    
    fun getConnectedPublicKey(): String? {
        return connectedPublicKey?.let { String(it) }
    }
    
    suspend fun requestAirdrop(amount: Long = 1000000000): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isConnected || connectedPublicKey == null) {
                withContext(Dispatchers.Main) {
                    callback?.onWalletError("Wallet not connected")
                }
                return@withContext false
            }
            
            // Simulate airdrop request
            Log.d(TAG, "Requesting airdrop of $amount lamports")
            delay(2000) // Simulate network delay
            
            // Generate mock transaction signature
            val mockSignature = generateMockSignature()
            
            withContext(Dispatchers.Main) {
                callback?.onTransactionComplete(mockSignature)
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Airdrop request failed", e)
            withContext(Dispatchers.Main) {
                callback?.onWalletError("Airdrop failed: ${e.message}")
            }
            false
        }
    }
    
    private fun generateMockSignature(): String {
        // Generate a realistic-looking transaction signature
        val chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        return (1..88).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    // Format public key for display (show first 4 and last 4 characters)
    fun formatPublicKeyForDisplay(publicKey: String): String {
        return if (publicKey.length >= 8) {
            "${publicKey.take(4)}...${publicKey.takeLast(4)}"
        } else {
            publicKey
        }
    }
    
    // Get current connected wallet address
    fun getConnectedWalletAddress(): String? {
        return if (isConnected && connectedPublicKey != null) {
            String(connectedPublicKey!!)
        } else {
            null
        }
    }
}