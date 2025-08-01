package com.anonymous.SolanaMobileApp

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var swipeContainer: FrameLayout
    private lateinit var walletManager: WalletManager
    private val tokenCards = mutableListOf<SwipeableTokenCard>()
    private var currentCardIndex = 0
    
    // Profile page views
    private lateinit var profilePage: View
    private lateinit var discoverContent: View
    private lateinit var profileWalletButton: Button
    private lateinit var walletStatusText: TextView
    private lateinit var profileDetailsCard: CardView
    private lateinit var activityCard: CardView
    private lateinit var profileWalletAddress: TextView
    private lateinit var profileSolBalance: TextView
    private lateinit var requestAirdropButton: Button
    private lateinit var copyAddressButton: Button
    private lateinit var tokensLikedCount: TextView
    private lateinit var tokensPassedCount: TextView
    private lateinit var totalSwipesCount: TextView
    
    // Sample token data
    private val tokens = listOf(
        TokenData("1", "MOON Token", "cryptoking", "🚀 To the moon! The next big thing in DeFi. Diamond hands only! 💎🙌", 1247,
                 "ABC123...XYZ789", "cryptoking", "sl_moon", 2500, 15420, 8, "https://discord.gg/moontoken"),
        TokenData("2", "DOGE 2.0", "memeLord", "🐕 Much wow, very token! The evolution of meme coins on Solana blockchain.", 856,
                 "DEF456...UVW012", "memelord99", "sl_doge", 1800, 8560, 5, "https://telegram.org/dogetoken"),
        TokenData("3", "SOL CAT", "catWhisperer", "🐱 Purr-fect token for cat lovers! Meow your way to financial freedom with SOL CAT.", 2103,
                 "GHI789...RST345", "catwhisperer", "sl_cat", 3200, 21030, 12, "https://discord.gg/solcat"),
        TokenData("4", "LAMBO Token", "speedDemon", "🏎️ When lambo? NOW! The token that will actually get you that lambo. Built different.", 3421,
                 "JKL012...OPQ678", "speeddemon", "sl_lambo", 5000, 34210, 15, "https://telegram.org/lambo"),
        TokenData("5", "DIAMOND", "gemHunter", "💎 Unbreakable hands, unbreakable token. DIAMOND is forever on Solana blockchain.", 567,
                 "MNO345...TUV901", "gemhunter", "sl_diamond", 1200, 5670, 3, "https://discord.gg/diamond")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupWalletManager()
        setupBottomNavigation()
        setupSwipeCards()
    }
    
    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        swipeContainer = findViewById(R.id.swipeContainer)
        
        // Initialize page views
        discoverContent = findViewById(R.id.discoverContent)
        profilePage = findViewById(R.id.profilePage)
        
        // Initialize profile page views
        profileWalletButton = profilePage.findViewById(R.id.profileWalletButton)
        walletStatusText = profilePage.findViewById(R.id.walletStatusText)
        profileDetailsCard = profilePage.findViewById(R.id.profileDetailsCard)
        activityCard = profilePage.findViewById(R.id.activityCard)
        profileWalletAddress = profilePage.findViewById(R.id.profileWalletAddress)
        profileSolBalance = profilePage.findViewById(R.id.profileSolBalance)
        requestAirdropButton = profilePage.findViewById(R.id.requestAirdropButton)
        copyAddressButton = profilePage.findViewById(R.id.copyAddressButton)
        tokensLikedCount = profilePage.findViewById(R.id.tokensLikedCount)
        tokensPassedCount = profilePage.findViewById(R.id.tokensPassedCount)
        totalSwipesCount = profilePage.findViewById(R.id.totalSwipesCount)
    }
    
    private fun setupWalletManager() {
        walletManager = WalletManager(this)
        
        walletManager.setCallback(object : WalletManager.WalletCallback {
            override fun onWalletConnected(publicKey: String) {
                val displayKey = walletManager.formatPublicKeyForDisplay(publicKey)
                updateProfileUI(true, publicKey, displayKey)
                showToast("Wallet connected: $displayKey")
            }
            
            override fun onWalletDisconnected() {
                updateProfileUI(false, null, null)
                showToast("Wallet disconnected")
            }
            
            override fun onWalletError(error: String) {
                showToast("Wallet error: $error")
            }
            
            override fun onTransactionComplete(signature: String) {
                showToast("Transaction complete: ${signature.take(8)}...")
                // Update SOL balance after transaction (simulate)
                profileSolBalance.text = "1.00 SOL"
            }
        })
        
        profileWalletButton.setOnClickListener {
            if (walletManager.isWalletConnected()) {
                // Disconnect wallet
                lifecycleScope.launch {
                    walletManager.disconnectWallet()
                }
            } else {
                // Connect wallet
                lifecycleScope.launch {
                    walletManager.connectWallet()
                }
            }
        }
        
        // Setup profile action buttons
        requestAirdropButton.setOnClickListener {
            lifecycleScope.launch {
                walletManager.requestAirdrop()
            }
        }
        
        copyAddressButton.setOnClickListener {
            walletManager.getConnectedPublicKey()?.let { address ->
                val clipboard = getSystemService(ClipboardManager::class.java)
                val clip = ClipData.newPlainText("Wallet Address", address)
                clipboard.setPrimaryClip(clip)
                showToast("Address copied to clipboard")
            }
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_discover -> {
                    showDiscoverTab()
                    true
                }
                R.id.nav_superstar -> {
                    showToast("Superstar Tab")
                    true
                }
                R.id.nav_rankings -> {
                    showToast("Rankings Tab")
                    true
                }
                R.id.nav_activities -> {
                    showToast("Activities Tab")
                    true
                }
                R.id.nav_profile -> {
                    showProfileTab()
                    true
                }
                else -> false
            }
        }
        
        // Select discover tab by default
        bottomNavigation.selectedItemId = R.id.nav_discover
    }
    
    private fun setupSwipeCards() {
        // Clear existing cards
        swipeContainer.removeAllViews()
        tokenCards.clear()
        
        // Create cards for first 3 tokens (stack of 3)
        for (i in 0 until minOf(3, tokens.size)) {
            createAndAddCard(currentCardIndex + i)
        }
    }
    
    private fun createAndAddCard(tokenIndex: Int) {
        if (tokenIndex >= tokens.size) return
        
        val card = SwipeableTokenCard(this)
        card.setTokenData(tokens[tokenIndex])
        
        // Store the token index in the card tag for later reference
        card.tag = tokenIndex
        
        card.setOnSwipeListener(object : SwipeableTokenCard.OnSwipeListener {
            override fun onSwipeRight(swipedCard: SwipeableTokenCard) {
                val cardTokenIndex = swipedCard.tag as Int
                handleSwipeRight(swipedCard, cardTokenIndex)
            }
            
            override fun onSwipeLeft(swipedCard: SwipeableTokenCard) {
                val cardTokenIndex = swipedCard.tag as Int
                handleSwipeLeft(swipedCard, cardTokenIndex)
            }

            override fun onLikeButton(swipedCard: SwipeableTokenCard) {
                val cardTokenIndex = swipedCard.tag as Int
                handleSwipeRight(swipedCard, cardTokenIndex)
            }

            override fun onDislikeButton(swipedCard: SwipeableTokenCard) {
                val cardTokenIndex = swipedCard.tag as Int
                handleSwipeLeft(swipedCard, cardTokenIndex)
            }
        })
        
        // Position cards in stack (z-index effect)
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        
        // Offset cards slightly for stack effect
        val cardPosition = tokenCards.size
        layoutParams.topMargin = cardPosition * 8
        layoutParams.leftMargin = cardPosition * 4
        layoutParams.rightMargin = cardPosition * 4
        
        card.layoutParams = layoutParams
        card.elevation = (10 - cardPosition).toFloat()
        
        swipeContainer.addView(card, 0) // Add to bottom of stack
        tokenCards.add(card)
    }
    
    private fun handleSwipeRight(card: SwipeableTokenCard, tokenIndex: Int) {
        showToast("❤️ Liked ${tokens[tokenIndex].name}!")
        removeCardAndAddNext(card, tokenIndex)
    }
    
    private fun handleSwipeLeft(card: SwipeableTokenCard, tokenIndex: Int) {
        showToast("👎 Passed on ${tokens[tokenIndex].name}")
        removeCardAndAddNext(card, tokenIndex)
    }
    
    private fun removeCardAndAddNext(card: SwipeableTokenCard, tokenIndex: Int) {
        // Remove swiped card
        swipeContainer.removeView(card)
        tokenCards.remove(card)
        
        // Update current index to next available token
        currentCardIndex++
        
        // Add next card if available
        if (currentCardIndex + tokenCards.size < tokens.size) {
            createAndAddCard(currentCardIndex + tokenCards.size)
        }
        
        // If no more cards, show completion message and reset
        if (tokenCards.isEmpty()) {
            showToast("🎉 You've seen all tokens! Restarting...")
            currentCardIndex = 0
            setupSwipeCards() // Reset for continuous demo
        }
    }
    
    private fun showDiscoverTab() {
        discoverContent.visibility = View.VISIBLE
        profilePage.visibility = View.GONE
    }
    
    private fun showProfileTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.VISIBLE
    }
    
    private fun updateProfileUI(connected: Boolean, fullAddress: String?, displayAddress: String?) {
        if (connected && fullAddress != null && displayAddress != null) {
            // Wallet connected - show profile details
            profileWalletButton.text = "Disconnect Wallet"
            walletStatusText.text = "Wallet connected successfully"
            profileDetailsCard.visibility = View.VISIBLE
            activityCard.visibility = View.VISIBLE
            
            // Update profile info
            profileWalletAddress.text = displayAddress
            profileSolBalance.text = "0.00 SOL"
            
            // Mock activity stats (in real app, track these)
            tokensLikedCount.text = "0"
            tokensPassedCount.text = "0" 
            totalSwipesCount.text = "0"
        } else {
            // Wallet disconnected - hide profile details
            profileWalletButton.text = "Connect Solana Wallet"
            walletStatusText.text = "Connect your wallet to see your profile details"
            profileDetailsCard.visibility = View.GONE
            activityCard.visibility = View.GONE
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}