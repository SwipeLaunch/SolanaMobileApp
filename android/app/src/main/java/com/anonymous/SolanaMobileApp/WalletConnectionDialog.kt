package com.anonymous.SolanaMobileApp

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class WalletConnectionDialog(
    private val context: Context,
    private val lifecycleScope: CoroutineScope,
    private val seedVaultService: com.anonymous.SolanaMobileApp.services.SeedVaultService,
    private val mwaService: com.anonymous.SolanaMobileApp.services.MobileWalletAdapterService,
    private val onWalletConnected: (String, String) -> Unit
) {

    fun showConnectionOptions() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.wallet_connection_dialog, null)
        
        val dialog = AlertDialog.Builder(context)
            .setTitle("🔐 Connect Wallet")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set up buttons
        val smsButton = dialogView.findViewById<Button>(R.id.connectSmsButton)
        val phantomButton = dialogView.findViewById<Button>(R.id.connectPhantomButton)
        val solflareButton = dialogView.findViewById<Button>(R.id.connectSolflareButton)
        val mockButton = dialogView.findViewById<Button>(R.id.connectMockButton)
        
        val securityInfo = dialogView.findViewById<TextView>(R.id.securityInfo)
        
        // Show security information
        securityInfo.text = seedVaultService.getSecurityInfo()

        // SMS Seed Vault Connection
        smsButton.setOnClickListener {
            dialog.dismiss()
            connectViaSeedVault()
        }

        // Phantom Wallet Connection
        phantomButton.setOnClickListener {
            dialog.dismiss()
            mwaService.connectPhantom()
            showToast("Launching Phantom Wallet...")
        }

        // Solflare Wallet Connection
        solflareButton.setOnClickListener {
            dialog.dismiss()
            mwaService.connectSolflare()
            showToast("Launching Solflare Wallet...")
        }

        // Mock Wallet (for testing)
        mockButton.setOnClickListener {
            dialog.dismiss()
            connectMockWallet()
        }

        dialog.show()
    }

    private fun connectViaSeedVault() {
        showToast("🔐 Connecting via SMS Seed Vault...")
        
        lifecycleScope.launch {
            try {
                Log.d("WalletDialog", "🔐 Initiating SMS Seed Vault connection...")
                
                // Disconnect any existing connections
                seedVaultService.disconnect()
                
                // Initialize and connect via Seed Vault
                seedVaultService.initializeSeedVault()
                seedVaultService.createOrAccessSeed()
                
                // The state will be handled by the observer in MainActivity
                showToast("✅ SMS Seed Vault connected securely!")
                
            } catch (e: Exception) {
                Log.e("WalletDialog", "❌ Failed to connect via Seed Vault", e)
                showToast("❌ Connection failed: ${e.message}")
            }
        }
    }

    private fun connectMockWallet() {
        // Generate a mock wallet for testing
        val mockPublicKey = "W97AHbTgwfsDrDWkKEFjb2jvMF7Mw3UUb2u1hU7C8ABC"
        val displayKey = "${mockPublicKey.take(4)}...${mockPublicKey.takeLast(4)}"
        
        onWalletConnected(mockPublicKey, displayKey)
        showToast("🔓 Mock wallet connected (testing)")
        
        Log.d("WalletDialog", "Mock wallet connected: $displayKey")
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showDisconnectionConfirmation(onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("🔓 Disconnect Wallet")
            .setMessage("Are you sure you want to disconnect your wallet?\n\nThis will clear your wallet connection and you'll need to reconnect.")
            .setPositiveButton("Disconnect") { _, _ ->
                onConfirm()
                showToast("Wallet disconnected")
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}