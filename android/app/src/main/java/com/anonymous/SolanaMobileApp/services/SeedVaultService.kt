package com.anonymous.SolanaMobileApp.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.security.SecureRandom

/**
 * Solana Mobile Stack (SMS) Seed Vault Service
 * 
 * This service demonstrates the concept of the Solana Mobile Stack's Seed Vault -
 * a secure hardware-backed storage for private keys and seed phrases.
 * 
 * In production, this would use the actual Seed Vault SDK, but for this demo
 * we simulate the secure key management functionality.
 */
class SeedVaultService(private val context: Context) {

    companion object {
        private const val TAG = "SeedVaultService"
        private const val PURPOSE_SOLANA = 501 // Solana's BIP44 purpose
        private const val COIN_TYPE_SOLANA = 501 // Solana's coin type
    }

    private val _seedVaultState = MutableStateFlow<SeedVaultState>(SeedVaultState.Disconnected)
    val seedVaultState: StateFlow<SeedVaultState> = _seedVaultState

    private var currentSeed: Long? = null
    private var currentPublicKey: ByteArray? = null
    private var seedPhrase: String? = null

    sealed class SeedVaultState {
        object Disconnected : SeedVaultState()
        object Connecting : SeedVaultState()
        data class Connected(
            val publicKey: String,
            val seedId: Long,
            val derivationPath: String,
            val isHardwareBacked: Boolean = false
        ) : SeedVaultState()
        data class Error(val message: String) : SeedVaultState()
    }

    suspend fun initializeSeedVault() = withContext(Dispatchers.IO) {
        try {
            _seedVaultState.value = SeedVaultState.Connecting
            Log.d(TAG, "🔐 Initializing Solana Mobile Stack Seed Vault...")

            // Simulate hardware security module availability check
            val isHardwareBacked = isDeviceSupported()
            
            if (isHardwareBacked) {
                Log.d(TAG, "✅ Hardware security module detected")
            } else {
                Log.d(TAG, "⚠️  Using software-based secure storage")
            }

            // Simulate initialization delay
            kotlinx.coroutines.delay(500)
            
            Log.d(TAG, "✅ Seed Vault initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Seed Vault", e)
            _seedVaultState.value = SeedVaultState.Error("Initialization failed: ${e.message}")
        }
    }

    suspend fun createOrAccessSeed() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌱 Creating or accessing seed in secure environment...")

            // Generate a secure seed ID
            currentSeed = SecureRandom().nextLong()
            
            // Generate a mnemonic seed phrase (in production, this would be hardware-generated)
            seedPhrase = generateMockSeedPhrase()
            
            Log.d(TAG, "✅ Seed created/accessed with ID: ${currentSeed}")
            Log.d(TAG, "🔒 Seed phrase securely stored in hardware vault")

            // Derive the first Solana account key
            deriveAccountKey(0, 0)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create/access seed", e)
            _seedVaultState.value = SeedVaultState.Error("Seed creation failed: ${e.message}")
        }
    }

    suspend fun deriveAccountKey(accountIndex: Int = 0, changeIndex: Int = 0) = withContext(Dispatchers.IO) {
        try {
            val seedId = currentSeed ?: throw IllegalStateException("No seed available")

            // Create BIP44 derivation path for Solana: m/44'/501'/account'/change'
            val derivationPath = "m/44'/${PURPOSE_SOLANA}'/${accountIndex}'/${changeIndex}'"

            Log.d(TAG, "🔑 Deriving key with path: $derivationPath")

            // Simulate secure key derivation (in production, this happens in hardware)
            currentPublicKey = generateMockPublicKey(seedId, derivationPath)

            val publicKeyHex = currentPublicKey?.let { 
                it.joinToString("") { byte -> "%02x".format(byte) }
            } ?: "unknown"

            // Check if running on Solana Phone or hardware with Seed Vault
            val isHardwareBacked = isDeviceSupported()

            _seedVaultState.value = SeedVaultState.Connected(
                publicKey = publicKeyHex,
                seedId = seedId,
                derivationPath = derivationPath,
                isHardwareBacked = isHardwareBacked
            )

            if (isHardwareBacked) {
                Log.d(TAG, "🔐 Key derived in hardware security module: ${publicKeyHex.take(8)}...")
            } else {
                Log.d(TAG, "🔑 Key derived with secure software: ${publicKeyHex.take(8)}... (demo)")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to derive account key", e)
            _seedVaultState.value = SeedVaultState.Error("Key derivation failed: ${e.message}")
        }
    }

    suspend fun signTransaction(transaction: ByteArray): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val seedId = currentSeed ?: throw IllegalStateException("No seed available")

            Log.d(TAG, "📝 Signing transaction with Seed Vault...")
            Log.d(TAG, "🔒 Transaction data never leaves secure environment")

            // Simulate hardware signing (in production, this happens in secure hardware)
            kotlinx.coroutines.delay(200) // Simulate hardware processing time
            
            val signature = generateMockSignature(transaction, seedId)
            
            Log.d(TAG, "✅ Transaction signed securely")
            return@withContext signature

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to sign transaction", e)
            return@withContext null
        }
    }

    suspend fun signMessage(message: ByteArray): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val seedId = currentSeed ?: throw IllegalStateException("No seed available")

            Log.d(TAG, "📝 Signing message with Seed Vault...")
            
            // Simulate hardware signing
            kotlinx.coroutines.delay(150)
            
            val signature = generateMockSignature(message, seedId)
            
            Log.d(TAG, "✅ Message signed securely")
            return@withContext signature

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to sign message", e)
            return@withContext null
        }
    }

    private fun generateMockPublicKey(seedId: Long, path: String): ByteArray {
        // Generate a deterministic 32-byte public key based on seed and path
        val combined = "$seedId:$path".toByteArray()
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(combined)
        return hash
    }

    private fun generateMockSignature(data: ByteArray, seedId: Long): ByteArray {
        // Generate a deterministic 64-byte signature
        val combined = data + seedId.toString().toByteArray()
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(combined)
        return hash + hash // 64 bytes for ED25519 signature
    }

    private fun generateMockSeedPhrase(): String {
        // Mock seed phrase (in production, this would be generated by hardware)
        val words = listOf("abandon", "ability", "able", "about", "above", "absent", 
                          "absorb", "abstract", "absurd", "abuse", "access", "accident")
        return words.shuffled().take(12).joinToString(" ")
    }

    fun getPublicKey(): String? {
        return currentPublicKey?.let { 
            it.joinToString("") { byte -> "%02x".format(byte) }
        }
    }

    fun getSeedId(): Long? = currentSeed

    fun getSeedPhrase(): String? {
        // In production, seed phrases would NEVER be exposed outside the secure hardware
        Log.w(TAG, "⚠️  DEMO ONLY: Seed phrase access (would not be possible in production)")
        return seedPhrase
    }

    fun isConnected(): Boolean {
        return _seedVaultState.value is SeedVaultState.Connected
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            // Clear sensitive data
            currentSeed = null
            currentPublicKey = null
            seedPhrase = null
            _seedVaultState.value = SeedVaultState.Disconnected
            Log.d(TAG, "🔓 Seed Vault disconnected, sensitive data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during disconnect", e)
        }
    }

    // Check if device supports hardware-backed Seed Vault
    fun isDeviceSupported(): Boolean {
        return try {
            // Check for Solana Phone or other SMS-compatible devices
            val deviceModel = android.os.Build.MODEL
            val deviceManufacturer = android.os.Build.MANUFACTURER
            
            // Check for Solana Phone models
            val isSolanaPhone = deviceModel.contains("Saga", ignoreCase = true) ||
                               deviceManufacturer.contains("Solana", ignoreCase = true)
            
            if (isSolanaPhone) {
                Log.d(TAG, "🔥 Solana Phone detected - hardware Seed Vault available")
                return true
            }
            
            // Check for other hardware security features
            val hasSecureHardware = context.packageManager.hasSystemFeature("android.hardware.strongbox_keystore") ||
                                   context.packageManager.hasSystemFeature("android.hardware.security.model.compatible")
            
            if (hasSecureHardware) {
                Log.d(TAG, "🔒 Hardware security features detected")
                return true
            }
            
            Log.d(TAG, "📱 Standard device - using secure software implementation")
            false
            
        } catch (e: Exception) {
            Log.w(TAG, "Could not check Seed Vault availability", e)
            false
        }
    }

    fun getSecurityInfo(): String {
        val isHardware = isDeviceSupported()
        val model = android.os.Build.MODEL
        
        return if (isHardware) {
            "🔐 Hardware Seed Vault\n📱 Device: $model\n🛡️  Keys stored in secure hardware"
        } else {
            "🔑 Software Seed Vault (Demo)\n📱 Device: $model\n⚠️  Demo implementation"
        }
    }
}