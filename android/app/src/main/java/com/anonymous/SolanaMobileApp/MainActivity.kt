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
    private lateinit var databaseService: DatabaseService
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
    private lateinit var profileLikesCount: TextView
    private lateinit var profilePresalesCount: TextView
    private lateinit var profileFollowingCount: TextView
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
    
    // Real token data from database - will be loaded dynamically
    private var tokens = mutableListOf<TokenData>()
    
    // Real presale data from database - will be loaded dynamically
    private var presaleTokens = mutableListOf<PresaleTokenData>()
    
    // Real leaderboard data from database - will be loaded dynamically
    private var tokenLaunchedLeaderboard = mutableListOf<TokenLaunchedData>()
    private var slStakedLeaderboard = mutableListOf<SLTokenStakedData>()
    private var creatorMostLikesLeaderboard = mutableListOf<CreatorMostLikesData>()
    private var creatorMostLaunchedLeaderboard = mutableListOf<CreatorMostLaunchedData>()
    
    // Activity feed data - loaded from database
    private var activityFeedData = mutableListOf<ActivityFeedData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupDatabase()
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
        profileLikesCount = profilePage.findViewById(R.id.profileLikesCount)
        profilePresalesCount = profilePage.findViewById(R.id.profilePresalesCount)
        profileFollowingCount = profilePage.findViewById(R.id.profileFollowingCount)
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
    
    private fun setupDatabase() {
        databaseService = DatabaseService()
        
        // Test database connection with detailed logging
        lifecycleScope.launch {
            android.util.Log.d("Database", "Starting database connection test...")
            
            val isConnected = databaseService.testConnection()
            if (isConnected) {
                android.util.Log.d("Database", "✅ Connection successful!")
                showToast("✅ Database connected!")
                
                // Test reading from all tables
                testDatabaseReads()
            } else {
                android.util.Log.e("Database", "❌ Connection failed!")
                showToast("⚠️ Using offline mode - database connection failed")
            }
        }
    }
    
    private suspend fun testDatabaseReads() {
        android.util.Log.d("Database", "Testing database read operations...")
        
        // Test tokens table with raw data first
        try {
            val rawResult = databaseService.getRawTokenData()
            android.util.Log.d("Database", "📊 Raw tokens result: $rawResult")
            
            val tokens = databaseService.getAllTokens()
            android.util.Log.d("Database", "📊 Tokens table: Found ${tokens.size} records")
            if (tokens.isNotEmpty()) {
                tokens.take(3).forEach { token ->
                    android.util.Log.d("Database", "  Token: ${token.token_name} (${token.symbol}) by ${token.creator_wallet} - Votes: ${token.vote_count}")
                }
            }
            showToast("📊 Found ${tokens.size} tokens in database")
        } catch (e: Exception) {
            android.util.Log.e("Database", "❌ Error reading tokens: ${e.message}")
        }
        
        // Test users table
        try {
            val users = databaseService.getUsers()
            android.util.Log.d("Database", "👥 Users table: Found ${users.size} records")
            if (users.isNotEmpty()) {
                users.take(3).forEach { user ->
                    android.util.Log.d("Database", "  User: ${user.wallet_address} (${user.twitter_handle ?: "no twitter"})")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Database", "❌ Error reading users: ${e.message}")
        }
        
        // Test user_votes table
        try {
            val votes = databaseService.getUserVotes()
            android.util.Log.d("Database", "🗳️ User votes table: Found ${votes.size} records")
            if (votes.isNotEmpty()) {
                votes.take(3).forEach { vote ->
                    android.util.Log.d("Database", "  Vote: User ${vote.user_wallet} voted for token ${vote.token_id}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Database", "❌ Error reading votes: ${e.message}")
        }
        
        // Test presale_participants table
        try {
            val participants = databaseService.getPresaleParticipants()
            android.util.Log.d("Database", "🚀 Presale participants: Found ${participants.size} records")
            if (participants.isNotEmpty()) {
                participants.take(3).forEach { participant ->
                    android.util.Log.d("Database", "  Participant: ${participant.user_wallet} contributed ${participant.sol_contributed} SOL to token ${participant.token_id}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Database", "❌ Error reading participants: ${e.message}")
        }
        
        android.util.Log.d("Database", "Database read tests completed!")
        
        // Now load the real data into the UI
        loadTokensFromDatabase()
        loadPresaleDataFromDatabase()
        loadLeaderboardData()
    }
    
    private suspend fun loadTokensFromDatabase() {
        try {
            // DISCOVER PAGE: Always show ALL active tokens for discovery
            val allDbTokens = databaseService.getAllTokens()
            val activeTokens = allDbTokens.filter { it.status == "active" }
            
            if (activeTokens.isNotEmpty()) {
                android.util.Log.d("MainActivity", "Loading ${activeTokens.size} active tokens for discovery")
                
                tokens.clear()
                tokens.addAll(activeTokens.map { dbToken ->
                    TokenData(
                        id = dbToken.token_id?.toString() ?: "0",
                        name = dbToken.token_name,
                        creator = extractUsernameFromWallet(dbToken.creator_wallet),
                        description = dbToken.description ?: "No description available",
                        likes = dbToken.vote_count,
                        creatorWallet = dbToken.creator_wallet,
                        creatorTwitter = extractUsernameFromWallet(dbToken.creator_wallet),
                        creatorSolanaHandle = "sl_${dbToken.symbol.lowercase()}",
                        slTokenStaked = (dbToken.sol_raised * 1000).toInt(),
                        totalLikesReceived = dbToken.vote_count,
                        tokensLaunched = 1,
                        communityLink = "https://discord.gg/${dbToken.symbol.lowercase()}"
                    )
                })
                
                android.util.Log.d("MainActivity", "Successfully loaded ${tokens.size} active tokens for discovery")
                showToast("🔍 Discover ${tokens.size} active tokens!")
                refreshTokenCards()
                
            } else {
                android.util.Log.d("MainActivity", "No active tokens found, using fallback")
                loadFallbackTokens()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading tokens: ${e.message}")
            loadFallbackTokens()
        }
    }
    
    private fun extractUsernameFromWallet(walletAddress: String): String {
        // Extract a readable username from wallet address
        return if (walletAddress.length >= 8) {
            "user_${walletAddress.take(4)}${walletAddress.takeLast(4)}"
        } else {
            "user_${walletAddress}"
        }
    }
    
    private fun loadFallbackTokens() {
        // Fallback tokens if database is empty
        tokens.clear()
        tokens.addAll(listOf(
            TokenData("1", "MOON Token", "cryptoking", "🚀 Sample token for testing", 1247,
                     "ABC123...XYZ789", "cryptoking", "sl_moon", 2500, 1247, 8, "https://discord.gg/moontoken")
        ))
        refreshTokenCards()
    }
    
    private fun refreshTokenCards() {
        // Reset and recreate the token cards with new data
        currentCardIndex = 0
        setupSwipeCards()
    }
    
    private suspend fun loadPresaleDataFromDatabase() {
        try {
            // PRESALE PAGE: Always show ALL presale tokens
            val dbTokens = databaseService.getAllTokens()
            val presaleDbTokens = dbTokens.filter { it.status == "presale" }
            
            if (presaleDbTokens.isNotEmpty()) {
                android.util.Log.d("MainActivity", "Loading ${presaleDbTokens.size} presale tokens")
                
                presaleTokens.clear()
                presaleTokens.addAll(presaleDbTokens.map { dbToken ->
                    val currentTime = System.currentTimeMillis()
                    
                    PresaleTokenData(
                        id = "ps${dbToken.token_id}",
                        name = dbToken.token_name,
                        symbol = dbToken.symbol,
                        description = dbToken.description ?: "Presale token - no description available",
                        totalSupply = 1_000_000_000L, // Default supply
                        raisedSol = dbToken.sol_raised, // Current SOL raised
                        startTime = currentTime,
                        endTime = currentTime + (7 * 24 * 60 * 60 * 1000L), // 7 days from now
                        creatorAddress = dbToken.creator_wallet,
                        tokenAddress = dbToken.token_mint_address ?: dbToken.creator_wallet
                    )
                })
                
                android.util.Log.d("MainActivity", "Successfully loaded ${presaleTokens.size} presale tokens")
                showToast("🚀 ${presaleTokens.size} active presales available!")
                
                // Update the presale adapter
                refreshPresaleAdapter()
                
            } else {
                android.util.Log.d("MainActivity", "No presale tokens found, loading fallback")
                loadFallbackPresales()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading presale data: ${e.message}")
            loadFallbackPresales()
        }
    }
    
    private fun loadFallbackPresales() {
        presaleTokens.clear()
        presaleTokens.add(
            PresaleTokenData("ps1", "Sample Presale", "SAMPLE", "🚀 No presale tokens found in database", 
                            1_000_000_000L, 25.0, System.currentTimeMillis(), 
                            System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L), 
                            "SampleCreator", "SampleAddr")
        )
        refreshPresaleAdapter()
    }
    
    private fun refreshPresaleAdapter() {
        // Update the presale RecyclerView adapter with new data
        runOnUiThread {
            presaleAdapter = PresaleAdapter(presaleTokens) { token -> 
                showBuyDialog(token)
            }
            presaleRecyclerView.adapter = presaleAdapter
        }
    }
    
    private suspend fun loadLeaderboardData() {
        try {
            val allDbTokens = databaseService.getAllTokens()
            android.util.Log.d("MainActivity", "Loading leaderboard data from ${allDbTokens.size} tokens")
            
            // 1. Token Launched Leaderboard (launched tokens sorted by SOL raised)
            val launchedTokens = allDbTokens.filter { it.status == "launched" }
                .sortedByDescending { it.sol_raised }
                .take(5)
            
            tokenLaunchedLeaderboard.clear()
            tokenLaunchedLeaderboard.addAll(launchedTokens.mapIndexed { index, token ->
                TokenLaunchedData(
                    rank = index + 1,
                    tokenName = token.token_name,
                    tokenSymbol = token.symbol,
                    marketCap = token.sol_raised, // SOL raised
                    marketCapUSD = token.sol_raised * 50, // Mock USD conversion
                    creator = extractUsernameFromWallet(token.creator_wallet),
                    launchDate = System.currentTimeMillis()
                )
            })
            
            // 2. Most Likes Leaderboard (creators sorted by vote count)
            val creatorVotes = allDbTokens.groupBy { it.creator_wallet }
                .map { (wallet, tokens) ->
                    val totalVotes = tokens.sumOf { it.vote_count }
                    val tokenCount = tokens.size
                    val avgLikes = if (tokenCount > 0) totalVotes.toDouble() / tokenCount else 0.0
                    val bestToken = tokens.maxByOrNull { it.vote_count }
                    
                    CreatorMostLikesData(
                        rank = 0, // Will be set after sorting
                        creatorName = extractUsernameFromWallet(wallet),
                        twitterHandle = null, // No twitter data available
                        totalLikes = totalVotes,
                        totalTokensCreated = tokenCount,
                        averageLikesPerToken = avgLikes,
                        topTokenName = bestToken?.token_name ?: "",
                        topTokenLikes = bestToken?.vote_count ?: 0
                    )
                }
                .sortedByDescending { it.totalLikes }
                .take(5)
            
            creatorMostLikesLeaderboard.clear()
            creatorMostLikesLeaderboard.addAll(creatorVotes.mapIndexed { index, creator ->
                creator.copy(rank = index + 1)
            })
            
            // 3. Most Launched Leaderboard (creators by token count)
            val creatorLaunched = allDbTokens.groupBy { it.creator_wallet }
                .map { (wallet, tokens) ->
                    val totalMarketCap = tokens.sumOf { it.sol_raised * 50_000 }
                    val successRate = tokens.count { it.status == "launched" }.toDouble() / tokens.size
                    val bestToken = tokens.maxByOrNull { it.sol_raised }
                    
                    CreatorMostLaunchedData(
                        rank = 0, // Will be set after sorting
                        creatorName = extractUsernameFromWallet(wallet),
                        twitterHandle = null, // No twitter data available
                        totalTokensLaunched = tokens.size,
                        totalMarketCap = totalMarketCap,
                        successRate = successRate,
                        bestPerformingToken = bestToken?.token_name ?: "",
                        bestTokenMarketCap = bestToken?.sol_raised?.times(50_000) ?: 0.0
                    )
                }
                .sortedByDescending { it.totalTokensLaunched }
                .take(5)
            
            creatorMostLaunchedLeaderboard.clear()
            creatorMostLaunchedLeaderboard.addAll(creatorLaunched.mapIndexed { index, creator ->
                creator.copy(rank = index + 1)
            })
            
            // 4. SL Token Balance Leaderboard - Show users with their SL balance and voting rights
            val users = databaseService.getUsers()
            slStakedLeaderboard.clear()
            slStakedLeaderboard.addAll(users.sortedByDescending { it.sl_token_balance }.take(10).mapIndexed { index, user ->
                SLTokenStakedData(
                    rank = index + 1,
                    walletAddress = user.wallet_address,
                    twitterHandle = user.twitter_handle,
                    solanaDomain = user.solana_name,
                    slTokenBalance = user.sl_token_balance,
                    dailyVotingRightsRemaining = user.daily_voting_rights_remaining,
                    dailyVotingRightsTotal = user.daily_voting_rights_total
                )
            })
            
            android.util.Log.d("MainActivity", "Loaded leaderboards: ${tokenLaunchedLeaderboard.size} launched, ${creatorMostLikesLeaderboard.size} likes, ${creatorMostLaunchedLeaderboard.size} most launched")
            showToast("🏆 Loaded real leaderboard data!")
            
            // Refresh the leaderboard RecyclerViews
            refreshLeaderboardAdapters()
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading leaderboard data: ${e.message}")
            loadFallbackLeaderboards()
        }
    }
    
    private fun loadFallbackLeaderboards() {
        // Add minimal fallback data if database loading fails
        tokenLaunchedLeaderboard.clear()
        creatorMostLikesLeaderboard.clear()
        creatorMostLaunchedLeaderboard.clear()
        slStakedLeaderboard.clear()
        
        android.util.Log.d("MainActivity", "Using fallback leaderboard data")
    }
    
    private fun refreshLeaderboardAdapters() {
        runOnUiThread {
            // Update all leaderboard adapters with new data
            setupLeaderboardRecyclerViews()
        }
    }
    
    private fun setupWalletManager() {
        walletManager = WalletManager(this)
        
        walletManager.setCallback(object : WalletManager.WalletCallback {
            override fun onWalletConnected(publicKey: String) {
                val displayKey = walletManager.formatPublicKeyForDisplay(publicKey)
                updateProfileUI(true, publicKey, displayKey)
                showToast("Wallet connected: $displayKey")
                
                // Update Profile section with wallet-specific data
                updateMyTokensDisplay()
            }
            
            override fun onWalletDisconnected() {
                updateProfileUI(false, null, null)
                showToast("Wallet disconnected")
                
                // Clear Profile section wallet-specific data
                updateMyTokensDisplay()
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
        
        // Initialize My Tokens display now that wallet manager is ready
        updateMyTokensDisplay()
        
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
            
            // Update activity summary with real data
            updateActivitySummary(fullAddress)
            
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
        
        // Load real activity data from database
        loadActivityData()
    }
    
    private fun updateActivityStats() {
        // Count likes and presales from activity data (mock implementation)
        val likesCount = activityFeedData.count { it.activityType == ActivityType.LIKE }
        val presalesCount = activityFeedData.count { it.activityType == ActivityType.PRESALE }
        val followingCount = 8 // Mock following count
        
        // Activity summary has been moved to Profile page
        // Update profile activity summary instead if needed
    }
    
    private fun loadActivityData() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("MainActivity", "Loading activity data from database...")
                
                // Clear existing data
                activityFeedData.clear()
                
                // Load tokens to get token information
                val allTokens = databaseService.getAllTokens()
                android.util.Log.d("MainActivity", "Loaded ${allTokens.size} tokens")
                
                // Load user votes to create like activities
                val votes = databaseService.getUserVotes()
                android.util.Log.d("MainActivity", "Loaded ${votes.size} votes")
                
                // Load presale participants to create presale activities
                val participants = databaseService.getPresaleParticipants()
                android.util.Log.d("MainActivity", "Loaded ${participants.size} presale participants")
                
                // Convert votes to activity data
                votes.forEach { vote ->
                    val token = allTokens.find { it.token_id == vote.token_id }
                    if (token != null) {
                        val activityData = ActivityFeedData(
                            id = "vote_${vote.user_wallet}_${vote.token_id}",
                            userId = vote.user_wallet,
                            userName = formatWalletForDisplay(vote.user_wallet),
                            userAvatar = formatWalletForDisplay(vote.user_wallet).take(2).uppercase(),
                            activityType = ActivityType.LIKE,
                            description = "liked ${token.token_name}",
                            timestamp = System.currentTimeMillis() - (Math.random() * 7200000).toLong(), // Random time within last 2 hours
                            tokenInfo = TokenActivityInfo(
                                tokenName = token.token_name,
                                tokenCreator = formatWalletForDisplay(token.creator_wallet),
                                tokenPrice = "${token.launch_price_sol ?: 0.05} SOL"
                            )
                        )
                        activityFeedData.add(activityData)
                    }
                }
                
                // Convert presale participants to activity data
                participants.forEach { participant ->
                    val token = allTokens.find { it.token_id == participant.token_id }
                    if (token != null) {
                        val activityData = ActivityFeedData(
                            id = "presale_${participant.user_wallet}_${participant.token_id}",
                            userId = participant.user_wallet,
                            userName = formatWalletForDisplay(participant.user_wallet),
                            userAvatar = formatWalletForDisplay(participant.user_wallet).take(2).uppercase(),
                            activityType = ActivityType.PRESALE,
                            description = "joined ${token.token_name} presale",
                            timestamp = System.currentTimeMillis() - (Math.random() * 7200000).toLong(), // Random time within last 2 hours
                            tokenInfo = TokenActivityInfo(
                                tokenName = token.token_name,
                                tokenCreator = formatWalletForDisplay(token.creator_wallet),
                                tokenPrice = "${token.launch_price_sol ?: 0.05} SOL"
                            )
                        )
                        activityFeedData.add(activityData)
                    }
                }
                
                // Sort by timestamp (most recent first)
                activityFeedData.sortByDescending { it.timestamp }
                
                android.util.Log.d("MainActivity", "Created ${activityFeedData.size} activity items")
                
                // Update UI on main thread
                runOnUiThread {
                    activityFeedAdapter.notifyDataSetChanged()
                    
                    // Update Following count in profile to show activity count
                    updateFollowingCountFromActivities()
                    
                    // Show/hide empty state based on data
                    if (activityFeedData.isNotEmpty()) {
                        emptyStateLayout.visibility = View.GONE
                        activityFeedRecyclerView.visibility = View.VISIBLE
                    } else {
                        emptyStateLayout.visibility = View.VISIBLE
                        activityFeedRecyclerView.visibility = View.GONE
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading activity data: ${e.message}")
                android.util.Log.e("MainActivity", "Error details: ${e.stackTraceToString()}")
                
                runOnUiThread {
                    // Show empty state on error
                    emptyStateLayout.visibility = View.VISIBLE
                    activityFeedRecyclerView.visibility = View.GONE
                    showToast("Error loading activity data")
                }
            }
        }
    }
    
    private fun formatWalletForDisplay(walletAddress: String): String {
        return if (walletAddress.length >= 8) {
            "${walletAddress.take(4)}...${walletAddress.takeLast(4)}"
        } else {
            walletAddress
        }
    }
    
    private fun updateFollowingCountFromActivities() {
        // Update the Following count in profile page to show number of activities
        try {
            profileFollowingCount.text = activityFeedData.size.toString()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating following count: ${e.message}")
        }
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
        
        // Don't call updateMyTokensDisplay() during init - wallet manager isn't ready yet
        // It will be called when wallet manager is set up
    }
    
    private fun updateMyTokensDisplay() {
        val connectedWallet = walletManager.getConnectedWalletAddress()
        
        if (connectedWallet == null) {
            // No wallet connected - show empty state
            android.util.Log.d("MyTokens", "No wallet connected - showing empty state")
            showMyTokensEmptyState()
            return
        }
        
        // Load wallet-specific tokens from database
        lifecycleScope.launch {
            loadWalletTokensFromDatabase(connectedWallet)
        }
    }
    
    private suspend fun loadWalletTokensFromDatabase(walletAddress: String) {
        try {
            // Use the specific wallet address for testing
            val testWalletAddress = "umuAXMPXgzcgbmg2361ij8jncRWyb8noZeXFFCdvKmNu"
            android.util.Log.d("MyTokens", "Loading tokens for wallet: $testWalletAddress")
            
            // Get tokens that this wallet has voted for (liked)
            val votedTokenIds = databaseService.getUserVotedTokenIds(testWalletAddress)
            android.util.Log.d("MyTokens", "Found ${votedTokenIds.size} voted tokens")
            
            // Get tokens that this wallet participated in presales
            val presaleTokenIds = databaseService.getUserPresaleTokenIds(testWalletAddress)
            android.util.Log.d("MyTokens", "Found ${presaleTokenIds.size} presale participations")
            
            // Get all tokens and filter for wallet interactions
            val allDbTokens = databaseService.getAllTokens()
            val walletTokens = allDbTokens.filter { token ->
                votedTokenIds.contains(token.token_id) || presaleTokenIds.contains(token.token_id)
            }
            
            android.util.Log.d("MyTokens", "Total wallet-related tokens: ${walletTokens.size}")
            
            if (walletTokens.isNotEmpty()) {
                // Convert to CreatedTokenInfo format for existing adapter
                val walletCreatedTokens = walletTokens.map { dbToken ->
                    val isVoted = votedTokenIds.contains(dbToken.token_id)
                    val isPresaleParticipant = presaleTokenIds.contains(dbToken.token_id)
                    
                    val status = when {
                        isVoted && isPresaleParticipant -> "Liked & Invested"
                        isVoted -> "Liked"
                        isPresaleParticipant -> "Invested"
                        else -> dbToken.status
                    }
                    
                    CreatedTokenInfo(
                        name = dbToken.token_name,
                        symbol = dbToken.symbol,
                        description = dbToken.description ?: "Token you've interacted with",
                        supply = 1_000_000_000L, // Default supply
                        launchType = if (dbToken.status == "presale") LaunchType.PRESALE else LaunchType.INSTANT,
                        tokenAddress = dbToken.token_mint_address ?: "Unknown",
                        chatLink = "https://discord.gg/${dbToken.symbol.lowercase()}",
                        status = status
                    )
                }
                
                runOnUiThread {
                    showMyTokensWithData(walletCreatedTokens)
                    showToast("📊 Found ${walletCreatedTokens.size} tokens you've interacted with!")
                }
                
            } else {
                android.util.Log.d("MyTokens", "No wallet interactions found")
                runOnUiThread {
                    showMyTokensEmptyState()
                    showToast("💡 No token interactions found for this wallet")
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("MyTokens", "Error loading wallet tokens: ${e.message}")
            runOnUiThread {
                showMyTokensEmptyState()
                showToast("⚠️ Error loading your tokens")
            }
        }
    }
    
    private fun showMyTokensEmptyState() {
        val emptyState = profilePage.findViewById<LinearLayout>(R.id.myTokensEmptyState)
        myTokensRecyclerView.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }
    
    private fun showMyTokensWithData(walletTokens: List<CreatedTokenInfo>) {
        val emptyState = profilePage.findViewById<LinearLayout>(R.id.myTokensEmptyState)
        myTokensRecyclerView.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        
        val adapter = MyTokensAdapter(walletTokens) { token ->
            showTokenDetails(token)
        }
        myTokensRecyclerView.adapter = adapter
        
        android.util.Log.d("MyTokens", "Successfully displayed ${walletTokens.size} wallet tokens")
    }
    
    private fun updateActivitySummary(walletAddress: String?) {
        // Use the test wallet address if provided
        val testWalletAddress = "umuAXMPXgzcgbmg2361ij8jncRWyb8noZeXFFCdvKmNu"
        
        lifecycleScope.launch {
            try {
                // Get likes given (votes by this wallet)
                val votedTokenIds = databaseService.getUserVotedTokenIds(testWalletAddress)
                profileLikesCount.text = votedTokenIds.size.toString()
                
                // Get presales joined  
                val presaleTokenIds = databaseService.getUserPresaleTokenIds(testWalletAddress)
                profilePresalesCount.text = presaleTokenIds.size.toString()
                
                // Following count - for now just a placeholder
                // In a real app, you'd have a follows table
                profileFollowingCount.text = "0"
                
                android.util.Log.d("ActivitySummary", "Updated for wallet $testWalletAddress: Likes=${votedTokenIds.size}, Presales=${presaleTokenIds.size}")
                
            } catch (e: Exception) {
                android.util.Log.e("ActivitySummary", "Error updating activity summary: ${e.message}")
                // Set default values on error
                profileLikesCount.text = "0"
                profilePresalesCount.text = "0"
                profileFollowingCount.text = "0"
            }
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