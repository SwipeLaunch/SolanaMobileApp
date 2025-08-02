package com.anonymous.SolanaMobileApp

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File

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
    private lateinit var myTokensCard: CardView
    private lateinit var myTokensRecyclerView: RecyclerView
    private lateinit var myTokensEmptyState: LinearLayout
    
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
    private lateinit var leaderboardButton: Button
    private lateinit var activityLikesCount: TextView
    private lateinit var activityPresalesCount: TextView
    private lateinit var activityFollowingCount: TextView
    private lateinit var activityFeedAdapter: ActivityFeedAdapter
    
    // Create Token page views
    private lateinit var createTokenPage: View
    private lateinit var pageTitle: TextView
    private lateinit var selectedImage: ImageView
    private lateinit var imagePlaceholder: LinearLayout
    private lateinit var removeImageButton: Button
    private lateinit var takePhotoButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var tokenNameInput: TextInputEditText
    private lateinit var tokenSymbolInput: TextInputEditText
    private lateinit var tokenDescriptionInput: TextInputEditText
    private lateinit var tokenSupplySpinner: Spinner
    private lateinit var tokenChatLinkInput: TextInputEditText
    private lateinit var launchTypeGroup: RadioGroup
    private lateinit var instantLaunchCard: CardView
    private lateinit var proposeTokenCard: CardView
    private lateinit var presaleOptionsLayout: LinearLayout
    private lateinit var createTokenButton: Button
    private lateinit var costInfoTitle: TextView
    private lateinit var costInfoDescription: TextView
    private lateinit var costInfoLayout: LinearLayout
    
    // Image handling
    private var currentImageUri: Uri? = null
    private var tempImageUri: Uri? = null
    
    // Created tokens storage (in real app, this would be from database/blockchain)
    private val createdTokens = mutableListOf<CreatedTokenInfo>()
    
    // Activity result launchers
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            currentImageUri = tempImageUri
            displaySelectedImage(currentImageUri!!)
        }
    }
    
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            currentImageUri = it
            displaySelectedImage(it)
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            showToast("Permission granted")
        } else {
            showToast("Permission required for camera access")
        }
    }
    
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
        myTokensCard = profilePage.findViewById(R.id.myTokensCard)
        myTokensRecyclerView = profilePage.findViewById(R.id.myTokensRecyclerView)
        myTokensEmptyState = profilePage.findViewById(R.id.myTokensEmptyState)
        
        // Initialize presale page views
        presaleRecyclerView = presalePage.findViewById(R.id.presaleRecyclerView)
        walletBalanceText = presalePage.findViewById(R.id.walletBalanceText)
        
        // Setup presale RecyclerView
        presaleAdapter = PresaleAdapter(presaleTokens) { token -> 
            showBuyDialog(token)
        }
        presaleRecyclerView.layoutManager = LinearLayoutManager(this)
        presaleRecyclerView.adapter = presaleAdapter
        
        // Add one sample token for testing
        if (createdTokens.isEmpty()) {
            createdTokens.add(
                CreatedTokenInfo(
                    name = "Moon Token",
                    symbol = "MOON",
                    description = "To the moon!",
                    supply = 1_000_000_000L,
                    launchType = LaunchType.PRESALE,
                    tokenAddress = "SampleAddr123",
                    chatLink = "https://t.me/moontoken",
                    status = "Voting"
                )
            )
        }
        
        // Setup My Tokens section
        setupMyTokens()
        
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
        leaderboardButton = activityPage.findViewById(R.id.leaderboardButton)
        activityLikesCount = activityPage.findViewById(R.id.activityLikesCount)
        activityPresalesCount = activityPage.findViewById(R.id.activityPresalesCount)
        activityFollowingCount = activityPage.findViewById(R.id.activityFollowingCount)
        
        // Initialize create token page views
        createTokenPage = findViewById(R.id.createTokenPage)
        pageTitle = createTokenPage.findViewById(R.id.pageTitle)
        selectedImage = createTokenPage.findViewById(R.id.selectedImage)
        imagePlaceholder = createTokenPage.findViewById(R.id.imagePlaceholder)
        removeImageButton = createTokenPage.findViewById(R.id.removeImageButton)
        takePhotoButton = createTokenPage.findViewById(R.id.takePhotoButton)
        selectImageButton = createTokenPage.findViewById(R.id.selectImageButton)
        tokenNameInput = createTokenPage.findViewById(R.id.tokenNameInput)
        tokenSymbolInput = createTokenPage.findViewById(R.id.tokenSymbolInput)
        tokenDescriptionInput = createTokenPage.findViewById(R.id.tokenDescriptionInput)
        tokenSupplySpinner = createTokenPage.findViewById(R.id.tokenSupplySpinner)
        tokenChatLinkInput = createTokenPage.findViewById(R.id.tokenChatLinkInput)
        launchTypeGroup = createTokenPage.findViewById(R.id.launchTypeGroup)
        instantLaunchCard = createTokenPage.findViewById(R.id.instantLaunchCard)
        proposeTokenCard = createTokenPage.findViewById(R.id.proposeTokenCard)
        presaleOptionsLayout = createTokenPage.findViewById(R.id.presaleOptionsLayout)
        createTokenButton = createTokenPage.findViewById(R.id.createTokenButton)
        costInfoTitle = createTokenPage.findViewById(R.id.costInfoTitle)
        costInfoDescription = createTokenPage.findViewById(R.id.costInfoDescription)
        costInfoLayout = createTokenPage.findViewById(R.id.costInfoLayout)
        
        setupLeaderboardTabs()
        setupLeaderboardRecyclerViews()
        setupActivityPage()
        setupCreateTokenPage()
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
                R.id.nav_create -> {
                    showCreateTokenTab()
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
        createTokenPage.visibility = View.GONE
        activityPage.visibility = View.GONE
    }
    
    private fun showProfileTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.VISIBLE
        presalePage.visibility = View.GONE
        leaderboardPage.visibility = View.GONE
        createTokenPage.visibility = View.GONE
        activityPage.visibility = View.GONE
    }
    
    private fun showPresaleTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.GONE
        presalePage.visibility = View.VISIBLE
        leaderboardPage.visibility = View.GONE
        createTokenPage.visibility = View.GONE
        activityPage.visibility = View.GONE
        updateWalletBalance()
    }
    
    private fun showCreateTokenTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.GONE
        presalePage.visibility = View.GONE
        leaderboardPage.visibility = View.GONE
        createTokenPage.visibility = View.VISIBLE
        activityPage.visibility = View.GONE
    }
    
    private fun showActivityTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.GONE
        presalePage.visibility = View.GONE
        leaderboardPage.visibility = View.GONE
        createTokenPage.visibility = View.GONE
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
            
            // Show My Tokens section when connected
            myTokensCard.visibility = View.VISIBLE
            updateMyTokensDisplay()
        } else {
            // Wallet disconnected - hide profile details
            profileWalletButton.text = "Connect Solana Wallet"
            walletStatusText.text = "Connect your wallet to see your profile details"
            profileDetailsCard.visibility = View.GONE
            activityCard.visibility = View.GONE
            myTokensCard.visibility = View.GONE
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
        createTokenPage.visibility = View.GONE
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
            tab.setBackgroundResource(R.drawable.tab_background_unselected)
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
                tabTokenLaunched.setBackgroundResource(R.drawable.tab_background_selected)
                tokenLaunchedRecyclerView.visibility = View.VISIBLE
            }
            "sl_staked" -> {
                tabSLStaked.setTextColor(0xFF9945FF.toInt())
                tabSLStaked.setBackgroundResource(R.drawable.tab_background_selected)
                slStakedRecyclerView.visibility = View.VISIBLE
            }
            "most_likes" -> {
                tabMostLikes.setTextColor(0xFF9945FF.toInt())
                tabMostLikes.setBackgroundResource(R.drawable.tab_background_selected)
                mostLikesRecyclerView.visibility = View.VISIBLE
            }
            "most_launched" -> {
                tabMostLaunched.setTextColor(0xFF9945FF.toInt())
                tabMostLaunched.setBackgroundResource(R.drawable.tab_background_selected)
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
        
        // Setup leaderboard button
        leaderboardButton.setOnClickListener {
            showLeaderboardOverlay()
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
    
    private fun setupCreateTokenPage() {
        // Setup photo capture buttons
        takePhotoButton.setOnClickListener {
            if (checkCameraPermission()) {
                takePicture()
            } else {
                requestCameraPermission()
            }
        }
        
        selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }
        
        removeImageButton.setOnClickListener {
            removeSelectedImage()
        }
        
        // Setup launch type radio group
        launchTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.instantLaunchRadio -> {
                    updateUIForInstantLaunch()
                }
                R.id.presaleLaunchRadio -> {
                    updateUIForCommunityPresale()
                }
            }
        }
        
        // Setup card click listeners to select radio buttons
        instantLaunchCard.setOnClickListener {
            launchTypeGroup.check(R.id.instantLaunchRadio)
        }
        
        proposeTokenCard.setOnClickListener {
            launchTypeGroup.check(R.id.presaleLaunchRadio)
        }
        
        // Setup supply spinner
        setupSupplySpinner()
        
        // Initialize with propose token selected (default)
        updateUIForCommunityPresale()
        
        // Setup create token button
        createTokenButton.setOnClickListener {
            createToken()
        }
    }
    
    private fun setupSupplySpinner() {
        val supplyOptions = arrayOf(
            "1 Billion (1,000,000,000)",
            "500 Million (500,000,000)",
            "100 Million (100,000,000)",
            "10 Million (10,000,000)",
            "1 Million (1,000,000)"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, supplyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tokenSupplySpinner.adapter = adapter
        
        // Set default to 1 Billion (index 0)
        tokenSupplySpinner.setSelection(0)
    }
    
    private fun updateUIForInstantLaunch() {
        pageTitle.text = "🚀 Create Token"
        createTokenButton.text = "🚀 Launch Token"
        presaleOptionsLayout.visibility = View.GONE
        
        // Update cost information
        costInfoTitle.text = "Instant Launch"
        costInfoDescription.text = "≈ 0.1 SOL + fees"
        costInfoLayout.setBackgroundColor(0xFFFFF3CD.toInt())
        
        // Update card selection states
        updateCardSelectionStates(true)
    }
    
    private fun updateUIForCommunityPresale() {
        pageTitle.text = "🗳️ Propose a Token"
        createTokenButton.text = "📝 Submit Proposal"
        presaleOptionsLayout.visibility = View.VISIBLE
        
        // Update cost information
        costInfoTitle.text = "Proposal"
        costInfoDescription.text = "Free • Needs 80 votes"
        costInfoLayout.setBackgroundColor(0xFFEBF8FF.toInt())
        
        // Update card selection states
        updateCardSelectionStates(false)
    }
    
    private fun updateCardSelectionStates(instantLaunchSelected: Boolean) {
        if (instantLaunchSelected) {
            // Instant Launch selected - show it prominently
            instantLaunchCard.visibility = View.VISIBLE
            instantLaunchCard.setCardBackgroundColor(0xFFEDE9FE.toInt()) // Light purple background
            instantLaunchCard.cardElevation = 8f // Higher elevation for selection
            instantLaunchCard.alpha = 1.0f // Fully visible
            
            // Propose Token unselected - gray it out and make it smaller
            proposeTokenCard.visibility = View.VISIBLE
            proposeTokenCard.setCardBackgroundColor(0xFFF3F4F6.toInt()) // Light gray background
            proposeTokenCard.cardElevation = 1f // Lower elevation
            proposeTokenCard.alpha = 0.6f // Faded
        } else {
            // Propose Token selected - show it prominently
            proposeTokenCard.visibility = View.VISIBLE
            proposeTokenCard.setCardBackgroundColor(0xFFEDE9FE.toInt()) // Light purple background
            proposeTokenCard.cardElevation = 8f // Higher elevation for selection
            proposeTokenCard.alpha = 1.0f // Fully visible
            
            // Instant Launch unselected - gray it out and make it smaller
            instantLaunchCard.visibility = View.VISIBLE
            instantLaunchCard.setCardBackgroundColor(0xFFF3F4F6.toInt()) // Light gray background
            instantLaunchCard.cardElevation = 1f // Lower elevation
            instantLaunchCard.alpha = 0.6f // Faded
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    private fun takePicture() {
        val imageFile = File(getExternalFilesDir(null), "token_image_${System.currentTimeMillis()}.jpg")
        tempImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
        takePictureLauncher.launch(tempImageUri)
    }
    
    private fun selectImageFromGallery() {
        selectImageLauncher.launch("image/*")
    }
    
    private fun displaySelectedImage(uri: Uri) {
        selectedImage.setImageURI(uri)
        selectedImage.visibility = View.VISIBLE
        imagePlaceholder.visibility = View.GONE
        removeImageButton.visibility = View.VISIBLE
    }
    
    private fun removeSelectedImage() {
        currentImageUri = null
        selectedImage.visibility = View.GONE
        imagePlaceholder.visibility = View.VISIBLE
        removeImageButton.visibility = View.GONE
        selectedImage.setImageURI(null)
    }
    
    private fun createToken() {
        // Validate inputs
        val name = tokenNameInput.text?.toString()?.trim()
        val symbol = tokenSymbolInput.text?.toString()?.trim()?.uppercase()
        val description = tokenDescriptionInput.text?.toString()?.trim()
        val chatLink = tokenChatLinkInput.text?.toString()?.trim()
        if (name.isNullOrEmpty()) {
            showToast("Please enter token name")
            return
        }
        
        if (symbol.isNullOrEmpty()) {
            showToast("Please enter token symbol")
            return
        }
        
        if (description.isNullOrEmpty()) {
            showToast("Please enter token description")
            return
        }
        
        // Get supply from spinner selection
        val supply = when (tokenSupplySpinner.selectedItemPosition) {
            0 -> 1_000_000_000L // 1 Billion
            1 -> 500_000_000L   // 500 Million
            2 -> 100_000_000L   // 100 Million
            3 -> 10_000_000L    // 10 Million
            4 -> 1_000_000L     // 1 Million
            else -> 1_000_000_000L // Default to 1 Billion
        }
        
        // Check wallet connection
        if (!walletManager.isWalletConnected()) {
            showToast("Please connect your wallet first")
            return
        }
        
        // Get launch type
        val launchType = when (launchTypeGroup.checkedRadioButtonId) {
            R.id.presaleLaunchRadio -> LaunchType.PRESALE
            else -> LaunchType.INSTANT
        }
        
        // Create token data
        val tokenData = CreateTokenData(
            name = name,
            symbol = symbol,
            description = description,
            initialSupply = supply,
            imageUri = currentImageUri,
            launchType = launchType,
            presaleDurationHours = if (launchType == LaunchType.PRESALE) 24 else null, // Fixed 24-hour duration
            chatLink = if (chatLink.isNullOrEmpty()) null else chatLink
        )
        
        // Show creation in progress
        createTokenButton.isEnabled = false
        createTokenButton.text = "Creating Token..."
        
        // Simulate token creation (in real app, this would call Solana Mobile Stack APIs)
        lifecycleScope.launch {
            kotlinx.coroutines.delay(3000) // Simulate network delay
            
            // Mock successful creation
            val result = TokenCreationResult(
                success = true,
                tokenAddress = "TokenAddr${System.currentTimeMillis()}",
                transactionSignature = "TxSig${System.currentTimeMillis()}"
            )
            
            handleTokenCreationResult(result)
        }
    }
    
    private fun handleTokenCreationResult(result: TokenCreationResult) {
        createTokenButton.isEnabled = true
        createTokenButton.text = "🚀 Create Token"
        
        if (result.success) {
            showToast("✅ Token created successfully!")
            
            // Store the created token (get data from the last form submission)
            val name = tokenNameInput.text?.toString()?.trim() ?: ""
            val symbol = tokenSymbolInput.text?.toString()?.trim()?.uppercase() ?: ""
            val description = tokenDescriptionInput.text?.toString()?.trim() ?: ""
            val chatLink = tokenChatLinkInput.text?.toString()?.trim()
            val supply = when (tokenSupplySpinner.selectedItemPosition) {
                0 -> 1_000_000_000L
                1 -> 500_000_000L
                2 -> 100_000_000L
                3 -> 10_000_000L
                4 -> 1_000_000L
                else -> 1_000_000_000L
            }
            val launchType = when (launchTypeGroup.checkedRadioButtonId) {
                R.id.presaleLaunchRadio -> LaunchType.PRESALE
                else -> LaunchType.INSTANT
            }
            
            val createdToken = CreatedTokenInfo(
                name = name,
                symbol = symbol,
                description = description,
                supply = supply,
                launchType = launchType,
                tokenAddress = result.tokenAddress ?: "Unknown",
                chatLink = if (chatLink.isNullOrEmpty()) null else chatLink,
                status = if (launchType == LaunchType.PRESALE) "Voting" else "Active"
            )
            
            createdTokens.add(createdToken)
            
            // Debug log
            android.util.Log.d("MyTokens", "Token added! Total tokens: ${createdTokens.size}")
            
            // Update My Tokens display
            updateMyTokensDisplay()
            
            // Show success dialog
            AlertDialog.Builder(this)
                .setTitle("🎉 Token Created!")
                .setMessage("Your token has been created successfully!\n\nToken Address: ${result.tokenAddress}\nTransaction: ${result.transactionSignature}")
                .setPositiveButton("View in Profile") { _, _ ->
                    // Navigate to profile tab to see the token
                    bottomNavigation.selectedItemId = R.id.nav_profile
                }
                .setNegativeButton("Create Another", null)
                .show()
                
            // Reset form
            resetCreateTokenForm()
        } else {
            showToast("❌ Failed to create token: ${result.errorMessage}")
        }
    }
    
    private fun resetCreateTokenForm() {
        tokenNameInput.text?.clear()
        tokenSymbolInput.text?.clear()
        tokenDescriptionInput.text?.clear()
        tokenChatLinkInput.text?.clear()
        tokenSupplySpinner.setSelection(0) // Reset to 1 Billion
        removeSelectedImage()
        launchTypeGroup.check(R.id.presaleLaunchRadio) // Default to Propose Token
        updateUIForCommunityPresale() // Update UI to show proposal info
    }
    
    private fun showLeaderboardOverlay() {
        // Simple approach: temporarily show leaderboard page
        showLeaderboardTab()
        showToast("🏆 Leaderboard opened! Use back button to return to Activities")
        
        // Show close button
        val closeButton = leaderboardPage.findViewById<Button>(R.id.closeLeaderboardButton)
        closeButton.visibility = View.VISIBLE
        closeButton.setOnClickListener {
            // Return to Activities tab
            showActivityTab()
            closeButton.visibility = View.GONE
        }
    }
    
    private fun setupMyTokens() {
        myTokensRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // For debugging, make My Tokens always visible
        myTokensCard.visibility = View.VISIBLE
        
        updateMyTokensDisplay()
    }
    
    private fun updateMyTokensDisplay() {
        val emptyState = profilePage.findViewById<LinearLayout>(R.id.myTokensEmptyState)
        
        // Debug log
        android.util.Log.d("MyTokens", "Updating display. Token count: ${createdTokens.size}")
        createdTokens.forEachIndexed { index, token ->
            android.util.Log.d("MyTokens", "Token $index: ${token.name} (${token.symbol})")
        }
        
        if (createdTokens.isEmpty()) {
            myTokensRecyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            myTokensRecyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            
            val adapter = MyTokensAdapter(createdTokens) { token ->
                // Handle token view click - show detailed token info
                showTokenDetails(token)
            }
            myTokensRecyclerView.adapter = adapter
        }
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
        
        AlertDialog.Builder(this)
            .setTitle("${token.name} (${token.symbol})")
            .setMessage(
                "Description: ${token.description}\n" +
                "Launch Type: $launchTypeText\n" +
                "Supply: ${formatSupplyForDisplay(token.supply)}\n" +
                "Status: ${token.status}\n" +
                "Token Address: ${token.tokenAddress}" +
                chatLinkText
            )
            .setPositiveButton("Copy Address") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Token Address", token.tokenAddress)
                clipboard.setPrimaryClip(clip)
                showToast("Token address copied to clipboard")
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun formatSupplyForDisplay(supply: Long): String {
        return when {
            supply >= 1_000_000_000 -> "${supply / 1_000_000_000}B"
            supply >= 1_000_000 -> "${supply / 1_000_000}M"
            supply >= 1_000 -> "${supply / 1_000}K"
            else -> supply.toString()
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}