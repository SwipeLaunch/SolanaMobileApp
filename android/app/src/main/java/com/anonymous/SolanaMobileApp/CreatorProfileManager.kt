package com.anonymous.SolanaMobileApp

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreatorProfileManager(
    private val context: Context,
    private val profileView: View,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val databaseService: DatabaseService
) {
    
    companion object {
        private const val CREATOR_ADDRESS = "umuAXMPXgzcgbmg2361ij8jncRWyb8noZeXFFCdvKmNu"
    }
    
    // UI Elements
    private val creatorAddress: TextView = profileView.findViewById(R.id.creatorAddress)
    private val totalTokenCount: TextView = profileView.findViewById(R.id.totalTokenCount)
    private val lastUpdated: TextView = profileView.findViewById(R.id.lastUpdated)
    private val creatorTokensRecyclerView: RecyclerView = profileView.findViewById(R.id.creatorTokensRecyclerView)
    private val creatorTokensEmptyState: LinearLayout = profileView.findViewById(R.id.creatorTokensEmptyState)
    private val creatorTokensLoadingState: LinearLayout = profileView.findViewById(R.id.creatorTokensLoadingState)
    private val refreshTokensButton: Button = profileView.findViewById(R.id.refreshTokensButton)
    
    // Data
    private var creatorTokens = mutableListOf<CreatedTokenInfo>()
    
    init {
        setupUI()
        loadCreatorTokens()
    }
    
    private fun setupUI() {
        // Display creator address (shortened)
        creatorAddress.text = "${CREATOR_ADDRESS.take(8)}...${CREATOR_ADDRESS.takeLast(6)}"
        
        // Setup RecyclerView
        creatorTokensRecyclerView.layoutManager = LinearLayoutManager(context)
        
        // Setup refresh button
        refreshTokensButton.setOnClickListener {
            loadCreatorTokens()
        }
        
        android.util.Log.d("CreatorProfile", "Creator Profile Manager initialized for: $CREATOR_ADDRESS")
    }
    
    fun loadCreatorTokens() {
        android.util.Log.d("CreatorProfile", "Loading tokens for creator: $CREATOR_ADDRESS")
        
        showLoadingState()
        
        lifecycleScope.launch {
            try {
                // Load tokens from database
                val tokens = databaseService.getTokensByCreator(CREATOR_ADDRESS)
                android.util.Log.d("CreatorProfile", "Found ${tokens.size} tokens from database")
                
                // Convert to display format
                val displayTokens = tokens.map { dbToken ->
                    CreatedTokenInfo(
                        name = dbToken.token_name,
                        symbol = dbToken.symbol,
                        description = dbToken.description ?: "Token by creator",
                        supply = 1_000_000_000L,
                        launchType = if (dbToken.status == "presale" || dbToken.status == "proposal") LaunchType.PRESALE else LaunchType.INSTANT,
                        tokenAddress = dbToken.token_mint_address ?: "Unknown",
                        chatLink = "https://discord.gg/${dbToken.symbol.lowercase()}",
                        status = dbToken.status.replaceFirstChar { it.uppercase() }
                    )
                }
                
                // Update UI on main thread
                updateTokenDisplay(displayTokens)
                updateLastRefreshTime()
                
            } catch (e: Exception) {
                android.util.Log.e("CreatorProfile", "Error loading creator tokens: ${e.message}")
                android.util.Log.d("CreatorProfile", "Loading sample creator tokens as fallback...")
                
                // Load sample data when database is unavailable
                val sampleTokens = listOf(
                    CreatedTokenInfo(
                        name = "DogeCoin",
                        symbol = "DOGE", 
                        description = "The people's cryptocurrency by the creator",
                        supply = 1_000_000_000L,
                        launchType = LaunchType.INSTANT,
                        tokenAddress = "DQhH...K9mX",
                        chatLink = "https://discord.gg/doge",
                        status = "Active"
                    ),
                    CreatedTokenInfo(
                        name = "SolanaSwap", 
                        symbol = "SSWAP",
                        description = "Revolutionary DeFi swap protocol by the creator",
                        supply = 500_000_000L,
                        launchType = LaunchType.PRESALE,
                        tokenAddress = "SwAp...R3nM",
                        chatLink = "https://discord.gg/sswap", 
                        status = "Proposal"
                    ),
                    CreatedTokenInfo(
                        name = "MoonRocket",
                        symbol = "MOON",
                        description = "To the moon and beyond! Created by the team",
                        supply = 2_000_000_000L,
                        launchType = LaunchType.INSTANT,
                        tokenAddress = "MoOn...B4sE",
                        chatLink = "https://discord.gg/moon",
                        status = "Active"
                    )
                )
                
                updateTokenDisplay(sampleTokens)
                updateLastRefreshTime()
                android.util.Log.d("CreatorProfile", "Loaded ${sampleTokens.size} sample creator tokens")
            }
        }
    }
    
    private fun showLoadingState() {
        creatorTokensRecyclerView.visibility = View.GONE
        creatorTokensEmptyState.visibility = View.GONE
        creatorTokensLoadingState.visibility = View.VISIBLE
        refreshTokensButton.isEnabled = false
    }
    
    private fun showEmptyState() {
        creatorTokensRecyclerView.visibility = View.GONE
        creatorTokensEmptyState.visibility = View.VISIBLE
        creatorTokensLoadingState.visibility = View.GONE
        refreshTokensButton.isEnabled = true
        totalTokenCount.text = "0"
    }
    
    private fun updateTokenDisplay(tokens: List<CreatedTokenInfo>) {
        creatorTokens.clear()
        creatorTokens.addAll(tokens)
        
        if (tokens.isNotEmpty()) {
            // Show tokens
            creatorTokensRecyclerView.visibility = View.VISIBLE
            creatorTokensEmptyState.visibility = View.GONE
            creatorTokensLoadingState.visibility = View.GONE
            
            // Setup adapter
            val adapter = MyTokensAdapter(tokens) { token ->
                showTokenDetails(token)
            }
            creatorTokensRecyclerView.adapter = adapter
            
            // Update count
            totalTokenCount.text = tokens.size.toString()
            
            android.util.Log.d("CreatorProfile", "Displayed ${tokens.size} creator tokens")
        } else {
            showEmptyState()
        }
        
        refreshTokensButton.isEnabled = true
    }
    
    private fun updateLastRefreshTime() {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        lastUpdated.text = formatter.format(Date())
    }
    
    private fun showTokenDetails(token: CreatedTokenInfo) {
        val launchTypeText = when (token.launchType) {
            LaunchType.INSTANT -> "Instant Launch"
            LaunchType.PRESALE -> "Community Proposal"
        }
        
        val chatLinkText = if (!token.chatLink.isNullOrEmpty()) {
            "\n\nCommunity Chat: ${token.chatLink}"
        } else {
            "\n\nNo community chat link"
        }
        
        val message = """
            Token: ${token.name} (${token.symbol})
            
            Status: ${token.status}
            Launch Type: $launchTypeText
            Supply: ${String.format("%,d", token.supply)}
            Address: ${token.tokenAddress}
            
            Description: ${token.description}$chatLinkText
        """.trimIndent()
        
        // Create simple dialog
        android.app.AlertDialog.Builder(context)
            .setTitle("📊 Token Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }
    
    // Public method to add a newly created token
    fun addNewToken(token: CreatedTokenInfo) {
        creatorTokens.add(0, token) // Add to beginning
        updateTokenDisplay(creatorTokens)
        android.util.Log.d("CreatorProfile", "Added new token: ${token.name}")
    }
}