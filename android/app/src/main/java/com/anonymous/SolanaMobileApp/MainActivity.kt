package com.anonymous.SolanaMobileApp

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var presalePage: View
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
    
    // Presale page views
    private lateinit var presaleRecyclerView: RecyclerView
    private lateinit var walletBalanceText: TextView
    private lateinit var presaleAdapter: PresaleAdapter
    
    // Leaderboard page views
    private lateinit var leaderboardPage: View
    private lateinit var tabTokenLaunched: TextView
    private lateinit var tabSLStaked: TextView
    private lateinit var tabMostLikes: TextView
    private lateinit var tabMostLaunched: TextView
    private lateinit var tokenLaunchedRecyclerView: RecyclerView
    private lateinit var slStakedRecyclerView: RecyclerView
    private lateinit var mostLikesRecyclerView: RecyclerView
    private lateinit var mostLaunchedRecyclerView: RecyclerView
    
    // Activity page views
    private lateinit var activityPage: View
    private lateinit var activityFeedRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var exploreCreatorsButton: Button
    private lateinit var activityLikesCount: TextView
    private lateinit var activityPresalesCount: TextView
    private lateinit var activityFollowingCount: TextView
    private lateinit var activityFeedAdapter: ActivityFeedAdapter
    
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
    
    // Sample presale data
    private val presaleTokens = listOf(
        PresaleTokenData("ps1", "ROCKET Token", "ROCKET", "🚀 Next-gen DeFi protocol with advanced yield farming capabilities", 
                        1_000_000_000L, 75.2, System.currentTimeMillis(), System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L), 
                        "Creator1", "TokenAddr1"),
        PresaleTokenData("ps2", "MOON Coin", "MOON", "🌙 Community-driven memecoin with deflationary tokenomics", 
                        500_000_000L, 42.8, System.currentTimeMillis(), System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000L), 
                        "Creator2", "TokenAddr2"),
        PresaleTokenData("ps3", "DIAMOND", "DMD", "💎 Premium store of value token with limited supply and staking rewards", 
                        100_000_000L, 89.1, System.currentTimeMillis(), System.currentTimeMillis() + (1 * 24 * 60 * 60 * 1000L), 
                        "Creator3", "TokenAddr3"),
        PresaleTokenData("ps4", "SOLAR Power", "SOLAR", "☀️ Green energy token supporting sustainable blockchain mining", 
                        2_000_000_000L, 23.5, System.currentTimeMillis(), System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), 
                        "Creator4", "TokenAddr4"),
        PresaleTokenData("ps5", "GALAXY Token", "GLXY", "🌌 Cross-chain gaming token for the metaverse ecosystem", 
                        750_000_000L, 67.3, System.currentTimeMillis(), System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L), 
                        "Creator5", "TokenAddr5")
    )
    
    // Sample leaderboard data
    private val tokenLaunchedLeaderboard = listOf(
        TokenLaunchedData(1, "MOON Token", "MOON", 2_500_000.0, 500_000_000.0, "cryptoking", System.currentTimeMillis()),
        TokenLaunchedData(2, "ROCKET Coin", "ROCKET", 1_800_000.0, 360_000_000.0, "spaceexplorer", System.currentTimeMillis()),
        TokenLaunchedData(3, "DIAMOND", "DMD", 1_200_000.0, 240_000_000.0, "gemhunter", System.currentTimeMillis()),
        TokenLaunchedData(4, "SOLAR Power", "SOLAR", 950_000.0, 190_000_000.0, "greenenergy", System.currentTimeMillis()),
        TokenLaunchedData(5, "GALAXY Token", "GLXY", 750_000.0, 150_000_000.0, "metaverse", System.currentTimeMillis())
    )
    
    private val slStakedLeaderboard = listOf(
        SLTokenStakedData(1, "ABC123...XYZ789", "cryptoking", "cryptoking.sol", 2_500_000.0, 90 * 24 * 60 * 60 * 1000L, 125_000.0),
        SLTokenStakedData(2, "DEF456...UVW012", "moonwhale", null, 1_800_000.0, 60 * 24 * 60 * 60 * 1000L, 90_000.0),
        SLTokenStakedData(3, "GHI789...RST345", null, "trader.sol", 1_200_000.0, 45 * 24 * 60 * 60 * 1000L, 60_000.0),
        SLTokenStakedData(4, "JKL012...OPQ678", "degenape", null, 950_000.0, 30 * 24 * 60 * 60 * 1000L, 47_500.0),
        SLTokenStakedData(5, "MNO345...TUV901", null, "hodler.sol", 750_000.0, 15 * 24 * 60 * 60 * 1000L, 37_500.0)
    )
    
    private val creatorMostLikesLeaderboard = listOf(
        CreatorMostLikesData(1, "cryptoking", "cryptoking", 1_250_000, 15, 83_333.0, "MOON Token", 987_654),
        CreatorMostLikesData(2, "memelord", "memelord99", 950_000, 12, 79_167.0, "DOGE 2.0", 456_789),
        CreatorMostLikesData(3, "gemhunter", "gemhunter", 850_000, 10, 85_000.0, "DIAMOND", 234_567),
        CreatorMostLikesData(4, "spaceexplorer", "spaceX_fan", 720_000, 8, 90_000.0, "ROCKET Coin", 345_678),
        CreatorMostLikesData(5, "catwhisperer", "catwhisperer", 650_000, 9, 72_222.0, "SOL CAT", 123_456)
    )
    
    private val creatorMostLaunchedLeaderboard = listOf(
        CreatorMostLaunchedData(1, "prolific_dev", "prolific_dev", 25, 5_250_000.0, 0.72, "MEGA Token", 1_500_000.0),
        CreatorMostLaunchedData(2, "token_factory", "token_factory", 18, 3_200_000.0, 0.67, "SUPER Coin", 800_000.0),
        CreatorMostLaunchedData(3, "cryptoking", "cryptoking", 15, 4_100_000.0, 0.80, "MOON Token", 2_500_000.0),
        CreatorMostLaunchedData(4, "serial_launcher", "serial_launcher", 12, 2_100_000.0, 0.58, "WINNER Token", 450_000.0),
        CreatorMostLaunchedData(5, "defi_builder", "defi_builder", 10, 1_800_000.0, 0.70, "YIELD Token", 350_000.0)
    )
    
    // Sample activity feed data
    private val activityFeedData = listOf(
        ActivityFeedData("1", "ck1", "cryptoking", "CK", ActivityType.LIKE, "liked MOON Token by spaceexplorer", 
            System.currentTimeMillis() - 120000, TokenActivityInfo("MOON Token", "spaceexplorer", "0.05 SOL")),
        ActivityFeedData("2", "ml1", "memelord", "ML", ActivityType.PRESALE, "joined DOGE 2.0 presale", 
            System.currentTimeMillis() - 300000, TokenActivityInfo("DOGE 2.0", "memelord", "0.03 SOL")),
        ActivityFeedData("3", "se1", "spaceexplorer", "SE", ActivityType.LAUNCH, "launched ROCKET Coin", 
            System.currentTimeMillis() - 600000, TokenActivityInfo("ROCKET Coin", "spaceexplorer", "0.08 SOL")),
        ActivityFeedData("4", "cw1", "catwhisperer", "CW", ActivityType.LIKE, "liked DIAMOND by gemhunter", 
            System.currentTimeMillis() - 900000, TokenActivityInfo("DIAMOND", "gemhunter", "0.12 SOL")),
        ActivityFeedData("5", "sd1", "speedDemon", "SD", ActivityType.PRESALE, "joined SOLAR Power presale", 
            System.currentTimeMillis() - 1200000, TokenActivityInfo("SOLAR Power", "greenenergy", "0.04 SOL")),
        ActivityFeedData("6", "gh1", "gemhunter", "GH", ActivityType.FOLLOW, "started following prolific_dev", 
            System.currentTimeMillis() - 1800000, null),
        ActivityFeedData("7", "ml2", "memelord", "ML", ActivityType.LIKE, "liked GALAXY Token by metaverse", 
            System.currentTimeMillis() - 2400000, TokenActivityInfo("GALAXY Token", "metaverse", "0.06 SOL"))
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
        presalePage = findViewById(R.id.presalePage)
        
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
        
        // Initialize presale page views
        presaleRecyclerView = presalePage.findViewById(R.id.presaleRecyclerView)
        walletBalanceText = presalePage.findViewById(R.id.walletBalanceText)
        
        // Setup presale RecyclerView
        presaleAdapter = PresaleAdapter(presaleTokens) { token -> 
            showBuyDialog(token)
        }
        presaleRecyclerView.layoutManager = LinearLayoutManager(this)
        presaleRecyclerView.adapter = presaleAdapter
        
        // Initialize leaderboard page views
        leaderboardPage = findViewById(R.id.leaderboardPage)
        tabTokenLaunched = leaderboardPage.findViewById(R.id.tabTokenLaunched)
        tabSLStaked = leaderboardPage.findViewById(R.id.tabSLStaked)
        tabMostLikes = leaderboardPage.findViewById(R.id.tabMostLikes)
        tabMostLaunched = leaderboardPage.findViewById(R.id.tabMostLaunched)
        tokenLaunchedRecyclerView = leaderboardPage.findViewById(R.id.tokenLaunchedRecyclerView)
        slStakedRecyclerView = leaderboardPage.findViewById(R.id.slStakedRecyclerView)
        mostLikesRecyclerView = leaderboardPage.findViewById(R.id.mostLikesRecyclerView)
        mostLaunchedRecyclerView = leaderboardPage.findViewById(R.id.mostLaunchedRecyclerView)
        
        // Initialize activity page views
        activityPage = findViewById(R.id.activityPage)
        activityFeedRecyclerView = activityPage.findViewById(R.id.activityFeedRecyclerView)
        emptyStateLayout = activityPage.findViewById<LinearLayout>(R.id.emptyStateLayout)
        exploreCreatorsButton = activityPage.findViewById(R.id.exploreCreatorsButton)
        activityLikesCount = activityPage.findViewById(R.id.activityLikesCount)
        activityPresalesCount = activityPage.findViewById(R.id.activityPresalesCount)
        activityFollowingCount = activityPage.findViewById(R.id.activityFollowingCount)
        
        setupLeaderboardTabs()
        setupLeaderboardRecyclerViews()
        setupActivityPage()
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
                    showPresaleTab()
                    true
                }
                R.id.nav_rankings -> {
                    showLeaderboardTab()
                    true
                }
                R.id.nav_activities -> {
                    showActivityTab()
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
        presalePage.visibility = View.GONE
        leaderboardPage.visibility = View.GONE
        activityPage.visibility = View.GONE
    }
    
    private fun showProfileTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.VISIBLE
        presalePage.visibility = View.GONE
        leaderboardPage.visibility = View.GONE
        activityPage.visibility = View.GONE
    }
    
    private fun showPresaleTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.GONE
        presalePage.visibility = View.VISIBLE
        leaderboardPage.visibility = View.GONE
        activityPage.visibility = View.GONE
        updateWalletBalance()
    }
    
    private fun showActivityTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.GONE
        presalePage.visibility = View.GONE
        leaderboardPage.visibility = View.GONE
        activityPage.visibility = View.VISIBLE
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
    
    private fun updateWalletBalance() {
        // Mock wallet balance - in real app, get from wallet
        val balance = if (walletManager.isWalletConnected()) "1.25 SOL" else "0.00 SOL"
        walletBalanceText.text = balance
    }
    
    private fun showBuyDialog(token: PresaleTokenData) {
        if (!walletManager.isWalletConnected()) {
            showToast("Please connect your wallet first in the Profile tab")
            return
        }
        
        val tokensPerSol = token.getTokensPerSol()
        val dialog = AlertDialog.Builder(this)
            .setTitle("Buy ${token.symbol}")
            .setMessage("Purchase ${token.name} tokens\n\n" +
                       "Rate: 1 SOL = ${String.format("%.0f", tokensPerSol)} ${token.symbol}\n" +
                       "Target: 100 SOL (${token.getProgressPercentage()}% complete)\n\n" +
                       "How much SOL do you want to spend?")
            .setView(createBuyDialogView(token))
            .setPositiveButton("Confirm Purchase") { _, _ ->
                // In real implementation, this would create and sign a transaction
                processPurchase(token, 1.0) // Mock 1 SOL purchase
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun createBuyDialogView(token: PresaleTokenData): View {
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        // For simplicity, using basic view - in real app, create custom dialog layout
        return view
    }
    
    private fun processPurchase(token: PresaleTokenData, solAmount: Double) {
        if (!walletManager.isWalletConnected()) {
            showToast("Wallet not connected")
            return
        }
        
        lifecycleScope.launch {
            showToast("Processing transaction...")
            
            // Simulate transaction processing
            kotlinx.coroutines.delay(2000)
            
            val tokensReceived = token.getTokensPerSol() * solAmount
            showToast("✅ Purchase successful!\nReceived ${String.format("%.0f", tokensReceived)} ${token.symbol}")
            
            // In real app, update token progress and user's balance
        }
    }
    
    private fun showLeaderboardTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.GONE
        presalePage.visibility = View.GONE
        leaderboardPage.visibility = View.VISIBLE
        activityPage.visibility = View.GONE
    }
    
    private fun setupLeaderboardTabs() {
        tabTokenLaunched.setOnClickListener { showLeaderboardTab("token_launched") }
        tabSLStaked.setOnClickListener { showLeaderboardTab("sl_staked") }
        tabMostLikes.setOnClickListener { showLeaderboardTab("most_likes") }
        tabMostLaunched.setOnClickListener { showLeaderboardTab("most_launched") }
    }
    
    private fun setupLeaderboardRecyclerViews() {
        // Token Launched RecyclerView
        val tokenLaunchedAdapter = TokenLaunchedAdapter(tokenLaunchedLeaderboard)
        tokenLaunchedRecyclerView.layoutManager = LinearLayoutManager(this)
        tokenLaunchedRecyclerView.adapter = tokenLaunchedAdapter
        
        // SL Staked RecyclerView
        val slStakedAdapter = SLStakedAdapter(slStakedLeaderboard)
        slStakedRecyclerView.layoutManager = LinearLayoutManager(this)
        slStakedRecyclerView.adapter = slStakedAdapter
        
        // Most Likes RecyclerView
        val mostLikesAdapter = CreatorLikesAdapter(creatorMostLikesLeaderboard)
        mostLikesRecyclerView.layoutManager = LinearLayoutManager(this)
        mostLikesRecyclerView.adapter = mostLikesAdapter
        
        // Most Launched RecyclerView
        val mostLaunchedAdapter = CreatorLaunchedAdapter(creatorMostLaunchedLeaderboard)
        mostLaunchedRecyclerView.layoutManager = LinearLayoutManager(this)
        mostLaunchedRecyclerView.adapter = mostLaunchedAdapter
        
        // Set default tab to Token Launched
        showLeaderboardTab("token_launched")
    }
    
    private fun showLeaderboardTab(tabType: String) {
        // Reset all tab styles
        listOf(tabTokenLaunched, tabSLStaked, tabMostLikes, tabMostLaunched).forEach { tab ->
            tab.setTextColor(0xFF666666.toInt())
            tab.setBackgroundColor(0xFFF0F0F0.toInt())
        }
        
        // Hide all RecyclerViews
        tokenLaunchedRecyclerView.visibility = View.GONE
        slStakedRecyclerView.visibility = View.GONE
        mostLikesRecyclerView.visibility = View.GONE
        mostLaunchedRecyclerView.visibility = View.GONE
        
        // Show selected tab and RecyclerView
        when (tabType) {
            "token_launched" -> {
                tabTokenLaunched.setTextColor(0xFF9945FF.toInt())
                tabTokenLaunched.setBackgroundColor(0xFF9945FF20.toInt())
                tokenLaunchedRecyclerView.visibility = View.VISIBLE
            }
            "sl_staked" -> {
                tabSLStaked.setTextColor(0xFF9945FF.toInt())
                tabSLStaked.setBackgroundColor(0xFF9945FF20.toInt())
                slStakedRecyclerView.visibility = View.VISIBLE
            }
            "most_likes" -> {
                tabMostLikes.setTextColor(0xFF9945FF.toInt())
                tabMostLikes.setBackgroundColor(0xFF9945FF20.toInt())
                mostLikesRecyclerView.visibility = View.VISIBLE
            }
            "most_launched" -> {
                tabMostLaunched.setTextColor(0xFF9945FF.toInt())
                tabMostLaunched.setBackgroundColor(0xFF9945FF20.toInt())
                mostLaunchedRecyclerView.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupActivityPage() {
        // Setup activity feed adapter
        activityFeedAdapter = ActivityFeedAdapter(
            activityFeedData,
            onViewTokenClick = { activity ->
                showTokenDetailPopup(activity)
            },
            onLikeClick = { activity ->
                showToast("❤️ Liked ${activity.tokenInfo?.tokenName ?: "activity"}")
            }
        )
        
        activityFeedRecyclerView.layoutManager = LinearLayoutManager(this)
        activityFeedRecyclerView.adapter = activityFeedAdapter
        
        // Setup explore creators button
        exploreCreatorsButton.setOnClickListener {
            showToast("Navigate to creator discovery (placeholder)")
            // In real app, this would navigate to discover tab or creator list
            bottomNavigation.selectedItemId = R.id.nav_discover
        }
        
        // Update activity stats (mock data for demo)
        updateActivityStats()
        
        // Show/hide empty state based on data
        if (activityFeedData.isNotEmpty()) {
            emptyStateLayout.visibility = View.GONE
            activityFeedRecyclerView.visibility = View.VISIBLE
        } else {
            emptyStateLayout.visibility = View.VISIBLE
            activityFeedRecyclerView.visibility = View.GONE
        }
    }
    
    private fun updateActivityStats() {
        // Count likes and presales from activity data (mock implementation)
        val likesCount = activityFeedData.count { it.activityType == ActivityType.LIKE }
        val presalesCount = activityFeedData.count { it.activityType == ActivityType.PRESALE }
        val followingCount = 8 // Mock following count
        
        activityLikesCount.text = likesCount.toString()
        activityPresalesCount.text = presalesCount.toString()
        activityFollowingCount.text = followingCount.toString()
    }
    
    private fun showTokenDetailPopup(activity: ActivityFeedData) {
        if (activity.tokenInfo == null) {
            showToast("No token information available")
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.token_detail_popup, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        // Find views in the popup
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        val tokenName = dialogView.findViewById<TextView>(R.id.tokenName)
        val tokenSymbol = dialogView.findViewById<TextView>(R.id.tokenSymbol)
        val tokenPrice = dialogView.findViewById<TextView>(R.id.tokenPrice)
        val tokenDescription = dialogView.findViewById<TextView>(R.id.tokenDescription)
        val tokenLikes = dialogView.findViewById<TextView>(R.id.tokenLikes)
        val tokenMarketCap = dialogView.findViewById<TextView>(R.id.tokenMarketCap)
        val tokenRank = dialogView.findViewById<TextView>(R.id.tokenRank)
        val creatorName = dialogView.findViewById<TextView>(R.id.creatorName)
        val creatorAvatar = dialogView.findViewById<TextView>(R.id.creatorAvatar)
        val followCreatorButton = dialogView.findViewById<Button>(R.id.followCreatorButton)
        val likeTokenButton = dialogView.findViewById<Button>(R.id.likeTokenButton)
        val buyTokenButton = dialogView.findViewById<Button>(R.id.buyTokenButton)
        
        // Find matching token data from our sample data
        val tokenData = tokens.find { it.name == activity.tokenInfo.tokenName }
        
        // Populate the popup with data
        tokenName.text = activity.tokenInfo.tokenName
        tokenSymbol.text = extractSymbolFromName(activity.tokenInfo.tokenName)
        tokenPrice.text = activity.tokenInfo.tokenPrice
        creatorName.text = activity.tokenInfo.tokenCreator
        creatorAvatar.text = activity.tokenInfo.tokenCreator.take(2).uppercase()
        
        if (tokenData != null) {
            tokenDescription.text = tokenData.description
            tokenLikes.text = formatNumber(tokenData.likes)
            // Calculate mock market cap based on SL tokens staked
            val mockMarketCap = tokenData.slTokenStaked * 50 // Mock calculation
            tokenMarketCap.text = formatNumber(mockMarketCap)
            tokenRank.text = "#${tokenData.tokensLaunched + 3}" // Mock ranking based on tokens launched
        } else {
            // Use mock data for tokens not in our sample
            tokenDescription.text = "Revolutionary token on Solana blockchain with innovative features and strong community support."
            tokenLikes.text = "856"
            tokenMarketCap.text = "1.2M"
            tokenRank.text = "#12"
        }
        
        // Set click listeners
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        
        followCreatorButton.setOnClickListener {
            showToast("Following ${activity.tokenInfo.tokenCreator}")
            followCreatorButton.text = "Following"
            followCreatorButton.isEnabled = false
        }
        
        likeTokenButton.setOnClickListener {
            showToast("❤️ Liked ${activity.tokenInfo.tokenName}")
            likeTokenButton.text = "❤️ Liked"
            likeTokenButton.isEnabled = false
        }
        
        buyTokenButton.setOnClickListener {
            showToast("Buy functionality coming soon!")
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun extractSymbolFromName(tokenName: String): String {
        return when {
            tokenName.contains("MOON") -> "MOON"
            tokenName.contains("DOGE") -> "DOGE"
            tokenName.contains("CAT") -> "CAT"
            tokenName.contains("LAMBO") -> "LAMBO"
            tokenName.contains("DIAMOND") -> "DMD"
            tokenName.contains("ROCKET") -> "ROCKET"
            tokenName.contains("SOLAR") -> "SOLAR"
            tokenName.contains("GALAXY") -> "GLXY"
            else -> tokenName.take(3).uppercase()
        }
    }
    
    private fun formatNumber(number: Int): String {
        return when {
            number >= 1000000 -> String.format("%.1fM", number / 1000000.0)
            number >= 1000 -> String.format("%.1fK", number / 1000.0)
            else -> number.toString()
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}