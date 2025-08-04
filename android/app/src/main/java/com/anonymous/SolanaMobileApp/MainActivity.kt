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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import com.caverock.androidsvg.SVG
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var swipeContainer: FrameLayout
    private lateinit var walletManager: WalletManager
    private lateinit var databaseService: DatabaseService
    private val tokenCards = mutableListOf<SwipeableTokenCard>()
    private var currentCardIndex = 0
    
    // Page views
    private lateinit var profilePage: View
    private lateinit var discoverContent: View
    private lateinit var presalePage: View
    
    // New Creator Profile Manager
    private lateinit var creatorProfileManager: CreatorProfileManager
    
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
    private lateinit var votersRecyclerView: RecyclerView
    private lateinit var followingSection: LinearLayout
    private lateinit var followingRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var exploreCreatorsButton: Button
    private lateinit var leaderboardButton: Button
    private lateinit var voterAdapter: VoterAdapter
    private lateinit var followingActivityAdapter: ActivityFeedAdapter
    
    // Featured address views
    private lateinit var featuredAddressCard: androidx.cardview.widget.CardView
    private lateinit var featuredUserAvatar: View
    private lateinit var featuredUserName: TextView
    private lateinit var featuredUserWallet: TextView
    private lateinit var featuredUserStats: TextView
    private lateinit var followFeaturedButton: Button
    
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
    
    // Profile page views
    private lateinit var walletConnectionCard: CardView
    private lateinit var profileWalletButton: Button
    private lateinit var walletStatusText: TextView
    private lateinit var profileDetailsCard: CardView
    private lateinit var activityCard: CardView
    private lateinit var profileUserName: TextView
    private lateinit var profileWalletAddress: TextView
    private lateinit var profileSolBalance: TextView
    private lateinit var creatorAddress: TextView
    private lateinit var profileAvatar: ImageView
    private lateinit var requestAirdropButton: Button
    private lateinit var copyAddressButton: Button
    private lateinit var disconnectWalletButton: Button
    
    // Image handling
    private var currentImageUri: Uri? = null
    private var currentImageUrl: String? = null
    private var tempImageUri: Uri? = null
    
    // Created tokens storage (in real app, this would be from database/blockchain)
    private val createdTokens = mutableListOf<CreatedTokenInfo>()
    
    // Wallet balance tracking
    private var currentWalletBalance = 13.72 // Start with 13.72 SOL for demo
    
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
    
    // Activity data - loaded from database
    private var votersData = mutableListOf<VoterData>()
    private var followingActivityData = mutableListOf<ActivityFeedData>()
    private var followedUsers = mutableSetOf<String>() // Track followed user wallet addresses

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupDatabase()
        setupWalletManager()
        setupBottomNavigation()
        setupSwipeCards()
        
        // Load data that requires database after database setup
        loadActivityPageData()
    }
    
    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        swipeContainer = findViewById(R.id.swipeContainer)
        
        // Initialize page views
        discoverContent = findViewById(R.id.discoverContent)
        profilePage = findViewById(R.id.profilePage)
        presalePage = findViewById(R.id.presalePage)
        
        // Initialize profile page views
        walletConnectionCard = profilePage.findViewById(R.id.walletConnectionCard)
        profileWalletButton = profilePage.findViewById(R.id.profileWalletButton)
        walletStatusText = profilePage.findViewById(R.id.walletStatusText)
        profileDetailsCard = profilePage.findViewById(R.id.profileDetailsCard)
        activityCard = profilePage.findViewById(R.id.activityCard)
        profileUserName = profilePage.findViewById(R.id.profileUserName)
        profileWalletAddress = profilePage.findViewById(R.id.profileWalletAddress)
        profileSolBalance = profilePage.findViewById(R.id.profileSolBalance)
        creatorAddress = profilePage.findViewById(R.id.creatorAddress)
        profileAvatar = profilePage.findViewById(R.id.profileAvatar)
        
        // Set creator address immediately (this is the login wallet)
        val loginWallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
        creatorAddress.text = "${loginWallet.take(6)}...${loginWallet.takeLast(6)}"
        
        // Load profile avatar using Dicebear API
        loadProfileAvatar(loginWallet)
        
        requestAirdropButton = profilePage.findViewById(R.id.requestAirdropButton)
        copyAddressButton = profilePage.findViewById(R.id.copyAddressButton)
        disconnectWalletButton = profilePage.findViewById(R.id.disconnectWalletButton)
        
        // Note: CreatorProfileManager will be initialized after database setup
        
        // Initialize presale page views
        presaleRecyclerView = presalePage.findViewById(R.id.presaleRecyclerView)
        walletBalanceText = presalePage.findViewById(R.id.walletBalanceText)
        
        // Initialize wallet balance display
        updateWalletBalanceDisplay()
        
        // Setup presale RecyclerView
        presaleAdapter = PresaleAdapter(presaleTokens) { token -> 
            showBuyDialog(token)
        }
        presaleRecyclerView.layoutManager = LinearLayoutManager(this)
        presaleRecyclerView.adapter = presaleAdapter
        
        // Don't add sample tokens - let user create their own
        // Commenting out sample token to avoid confusion
        // if (createdTokens.isEmpty()) {
        //     createdTokens.add(sample token...)
        // }
        
        // Old profile setup removed - now using CreatorProfileManager
        
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
        votersRecyclerView = activityPage.findViewById(R.id.votersRecyclerView)
        followingSection = activityPage.findViewById(R.id.followingSection)
        followingRecyclerView = activityPage.findViewById(R.id.followingRecyclerView)
        emptyStateLayout = activityPage.findViewById<LinearLayout>(R.id.emptyStateLayout)
        exploreCreatorsButton = activityPage.findViewById(R.id.exploreCreatorsButton)
        leaderboardButton = activityPage.findViewById(R.id.leaderboardButton)
        
        // Initialize featured address views
        featuredAddressCard = activityPage.findViewById(R.id.featuredAddressCard)
        featuredUserAvatar = activityPage.findViewById(R.id.featuredUserAvatar)
        featuredUserName = activityPage.findViewById(R.id.featuredUserName)
        featuredUserWallet = activityPage.findViewById(R.id.featuredUserWallet)
        featuredUserStats = activityPage.findViewById(R.id.featuredUserStats)
        followFeaturedButton = activityPage.findViewById(R.id.followFeaturedButton)
        
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
        setupProfilePage()
    }
    
    private fun setupDatabase() {
        databaseService = DatabaseService()
        
        // Initialize Creator Profile Manager after database is ready
        creatorProfileManager = CreatorProfileManager(
            context = this,
            profileView = profilePage,
            lifecycleScope = lifecycleScope,
            databaseService = databaseService
        )
        
        // Don't add sample tokens anymore - let user create real tokens
        // Sample tokens removed to avoid confusion
        
        // Pass existing cached tokens to the manager
        createdTokens.forEach { token ->
            creatorProfileManager.addNewToken(token)
        }
        android.util.Log.d("MainActivity", "Passed ${createdTokens.size} cached tokens to CreatorProfileManager")
        
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
                showToast("⚠️ Database offline - tap 'Retry' to try connecting again")
                
                // Load sample data when database is unavailable
                loadSampleDataForOfflineMode()
                
                // Show retry button
                showDatabaseRetryOption()
            }
        }
    }
    
    private fun showDatabaseRetryOption() {
        android.app.AlertDialog.Builder(this)
            .setTitle("🔄 Database Connection")
            .setMessage("Unable to connect to Supabase database. This could be due to network issues.\n\nWould you like to retry the connection?")
            .setPositiveButton("Retry Connection") { _, _ ->
                showToast("🔄 Retrying database connection...")
                lifecycleScope.launch {
                    val isConnected = databaseService.testConnection()
                    if (isConnected) {
                        showToast("✅ Database connected successfully!")
                        // Reload all data from database
                        testDatabaseReads()
                        loadActivityPageData()
                    } else {
                        showToast("❌ Still unable to connect - using offline mode")
                    }
                }
            }
            .setNegativeButton("Continue Offline") { _, _ ->
                showToast("📱 Continuing with sample data")
            }
            .setCancelable(false)
            .show()
    }
    
    private fun loadSampleDataForOfflineMode() {
        android.util.Log.d("OfflineMode", "Loading sample data for offline mode...")
        
        // Sample token data for swipe cards
        tokens.clear()
        tokens.addAll(listOf(
            TokenData(
                id = "token1",
                name = "DogeCoin", 
                creator = "DogeCreator",
                description = "The people's cryptocurrency",
                likes = 1245,
                creatorWallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD",
                creatorTwitter = "@dogecreator",
                creatorSolanaHandle = "dogecreator.sol",
                slTokenStaked = 15000,
                totalLikesReceived = 1245,
                tokensLaunched = 3,
                communityLink = "https://discord.gg/doge"
            ),
            TokenData(
                id = "token2",
                name = "SolanaSwap",
                creator = "SwapTeam", 
                description = "Revolutionary DeFi swap protocol",
                likes = 892,
                creatorWallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD",
                creatorTwitter = "@swapteam",
                creatorSolanaHandle = "swapteam.sol",
                slTokenStaked = 25000,
                totalLikesReceived = 2134,
                tokensLaunched = 2,
                communityLink = "https://discord.gg/sswap"
            ),
            TokenData(
                id = "token3",
                name = "MoonRocket",
                creator = "MoonTeam",
                description = "To the moon and beyond!",
                likes = 2156,
                creatorWallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD",
                creatorTwitter = "@moonteam",
                creatorSolanaHandle = "moonteam.sol", 
                slTokenStaked = 50000,
                totalLikesReceived = 3456,
                tokensLaunched = 5,
                communityLink = "https://discord.gg/moon"
            )
        ))
        
        // Sample presale data
        presaleTokens.clear()
        presaleTokens.addAll(listOf(
            PresaleTokenData(
                id = "presale1",
                name = "AlphaToken",
                symbol = "ALPHA", 
                description = "Next generation blockchain solution",
                totalSupply = 1000000000L,
                raisedSol = 25.0,
                startTime = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000), // 2 days ago
                endTime = System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000), // 5 days from now
                creatorAddress = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD",
                tokenAddress = "ALPHa...X9mN"
            ),
            PresaleTokenData(
                id = "presale2",
                name = "BetaCoin",
                symbol = "BETA",
                description = "Innovative DeFi ecosystem", 
                totalSupply = 500000000L,
                raisedSol = 60.0,
                startTime = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000), // 1 day ago
                endTime = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000), // 2 days from now
                creatorAddress = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD",
                tokenAddress = "BETA2...K4pL"
            )
        ))
        
        // Sample voter/activity data
        votersData.clear()
        votersData.addAll(listOf(
            VoterData("2K4s...9xPq", 45, false),
            VoterData("7mN2...4aRt", 32, false),
            VoterData("9wE1...6vXz", 28, true)
        ))
        
        android.util.Log.d("OfflineMode", "Loaded sample data: ${tokens.size} tokens, ${presaleTokens.size} presales, ${votersData.size} voters")
        
        // Refresh all adapters with sample data
        runOnUiThread {
            // Refresh swipe cards
            setupSwipeCards()
            
            // Refresh presale adapter
            presaleAdapter.notifyDataSetChanged()
            
            // Refresh voter adapter
            voterAdapter.notifyDataSetChanged()
            
            // Refresh leaderboards
            loadFallbackLeaderboards()
            refreshLeaderboardAdapters()
            
            showToast("📱 Sample data loaded - app ready to use!")
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
            // DISCOVER PAGE: Prioritize tokens with images, then show others
            val tokensWithImages = databaseService.getTokensWithImages().filter { it.status == "active" }
            val allDbTokens = databaseService.getAllTokens()
            val tokensWithoutImages = allDbTokens.filter { it.status == "active" && it.image_url.isNullOrEmpty() }
            
            // Combine: tokens with images first, then tokens without images
            val activeTokens = tokensWithImages + tokensWithoutImages
            
            if (activeTokens.isNotEmpty()) {
                android.util.Log.d("MainActivity", "Loading ${activeTokens.size} active tokens for discovery")
                android.util.Log.d("MainActivity", "Found ${tokensWithImages.size} tokens with images")
                android.util.Log.d("MainActivity", "Found ${tokensWithoutImages.size} tokens without images")
                
                tokens.clear()
                tokens.addAll(activeTokens.map { dbToken ->
                    android.util.Log.d("MainActivity", "Creating TokenData for: ${dbToken.token_name}, image_url: ${dbToken.image_url}")
                    TokenData(
                        id = dbToken.token_id?.toString() ?: "0",
                        name = dbToken.token_name,
                        creator = extractUsernameFromWallet(dbToken.creator_wallet),
                        description = dbToken.description ?: "No description available",
                        likes = dbToken.vote_count,
                        imageUrl = dbToken.image_url,
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
                
                // Now that tokens are loaded, refresh the swipe cards
                runOnUiThread {
                    android.util.Log.d("MainActivity", "Refreshing swipe cards with loaded tokens")
                    refreshTokenCards()
                }
                
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
            TokenData("1", "MOON Token", "cryptoking", "🚀 Sample token for testing", 1247, null,
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
                    
                    // Parse the creation time from database or use a reasonable default
                    val startTime = try {
                        // If we have presale_started_at, use it
                        dbToken.presale_started_at?.let { timeStr ->
                            // Simple parsing - in real app would use proper date parsing
                            currentTime - (Math.random() * 24 * 60 * 60 * 1000).toLong() // Random time within last 24 hours
                        } ?: (currentTime - (Math.random() * 7 * 24 * 60 * 60 * 1000).toLong()) // Random time within last week
                    } catch (e: Exception) {
                        currentTime - (Math.random() * 24 * 60 * 60 * 1000).toLong() // Fallback to last 24 hours
                    }
                    
                    PresaleTokenData(
                        id = "${dbToken.token_id}", // Use just the token_id as string, not "ps" prefix
                        name = dbToken.token_name,
                        symbol = dbToken.symbol,
                        description = dbToken.description ?: "Presale token - no description available",
                        totalSupply = 1_000_000_000L, // Default supply
                        raisedSol = dbToken.sol_raised, // Current SOL raised
                        startTime = startTime,
                        endTime = currentTime + (7 * 24 * 60 * 60 * 1000L), // 7 days from now
                        creatorAddress = dbToken.creator_wallet,
                        tokenAddress = dbToken.token_mint_address ?: dbToken.creator_wallet,
                        // Use image URL from database, or generate random Dicebear avatar
                        logoUrl = if (!dbToken.image_url.isNullOrEmpty()) {
                            dbToken.image_url
                        } else {
                            // Generate random Dicebear avatar using token name as seed
                            "https://api.dicebear.com/9.x/thumbs/png?seed=${dbToken.token_name}"
                        }
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
                            "SampleCreator", "SampleAddr", 
                            "https://api.dicebear.com/9.x/thumbs/png?seed=SamplePresale")
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
            
            android.util.Log.d("MainActivity", "Top 3 launched tokens SOL raised: ${launchedTokens.take(3).map { "${it.token_name}: ${it.sol_raised} SOL" }}")
            
            tokenLaunchedLeaderboard.clear()
            tokenLaunchedLeaderboard.addAll(launchedTokens.mapIndexed { index, token ->
                // Create varied market caps with realistic decimal amounts
                val baseMarketCap = when(index) {
                    0 -> 311213.2  // $311,213.2
                    1 -> 187456.8  // $187,456.8
                    2 -> 124891.5  // $124,891.5
                    3 -> 89742.3   // $89,742.3
                    4 -> 67334.9   // $67,334.9
                    else -> 45128.7 // $45,128.7
                }
                val marketCapSOL = baseMarketCap / 180.0 // Convert USD to SOL at $180/SOL
                val launchTime = System.currentTimeMillis() - (index * 24 * 60 * 60 * 1000L) // Each token launched a day apart
                
                android.util.Log.d("MainActivity", "Token ${token.token_name}: Rank ${index + 1} → ${marketCapSOL.toInt()} SOL / $${baseMarketCap.toInt()} USD")
                TokenLaunchedData(
                    rank = index + 1,
                    tokenName = token.token_name,
                    tokenSymbol = token.symbol,
                    marketCap = marketCapSOL, // Market cap in SOL
                    marketCapUSD = baseMarketCap, // Market cap in USD
                    creator = extractUsernameFromWallet(token.creator_wallet),
                    launchDate = launchTime,
                    logoUrl = if (!token.image_url.isNullOrEmpty()) {
                        token.image_url
                    } else {
                        "https://api.dicebear.com/9.x/thumbs/png?seed=${token.token_name}"
                    }
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
            
            val usersWithSLBalance = users.filter { it.sl_token_balance > 0 }
            if (usersWithSLBalance.isNotEmpty()) {
                // Use real database data if available
                slStakedLeaderboard.addAll(usersWithSLBalance.sortedByDescending { it.sl_token_balance }.take(10).mapIndexed { index, user ->
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
            } else {
                // Use fallback mock data since database has no SL token holders
                slStakedLeaderboard.addAll(listOf(
                    SLTokenStakedData(
                        rank = 1,
                        walletAddress = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD",
                        twitterHandle = "@tokenking",
                        solanaDomain = "tokenking.sol",
                        slTokenBalance = 500000,
                        dailyVotingRightsRemaining = 8,
                        dailyVotingRightsTotal = 10
                    ),
                    SLTokenStakedData(
                        rank = 2,
                        walletAddress = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                        twitterHandle = "@cryptowhale",
                        solanaDomain = "whale.sol",
                        slTokenBalance = 350000,
                        dailyVotingRightsRemaining = 6,
                        dailyVotingRightsTotal = 8
                    ),
                    SLTokenStakedData(
                        rank = 3,
                        walletAddress = "DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1",
                        twitterHandle = "@defibuilder",
                        solanaDomain = null,
                        slTokenBalance = 280000,
                        dailyVotingRightsRemaining = 5,
                        dailyVotingRightsTotal = 7
                    ),
                    SLTokenStakedData(
                        rank = 4,
                        walletAddress = "5KvfoxsVKL8DAoSVMiRzKwZdRNvTpHdeFvMyCkAjbQEd",
                        twitterHandle = null,
                        solanaDomain = "trader.sol",
                        slTokenBalance = 200000,
                        dailyVotingRightsRemaining = 4,
                        dailyVotingRightsTotal = 6
                    ),
                    SLTokenStakedData(
                        rank = 5,
                        walletAddress = "8mHpMZrWVFaaQpgD9Tk6cUzrVjvfJjCTJb1z7zKw2JrK",
                        twitterHandle = "@solanaexplorer",
                        solanaDomain = null,
                        slTokenBalance = 150000,
                        dailyVotingRightsRemaining = 3,
                        dailyVotingRightsTotal = 5
                    )
                ))
                android.util.Log.d("MainActivity", "Using fallback SL Staked data since database has no SL token holders")
            }
            
            android.util.Log.d("MainActivity", "Loaded leaderboards: ${tokenLaunchedLeaderboard.size} launched, ${creatorMostLikesLeaderboard.size} likes, ${creatorMostLaunchedLeaderboard.size} most launched, ${slStakedLeaderboard.size} SL staked")
            android.util.Log.d("MainActivity", "Users with SL balance: ${users.filter { it.sl_token_balance > 0 }.size}/${users.size}")
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
        
        // Add mock SL Staked data
        slStakedLeaderboard.addAll(listOf(
            SLTokenStakedData(
                rank = 1,
                walletAddress = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD",
                twitterHandle = "@tokenking",
                solanaDomain = "tokenking.sol",
                slTokenBalance = 500000,
                dailyVotingRightsRemaining = 8,
                dailyVotingRightsTotal = 10
            ),
            SLTokenStakedData(
                rank = 2,
                walletAddress = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                twitterHandle = "@cryptowhale",
                solanaDomain = "whale.sol",
                slTokenBalance = 350000,
                dailyVotingRightsRemaining = 6,
                dailyVotingRightsTotal = 8
            ),
            SLTokenStakedData(
                rank = 3,
                walletAddress = "DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1",
                twitterHandle = "@defibuilder",
                solanaDomain = null,
                slTokenBalance = 280000,
                dailyVotingRightsRemaining = 5,
                dailyVotingRightsTotal = 7
            ),
            SLTokenStakedData(
                rank = 4,
                walletAddress = "5KvfoxsVKL8DAoSVMiRzKwZdRNvTpHdeFvMyCkAjbQEd",
                twitterHandle = null,
                solanaDomain = "trader.sol",
                slTokenBalance = 200000,
                dailyVotingRightsRemaining = 4,
                dailyVotingRightsTotal = 6
            ),
            SLTokenStakedData(
                rank = 5,
                walletAddress = "8mHpMZrWVFaaQpgD9Tk6cUzrVjvfJjCTJb1z7zKw2JrK",
                twitterHandle = "@solanaexplorer",
                solanaDomain = null,
                slTokenBalance = 150000,
                dailyVotingRightsRemaining = 3,
                dailyVotingRightsTotal = 5
            )
        ))
        
        android.util.Log.d("MainActivity", "Using fallback leaderboard data - SL Staked: ${slStakedLeaderboard.size}")
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
                showToast("Wallet connected: $displayKey")
                
                // Update profile UI to show connected state
                updateProfileUI(true, publicKey, displayKey)
                
                android.util.Log.d("CreatorProfile", "Wallet connected: $displayKey")
            }
            
            override fun onWalletDisconnected() {
                showToast("Wallet disconnected")
                
                // Update profile UI to show disconnected state
                updateProfileUI(false, null, null)
                
                android.util.Log.d("CreatorProfile", "Wallet disconnected")
            }
            
            override fun onWalletError(error: String) {
                showToast("Wallet error: $error")
            }
            
            override fun onTransactionComplete(signature: String) {
                showToast("Transaction complete: ${signature.take(8)}...")
                // Update SOL balance after transaction from database
                val walletAddress = walletManager.getConnectedPublicKey()
                if (walletAddress != null) {
                    lifecycleScope.launch {
                        val solBalance = databaseService.getUserSolBalance(walletAddress)
                        runOnUiThread {
                            profileSolBalance.text = "${String.format("%.2f", solBalance)} SOL"
                        }
                    }
                }
            }
        })
        
        // Creator profile is now independent of wallet connection
        // Note: Profile now only shows creator tokens via CreatorProfileManager
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
        android.util.Log.d("MainActivity", "setupSwipeCards called - tokens.size: ${tokens.size}")
        // Clear existing cards
        swipeContainer.removeAllViews()
        tokenCards.clear()
        
        // Create cards for first 3 tokens (stack of 3)
        for (i in 0 until minOf(3, tokens.size)) {
            android.util.Log.d("MainActivity", "Creating card for token index: ${currentCardIndex + i}")
            createAndAddCard(currentCardIndex + i)
        }
        android.util.Log.d("MainActivity", "setupSwipeCards completed - created ${tokenCards.size} cards")
    }
    
    private fun createAndAddCard(tokenIndex: Int) {
        android.util.Log.d("MainActivity", "createAndAddCard called for index: $tokenIndex, tokens.size: ${tokens.size}")
        if (tokenIndex >= tokens.size) {
            android.util.Log.d("MainActivity", "tokenIndex $tokenIndex >= tokens.size ${tokens.size}, returning")
            return
        }
        
        val card = SwipeableTokenCard(this)
        android.util.Log.d("MainActivity", "Created card, setting token data for: ${tokens[tokenIndex].name}")
        android.util.Log.d("MainActivity", "Token image URL: ${tokens[tokenIndex].imageUrl}")
        card.setTokenData(tokens[tokenIndex])
        android.util.Log.d("MainActivity", "Token data set for card")
        
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
        
        // Setup collapsible proposal gallery
        setupCollapsibleProposalGallery()
        
        // Load recently created tokens
        loadRecentlyCreatedTokens()
        activityPage.visibility = View.GONE
        
        // Refresh creator tokens when profile tab is shown
        creatorProfileManager.loadCreatorTokens()
        android.util.Log.d("CreatorProfile", "Profile tab shown, refreshing creator tokens")
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
        
        // Ensure we start in proposal mode
        launchTypeGroup.clearCheck()
        launchTypeGroup.check(R.id.presaleLaunchRadio)
        updateUIForCommunityPresale()
    }
    
    private fun showActivityTab() {
        discoverContent.visibility = View.GONE
        profilePage.visibility = View.GONE
        presalePage.visibility = View.GONE
        leaderboardPage.visibility = View.GONE
        createTokenPage.visibility = View.GONE
        activityPage.visibility = View.VISIBLE
        
        // Load activity feed when showing activities tab
        android.util.Log.d("MainActivity", "Showing activity tab - loading following activities")
        loadFollowingActivities()
    }
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun updateProfileUI(connected: Boolean, fullAddress: String?, displayAddress: String?) {
        if (connected && fullAddress != null && displayAddress != null) {
            // Wallet connected - show profile details
            profileWalletButton.text = "✓ Wallet Connected"
            walletStatusText.text = "Ready to use SwipeLaunch"
            profileDetailsCard.visibility = View.VISIBLE
            activityCard.visibility = View.VISIBLE
            
            // Update profile info
            profileWalletAddress.text = displayAddress
            
            // Set creator address (shortened format)
            val loginWallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
            creatorAddress.text = "${loginWallet.take(6)}...${loginWallet.takeLast(6)}"
            
            // Set default activity stats with random realistic numbers
            val randomLikes = (15..45).random()
            val randomPresales = (3..8).random()
            val randomFollowing = (5..15).random()
            val randomFollowers = (25..85).random()
            
            findViewById<TextView>(R.id.profileLikesCount).text = randomLikes.toString()
            findViewById<TextView>(R.id.profilePresalesCount).text = randomPresales.toString()
            findViewById<TextView>(R.id.profileFollowingCount).text = randomFollowing.toString()
            findViewById<TextView>(R.id.profileFollowersCount).text = randomFollowers.toString()
            
            // Load SOL balance from database
            lifecycleScope.launch {
                val solBalance = databaseService.getUserSolBalance(fullAddress)
                runOnUiThread {
                    profileSolBalance.text = "${String.format("%.2f", solBalance)} SOL"
                }
            }
            
            // Update activity summary with real data
            updateActivitySummary(fullAddress)
            
            // Show My Tokens section when connected
            myTokensCard.visibility = View.VISIBLE
            myCreatedTokensCard.visibility = View.VISIBLE
            updateMyTokensDisplay()
        } else {
            // Wallet disconnected - hide profile details but keep creator tokens visible
            profileWalletButton.text = "Connect Solana Wallet"
            walletStatusText.text = "Connect your wallet to see your profile details"
            profileDetailsCard.visibility = View.GONE
            activityCard.visibility = View.GONE
            
            // Keep creator token sections visible even when wallet is disconnected
            myTokensCard.visibility = View.VISIBLE
            myCreatedTokensCard.visibility = View.VISIBLE
            updateMyTokensDisplay()
        }
    }
    */
    
    private fun updateWalletBalance() {
        // Use current wallet balance - matches profile page
        val balance = if (walletManager.isWalletConnected()) "${String.format("%.2f", currentWalletBalance)} SOL" else "0.00 SOL"
        walletBalanceText.text = balance
    }
    
    private fun showBuyDialog(token: PresaleTokenData) {
        if (!walletManager.isWalletConnected()) {
            showToast("Please connect your wallet first in the Profile tab")
            return
        }
        
        val tokensPerSol = token.getTokensPerSol()
        val dialogView = createBuyDialogView(token)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Buy ${token.symbol}")
            .setMessage("Purchase ${token.name} tokens\n\n" +
                       "Rate: 1 SOL = ${String.format("%.0f", tokensPerSol)} ${token.symbol}\n" +
                       "Target: 100 SOL (${token.getProgressPercentage()}% complete)\n\n" +
                       "How much SOL do you want to spend?")
            .setView(dialogView)
            .setPositiveButton("Confirm Purchase") { _, _ ->
                val amountInput = dialogView.tag as EditText
                val solAmountText = amountInput.text.toString()
                val solAmount = solAmountText.toDoubleOrNull() ?: 0.0
                
                if (solAmount < 0.1) {
                    showToast("Minimum purchase is 0.1 SOL")
                    return@setPositiveButton
                }
                
                if (solAmount > 10.0) {
                    showToast("Maximum purchase is 10 SOL per transaction")
                    return@setPositiveButton
                }
                
                processPurchase(token, solAmount)
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun createBuyDialogView(token: PresaleTokenData): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }
        
        val amountInput = EditText(this).apply {
            hint = "Enter SOL amount (e.g. 0.5)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText("1.0")
            textSize = 16f
        }
        
        val infoText = TextView(this).apply {
            text = "Minimum: 0.1 SOL\nYou will receive: ${String.format("%.0f", token.getTokensPerSol())} ${token.symbol} per SOL"
            textSize = 14f
            setTextColor(getColor(android.R.color.darker_gray))
            setPadding(0, 20, 0, 0)
        }
        
        layout.addView(amountInput)
        layout.addView(infoText)
        
        // Store the input reference for later use
        layout.tag = amountInput
        
        return layout
    }
    
    private fun processPurchase(token: PresaleTokenData, solAmount: Double) {
        if (!walletManager.isWalletConnected()) {
            showToast("Wallet not connected")
            return
        }
        
        // Check if user has sufficient balance
        if (currentWalletBalance < solAmount) {
            showToast("❌ Insufficient balance! You have ${String.format("%.2f", currentWalletBalance)} SOL")
            return
        }
        
        lifecycleScope.launch {
            try {
                showToast("Processing transaction...")
                
                // Deduct balance first
                if (!deductBalance(solAmount)) {
                    showToast("❌ Insufficient balance")
                    return@launch
                }
                
                // Simulate transaction processing
                kotlinx.coroutines.delay(2000)
                
                val walletAddress = walletManager.getConnectedPublicKey()
                if (walletAddress != null) {
                    // Convert token ID to Int safely
                    android.util.Log.d("MainActivity", "Processing purchase for token ID: '${token.id}'")
                    val tokenIdInt = try {
                        val id = token.id.toInt()
                        android.util.Log.d("MainActivity", "Converted token ID to int: $id")
                        id
                    } catch (e: NumberFormatException) {
                        android.util.Log.e("MainActivity", "Invalid token ID: '${token.id}' - ${e.message}")
                        showToast("❌ Invalid token ID: ${token.id}")
                        return@launch
                    }
                    
                    // Add presale participant to database
                    val participantSuccess = databaseService.addPresaleParticipant(tokenIdInt, walletAddress, solAmount)
                    android.util.Log.d("MainActivity", "Participant added: $participantSuccess")
                    
                    // Update token's sol_raised amount in database
                    val success = databaseService.updateTokenSolRaised(tokenIdInt, solAmount)
                    android.util.Log.d("MainActivity", "Sol raised updated: $success")
                    
                    if (success) {
                        val tokensReceived = token.getTokensPerSol() * solAmount
                        showToast("✅ Purchase successful!\nReceived ${String.format("%.0f", tokensReceived)} ${token.symbol}\nSpent ${String.format("%.2f", solAmount)} SOL")
                        
                        // Refresh presale data to show updated progress
                        loadPresaleDataFromDatabase()
                    } else {
                        // Refund the balance since database update failed
                        currentWalletBalance += solAmount
                        updateWalletBalanceDisplay()
                        showToast("⚠️ Transaction failed - balance refunded")
                    }
                } else {
                    // Refund the balance since wallet address not found
                    currentWalletBalance += solAmount
                    updateWalletBalanceDisplay()
                    showToast("❌ Error: Wallet address not found - balance refunded")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error processing purchase: ${e.message}")
                // Refund the balance since transaction failed
                currentWalletBalance += solAmount
                updateWalletBalanceDisplay()
                showToast("❌ Purchase failed: ${e.message} - balance refunded")
            }
        }
    }
    
    private fun showUserLikesDialog() {
        if (!walletManager.isWalletConnected()) {
            showToast("Please connect your wallet to view your likes")
            return
        }
        
        lifecycleScope.launch {
            try {
                val walletAddress = walletManager.getConnectedPublicKey()
                if (walletAddress == null) {
                    showToast("Wallet not properly connected")
                    return@launch
                }
                
                // Get user's votes from database
                val userVotes = databaseService.getUserVotes().filter { it.user_wallet == walletAddress }
                val allTokens = databaseService.getAllTokens()
                
                android.util.Log.d("MainActivity", "User votes for $walletAddress: ${userVotes.size}")
                android.util.Log.d("MainActivity", "All tokens: ${allTokens.size}")
                
                // Create list of liked tokens with details
                val likedTokens = userVotes.mapNotNull { vote ->
                    allTokens.find { it.token_id == vote.token_id }?.let { token ->
                        LikedTokenInfo(
                            id = token.token_id ?: 0,
                            name = token.token_name,
                            symbol = token.symbol,
                            status = token.status,
                            description = token.description,
                            creator = token.creator_wallet,
                            logoUrl = if (!token.image_url.isNullOrEmpty()) {
                                token.image_url
                            } else {
                                "https://api.dicebear.com/9.x/thumbs/png?seed=${token.token_name}"
                            }
                        )
                    }
                }
                
                android.util.Log.d("MainActivity", "Liked tokens found: ${likedTokens.size}")
                
                runOnUiThread {
                    showLikesDialog(likedTokens)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading user likes: ${e.message}")
                runOnUiThread {
                    showToast("Error loading your likes")
                }
            }
        }
    }
    
    private fun showLikesDialog(likedTokens: List<LikedTokenInfo>) {
        val dialogView = layoutInflater.inflate(R.layout.likes_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val likesRecyclerView = dialogView.findViewById<RecyclerView>(R.id.likesRecyclerView)
        val emptyState = dialogView.findViewById<LinearLayout>(R.id.emptyState)
        
        if (likedTokens.isEmpty()) {
            likesRecyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            dialogTitle.text = "Your Likes"
        } else {
            likesRecyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            dialogTitle.text = "Your Likes (${likedTokens.size})"
            
            val adapter = LikedTokenAdapter(likedTokens) { token ->
                dialog.dismiss()
                showTokenInfoDialog(token)
            }
            
            likesRecyclerView.layoutManager = LinearLayoutManager(this)
            likesRecyclerView.adapter = adapter
        }
        
        dialog.show()
    }
    
    private fun showTokenInfoDialog(token: LikedTokenInfo) {
        val message = "🪙 Token: ${token.name} (${token.symbol})\n\n" +
                     "📊 Status: ${token.status.uppercase()}\n\n" +
                     "👤 Creator: ${formatWalletForDisplay(token.creator)}\n\n" +
                     "${token.description ?: "No description available"}"
        
        AlertDialog.Builder(this)
            .setTitle("Token Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .setNeutralButton("View in Discover") { _, _ ->
                bottomNavigation.selectedItemId = R.id.nav_discover
                showToast("Navigate to ${token.name} in discover tab")
            }
            .create()
            .show()
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
        // Setup voters adapter
        voterAdapter = VoterAdapter(votersData) { voter, position ->
            // Handle follow/unfollow
            val updatedVoter = voter.copy(isFollowed = !voter.isFollowed)
            votersData[position] = updatedVoter
            voterAdapter.notifyItemChanged(position)
            
            if (updatedVoter.isFollowed) {
                followedUsers.add(voter.walletAddress)
                android.util.Log.d("MainActivity", "Added to followed users: ${voter.walletAddress}")
                showToast("✅ Now following ${formatWalletForDisplay(voter.walletAddress)}")
            } else {
                followedUsers.remove(voter.walletAddress)
                android.util.Log.d("MainActivity", "Removed from followed users: ${voter.walletAddress}")
                showToast("❌ Unfollowed ${formatWalletForDisplay(voter.walletAddress)}")
            }
            
            android.util.Log.d("MainActivity", "Total followed users now: ${followedUsers.size}")
            android.util.Log.d("MainActivity", "Current followed users: ${followedUsers.joinToString()}")
            
            // Update following activities and count immediately
            loadFollowingActivities()
            // updateFollowingCountFromActivities() // OLD PROFILE FUNCTION - COMMENTED OUT
        }
        
        votersRecyclerView.layoutManager = LinearLayoutManager(this)
        votersRecyclerView.adapter = voterAdapter
        
        // Setup following activities adapter
        followingActivityAdapter = ActivityFeedAdapter(
            followingActivityData,
            onViewTokenClick = { activity ->
                showTokenDetailPopup(activity)
            }
        )
        
        followingRecyclerView.layoutManager = LinearLayoutManager(this)
        followingRecyclerView.adapter = followingActivityAdapter
        
        // Setup explore creators button
        exploreCreatorsButton.setOnClickListener {
            showToast("Navigate to creator discovery (placeholder)")
            bottomNavigation.selectedItemId = R.id.nav_discover
        }
        
        // Setup leaderboard button
        leaderboardButton.setOnClickListener {
            showLeaderboardOverlay()
        }
        
        // Initially hide following section and show empty state if no follows
        followingSection.visibility = View.GONE
        emptyStateLayout.visibility = if (followedUsers.isEmpty()) View.VISIBLE else View.GONE
        featuredAddressCard.visibility = View.GONE
    }
    
    private fun loadActivityPageData() {
        // Setup featured address section
        setupFeaturedAddress()
        
        // Load voters data from database
        loadVotersData()
    }
    
    private fun loadVotersData() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("MainActivity", "Loading voters data from database...")
                
                // Clear existing data
                votersData.clear()
                
                // Load user votes to get who voted
                val votes = databaseService.getUserVotes()
                android.util.Log.d("MainActivity", "Loaded ${votes.size} votes")
                
                // Group votes by user to count how many tokens each user voted on
                val voterStats = votes.groupBy { it.user_wallet }
                    .map { (wallet, userVotes) ->
                        val isFollowed = followedUsers.contains(wallet)
                        android.util.Log.d("MainActivity", "Voter $wallet - followed: $isFollowed")
                        VoterData(
                            walletAddress = wallet,
                            tokensVoted = userVotes.size,
                            isFollowed = isFollowed
                        )
                    }
                    .sortedByDescending { it.tokensVoted } // Sort by most active voters
                
                android.util.Log.d("MainActivity", "Voters with followed status: ${voterStats.map { "${it.walletAddress.take(8)} -> ${it.isFollowed}" }}")
                
                votersData.addAll(voterStats)
                
                android.util.Log.d("MainActivity", "Created ${votersData.size} voter entries")
                
                // Update UI on main thread
                runOnUiThread {
                    voterAdapter.notifyDataSetChanged()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading voters data: ${e.message}")
                android.util.Log.e("MainActivity", "Error details: ${e.stackTraceToString()}")
            }
        }
    }
    
    private fun loadFollowingActivities() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("MainActivity", "Loading following activities...")
                android.util.Log.d("MainActivity", "Currently followed users: ${followedUsers.size} - ${followedUsers.joinToString()}")
                
                // Clear existing data
                followingActivityData.clear()
                
                // Always refresh the adapter first
                runOnUiThread {
                    followingActivityAdapter.notifyDataSetChanged()
                }
                
                if (followedUsers.isEmpty()) {
                    android.util.Log.d("MainActivity", "No followed users, completely hiding following section")
                    runOnUiThread {
                        followingSection.visibility = View.GONE
                        followingRecyclerView.visibility = View.GONE
                        emptyStateLayout.visibility = View.GONE
                    }
                    return@launch
                }
                
                // Load tokens and votes for followed users only
                val allTokens = databaseService.getAllTokens()
                val allVotes = databaseService.getUserVotes()
                android.util.Log.d("MainActivity", "Total votes in database: ${allVotes.size}")
                android.util.Log.d("MainActivity", "Followed users: ${followedUsers.joinToString()}")
                
                val votes = allVotes.filter { followedUsers.contains(it.user_wallet) }
                
                android.util.Log.d("MainActivity", "Found ${votes.size} votes from followed users")
                
                // Debug: show which users have votes
                allVotes.groupBy { it.user_wallet }.forEach { (wallet, userVotes) ->
                    val isFollowed = followedUsers.contains(wallet)
                    android.util.Log.d("MainActivity", "User $wallet: ${userVotes.size} votes, followed: $isFollowed")
                }
                
                // Convert votes to activity data with diverse activity types
                votes.forEachIndexed { index, vote ->
                    val token = allTokens.find { it.token_id == vote.token_id }
                    if (token != null) {
                        val userName = formatWalletForDisplay(vote.user_wallet)
                        val baseTimestamp = System.currentTimeMillis() - (Math.random() * 7200000).toLong()
                        
                        // Generate diverse activity types based on index and token data
                        val activities = mutableListOf<ActivityFeedData>()
                        
                        // Always include the like activity
                        activities.add(ActivityFeedData(
                            id = "vote_${vote.user_wallet}_${vote.token_id}",
                            userId = vote.user_wallet,
                            userName = userName,
                            userAvatar = "https://api.dicebear.com/9.x/thumbs/png?seed=${vote.user_wallet}",
                            activityType = ActivityType.LIKE,
                            description = "liked ${token.token_name}",
                            timestamp = baseTimestamp,
                            tokenInfo = TokenActivityInfo(
                                tokenName = token.token_name,
                                tokenCreator = formatWalletForDisplay(token.creator_wallet),
                                tokenPrice = when (token.status) {
                                    "launched" -> "${String.format("%.2f", token.launch_price_sol ?: 0.05)} SOL"
                                    "presale" -> "${String.format("%.2f", token.launch_price_sol ?: 0.03)} SOL"
                                    else -> "Not launched yet"
                                }
                            )
                        ))
                        
                        // Add presale activities for presale tokens
                        if (token.status == "presale" && Math.random() > 0.4) {
                            val presaleAmount = (Math.random() * 5 + 0.1).let { String.format("%.2f", it) }
                            activities.add(ActivityFeedData(
                                id = "presale_${vote.user_wallet}_${vote.token_id}",
                                userId = vote.user_wallet,
                                userName = userName,
                                userAvatar = "https://api.dicebear.com/9.x/thumbs/png?seed=${vote.user_wallet}",
                                activityType = ActivityType.PRESALE,
                                description = "joined presale with ${presaleAmount} SOL for ${token.token_name}",
                                timestamp = baseTimestamp - (Math.random() * 3600000).toLong(),
                                tokenInfo = TokenActivityInfo(
                                    tokenName = token.token_name,
                                    tokenCreator = formatWalletForDisplay(token.creator_wallet),
                                    tokenPrice = "${presaleAmount} SOL contributed"
                                )
                            ))
                        }
                        
                        // Add launch activities for successful tokens
                        if (token.status == "launched" && token.vote_count > 50 && Math.random() > 0.6) {
                            activities.add(ActivityFeedData(
                                id = "launch_${vote.user_wallet}_${vote.token_id}",
                                userId = vote.user_wallet,
                                userName = userName,
                                userAvatar = "https://api.dicebear.com/9.x/thumbs/png?seed=${vote.user_wallet}",
                                activityType = ActivityType.LAUNCH,
                                description = "celebrated ${token.token_name} reaching ${token.vote_count} votes and launching!",
                                timestamp = baseTimestamp - (Math.random() * 7200000).toLong(),
                                tokenInfo = TokenActivityInfo(
                                    tokenName = token.token_name,
                                    tokenCreator = formatWalletForDisplay(token.creator_wallet),
                                    tokenPrice = "${token.vote_count} community votes"
                                )
                            ))
                        }
                        
                        // Add follow activities occasionally
                        if (Math.random() > 0.7) {
                            val followedCreator = allTokens.filter { it.creator_wallet != vote.user_wallet }.randomOrNull()
                            if (followedCreator != null) {
                                val creatorTokenCount = allTokens.count { it.creator_wallet == followedCreator.creator_wallet }
                                activities.add(ActivityFeedData(
                                    id = "follow_${vote.user_wallet}_${followedCreator.creator_wallet}",
                                    userId = vote.user_wallet,
                                    userName = userName,
                                    userAvatar = "https://api.dicebear.com/9.x/thumbs/png?seed=${vote.user_wallet}",
                                    activityType = ActivityType.FOLLOW,
                                    description = "started following ${formatWalletForDisplay(followedCreator.creator_wallet)} (${creatorTokenCount} tokens created)",
                                    timestamp = baseTimestamp - (Math.random() * 10800000).toLong(),
                                    tokenInfo = TokenActivityInfo(
                                        tokenName = "Creator Profile",
                                        tokenCreator = formatWalletForDisplay(followedCreator.creator_wallet),
                                        tokenPrice = "${creatorTokenCount} tokens created"
                                    )
                                ))
                            }
                        }
                        
                        followingActivityData.addAll(activities)
                        android.util.Log.d("MainActivity", "Added ${activities.size} activities for ${vote.user_wallet} - ${token.token_name}")
                    }
                }
                
                // Sort by timestamp (most recent first) and limit to 20 items
                followingActivityData.sortByDescending { it.timestamp }
                if (followingActivityData.size > 20) {
                    val originalSize = followingActivityData.size
                    val limitedData = followingActivityData.take(20).toMutableList()
                    followingActivityData.clear()
                    followingActivityData.addAll(limitedData)
                    android.util.Log.d("MainActivity", "Limited following activities from $originalSize to ${followingActivityData.size}")
                }
                
                android.util.Log.d("MainActivity", "Created ${followingActivityData.size} following activities")
                if (followingActivityData.isNotEmpty()) {
                    android.util.Log.d("MainActivity", "First activity userAvatar: ${followingActivityData[0].userAvatar}")
                }
                
                // Update UI on main thread
                runOnUiThread {
                    followingActivityAdapter.notifyDataSetChanged()
                    
                    // Show/hide following section and empty state properly
                    if (followingActivityData.isNotEmpty()) {
                        followingSection.visibility = View.VISIBLE
                        followingRecyclerView.visibility = View.VISIBLE
                        emptyStateLayout.visibility = View.GONE
                        android.util.Log.d("MainActivity", "Showing following section with ${followingActivityData.size} activities")
                    } else {
                        // If we have followed users but no activities, show empty message
                        if (followedUsers.isNotEmpty()) {
                            followingSection.visibility = View.VISIBLE
                            followingRecyclerView.visibility = View.GONE
                            emptyStateLayout.visibility = View.VISIBLE
                            android.util.Log.d("MainActivity", "Showing empty state - followed users but no activities")
                        } else {
                            followingSection.visibility = View.GONE
                            android.util.Log.d("MainActivity", "Hiding following section - no followed users")
                        }
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error loading following activities: ${e.message}")
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
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun updateFollowingCountFromActivities() {
        // Update the Following count in profile page to show number of followed users
        try {
            profileFollowingCount.text = followedUsers.size.toString()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating following count: ${e.message}")
        }
    }
    */
    
    private fun setupFeaturedAddress() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("MainActivity", "Setting up featured address...")
                
                // Get users from database to find a featured user
                val users = databaseService.getUsers()
                val tokens = databaseService.getAllTokens()
                val votes = databaseService.getUserVotes()
                
                android.util.Log.d("MainActivity", "Found ${users.size} users, ${tokens.size} tokens, ${votes.size} votes")
                
                if (users.isNotEmpty()) {
                    // Find user with most activity (tokens created + votes)
                    val userStats = users.map { user ->
                        val tokenCount = tokens.count { it.creator_wallet == user.wallet_address }
                        val voteCount = votes.count { it.user_wallet == user.wallet_address }
                        val totalActivity = tokenCount + voteCount
                        Triple(user, tokenCount, totalActivity)
                    }.sortedByDescending { it.third }
                    
                    val (featuredUser, tokenCount, _) = userStats.first()
                    val userVotes = votes.count { it.user_wallet == featuredUser.wallet_address }
                    
                    android.util.Log.d("MainActivity", "Featured user: ${featuredUser.wallet_address}, tokens: $tokenCount, votes: $userVotes")
                    
                    runOnUiThread {
                        // Display featured user info
                        featuredUserName.text = featuredUser.solana_name ?: formatWalletForDisplay(featuredUser.wallet_address)
                        featuredUserWallet.text = formatWalletForDisplay(featuredUser.wallet_address)
                        featuredUserStats.text = "🚀 $tokenCount tokens launched • ❤️ $userVotes likes"
                        
                        // Generate avatar color based on wallet address
                        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#FDA7DF")
                        val colorIndex = featuredUser.wallet_address.hashCode() % colors.size
                        featuredUserAvatar.setBackgroundColor(android.graphics.Color.parseColor(colors[kotlin.math.abs(colorIndex)]))
                        
                        // Setup follow button
                        var isFollowing = followedUsers.contains(featuredUser.wallet_address)
                        followFeaturedButton.text = if (isFollowing) "Following" else "Follow"
                        followFeaturedButton.setBackgroundResource(if (isFollowing) R.drawable.follow_button_following else R.drawable.follow_button_background)
                        followFeaturedButton.setTextColor(android.graphics.Color.parseColor(if (isFollowing) "#4CAF50" else "#9945FF"))
                        
                        followFeaturedButton.setOnClickListener {
                            isFollowing = !isFollowing
                            if (isFollowing) {
                                followedUsers.add(featuredUser.wallet_address)
                                followFeaturedButton.text = "Following"
                                followFeaturedButton.setBackgroundResource(R.drawable.follow_button_following)
                                followFeaturedButton.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                                showToast("✅ Now following ${featuredUserName.text}")
                            } else {
                                followedUsers.remove(featuredUser.wallet_address)
                                followFeaturedButton.text = "Follow"
                                followFeaturedButton.setBackgroundResource(R.drawable.follow_button_background)
                                followFeaturedButton.setTextColor(android.graphics.Color.parseColor("#9945FF"))
                                showToast("❌ Unfollowed ${featuredUserName.text}")
                            }
                            // Refresh the following activities
                            loadFollowingActivities()
                            // updateFollowingCountFromActivities() // OLD PROFILE FUNCTION - COMMENTED OUT
                        }
                        
                        // Show the featured address card
                        featuredAddressCard.visibility = View.VISIBLE
                        android.util.Log.d("MainActivity", "Featured address card shown")
                    }
                } else {
                    android.util.Log.d("MainActivity", "No users found, showing default featured address")
                    // Show a default featured address when no users are found
                    runOnUiThread {
                        featuredUserName.text = "SwipeLaunch Team"
                        featuredUserWallet.text = "umuA...mNu"
                        featuredUserStats.text = "🚀 Official account • ⭐ Featured creator"
                        featuredUserAvatar.setBackgroundColor(android.graphics.Color.parseColor("#9945FF"))
                        
                        // Setup follow button
                        val defaultWallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
                        var isFollowing = followedUsers.contains(defaultWallet)
                        followFeaturedButton.text = if (isFollowing) "Following" else "Follow"
                        followFeaturedButton.setBackgroundResource(if (isFollowing) R.drawable.follow_button_following else R.drawable.follow_button_background)
                        followFeaturedButton.setTextColor(android.graphics.Color.parseColor(if (isFollowing) "#4CAF50" else "#9945FF"))
                        
                        followFeaturedButton.setOnClickListener {
                            isFollowing = !isFollowing
                            if (isFollowing) {
                                followedUsers.add(defaultWallet)
                                followFeaturedButton.text = "Following"
                                followFeaturedButton.setBackgroundResource(R.drawable.follow_button_following)
                                followFeaturedButton.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                                showToast("✅ Now following SwipeLaunch Team")
                            } else {
                                followedUsers.remove(defaultWallet)
                                followFeaturedButton.text = "Follow"
                                followFeaturedButton.setBackgroundResource(R.drawable.follow_button_background)
                                followFeaturedButton.setTextColor(android.graphics.Color.parseColor("#9945FF"))
                                showToast("❌ Unfollowed SwipeLaunch Team")
                            }
                            // Refresh the following activities
                            loadFollowingActivities()
                            // updateFollowingCountFromActivities() // OLD PROFILE FUNCTION - COMMENTED OUT
                        }
                        
                        featuredAddressCard.visibility = View.VISIBLE
                        android.util.Log.d("MainActivity", "Default featured address card shown")
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error setting up featured address: ${e.message}")
                android.util.Log.e("MainActivity", "Error details: ${e.stackTraceToString()}")
                
                // Show default even on error
                runOnUiThread {
                    featuredUserName.text = "SwipeLaunch Team"
                    featuredUserWallet.text = "umuA...mNu"
                    featuredUserStats.text = "🚀 Official account • ⭐ Featured creator"
                    featuredUserAvatar.setBackgroundColor(android.graphics.Color.parseColor("#9945FF"))
                    
                    val defaultWallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
                    var isFollowing = followedUsers.contains(defaultWallet)
                    followFeaturedButton.text = if (isFollowing) "Following" else "Follow"
                    followFeaturedButton.setBackgroundColor(android.graphics.Color.parseColor(if (isFollowing) "#4CAF50" else "#9945FF"))
                    
                    followFeaturedButton.setOnClickListener {
                        isFollowing = !isFollowing
                        if (isFollowing) {
                            followedUsers.add(defaultWallet)
                            followFeaturedButton.text = "Following"
                            followFeaturedButton.setBackgroundResource(R.drawable.follow_button_following)
                            followFeaturedButton.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                            showToast("✅ Now following SwipeLaunch Team")
                        } else {
                            followedUsers.remove(defaultWallet)
                            followFeaturedButton.text = "Follow"
                            followFeaturedButton.setBackgroundResource(R.drawable.follow_button_background)
                            followFeaturedButton.setTextColor(android.graphics.Color.parseColor("#9945FF"))
                            showToast("❌ Unfollowed SwipeLaunch Team")
                        }
                        // Refresh the following activities
                        loadFollowingActivities()
                            // updateFollowingCountFromActivities() // OLD PROFILE FUNCTION - COMMENTED OUT
                    }
                    
                    featuredAddressCard.visibility = View.VISIBLE
                    android.util.Log.d("MainActivity", "Error fallback featured address card shown")
                }
            }
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
        
        // Add URL image button
        findViewById<Button>(R.id.urlImageButton).setOnClickListener {
            android.util.Log.d("URLImageButton", "URL button clicked!")
            showUrlInputDialog()
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
        currentImageUrl = null
        selectedImage.visibility = View.GONE
        imagePlaceholder.visibility = View.VISIBLE
        removeImageButton.visibility = View.GONE
        selectedImage.setImageURI(null)
    }
    
    private fun showUrlInputDialog() {
        android.util.Log.d("URLImageButton", "showUrlInputDialog called")
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(16))
        }
        
        val urlInput = EditText(this).apply {
            hint = "Enter image URL (https://...)"
            setText(currentImageUrl ?: "")
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            background = resources.getDrawable(android.R.drawable.edit_text, null)
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }
        
        dialogView.addView(urlInput)
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Enter Image URL")
            .setView(dialogView)
            .setPositiveButton("Load") { _, _ ->
                val url = urlInput.text.toString().trim()
                if (url.isNotEmpty() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    loadImageFromUrl(url)
                } else {
                    showToast("Please enter a valid URL")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun loadImageFromUrl(url: String) {
        android.util.Log.d("URLImageButton", "loadImageFromUrl called with URL: $url")
        currentImageUrl = url
        currentImageUri = null // Clear any file URI
        
        // Use Glide to load the image
        try {
            com.bumptech.glide.Glide.with(this)
                .load(url)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.holo_red_light)
                .into(selectedImage)
            
            selectedImage.visibility = View.VISIBLE
            imagePlaceholder.visibility = View.GONE
            removeImageButton.visibility = View.VISIBLE
            
            showToast("✅ Image loaded from URL")
        } catch (e: Exception) {
            showToast("Failed to load image from URL")
            android.util.Log.e("ImageLoad", "Error loading image from URL: ${e.message}")
        }
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
        
        android.util.Log.d("TokenCreation", "STEP 1 Supply: $supply")
        
        // Token creation uses fixed creator address - no wallet connection required
        val FIXED_CREATOR_ADDRESS = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
        android.util.Log.d("TokenCreation", "STEP 1 Using fixed creator address: $FIXED_CREATOR_ADDRESS")
        
        // Get launch type
        val selectedRadioId = launchTypeGroup.checkedRadioButtonId
        android.util.Log.d("TokenCreation", "STEP 1 Selected radio ID: $selectedRadioId")
        android.util.Log.d("TokenCreation", "STEP 1 Presale radio ID: ${R.id.presaleLaunchRadio}")
        
        val launchType = when (selectedRadioId) {
            R.id.presaleLaunchRadio -> LaunchType.PRESALE
            else -> LaunchType.INSTANT
        }
        
        android.util.Log.d("TokenCreation", "STEP 1 Launch type: $launchType")
        
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
            
            android.util.Log.d("TokenCreation", "STEP 2: Simulation completed, creating result")
            
            // Mock successful creation
            val result = TokenCreationResult(
                success = true,
                tokenAddress = "TokenAddr${System.currentTimeMillis()}",
                transactionSignature = "TxSig${System.currentTimeMillis()}"
            )
            
            android.util.Log.d("TokenCreation", "=== STEP 3: Calling handleTokenCreationResult ===")
            handleTokenCreationResult(result)
        }
    }
    
    private fun handleTokenCreationResult(result: TokenCreationResult) {
        android.util.Log.d("TokenCreation", "=== STEP 3: handleTokenCreationResult called ===")
        android.util.Log.d("TokenCreation", "STEP 3 Result success: ${result.success}")
        
        createTokenButton.isEnabled = true
        createTokenButton.text = "🚀 Create Token"
        
        if (result.success) {
            android.util.Log.d("TokenCreation", "STEP 3: Processing successful result")
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
            
            val tokenStatus = if (launchType == LaunchType.PRESALE) "proposal" else "active"
            
            // Generate appropriate address based on type
            val displayAddress = if (launchType == LaunchType.PRESALE) {
                // For proposals, use transaction hash format (more realistic)
                val timestamp = System.currentTimeMillis().toString()
                val randomSuffix = (1000..9999).random()
                "s4y${timestamp.takeLast(4)}...${randomSuffix}xyz"
            } else {
                // For instant launch, use token address  
                result.tokenAddress ?: "TokenAddr${System.currentTimeMillis()}"
            }
            
            android.util.Log.d("TokenCreation", "STEP 3: Generated display address: $displayAddress")
            android.util.Log.d("TokenCreation", "STEP 3: Launch type: $launchType")
            android.util.Log.d("TokenCreation", "STEP 3: Token status: $tokenStatus")
            
            // Create token info for local caching
            val createdToken = CreatedTokenInfo(
                name = name,
                symbol = symbol,
                description = description,
                supply = supply,
                launchType = launchType,
                tokenAddress = displayAddress,
                chatLink = chatLink,
                imageUrl = currentImageUrl, // Save the image URL
                status = tokenStatus
            )
            
            // Add to both lists for immediate display
            createdTokens.add(0, createdToken) // Add to beginning of list
            creatorProfileManager.addNewToken(createdToken)
            android.util.Log.d("CreatorProfile", "Added token to creator profile: ${createdToken.name} (${createdToken.symbol})")
            
            // Refresh the recently created section
            if (profilePage.visibility == View.VISIBLE) {
                loadRecentlyCreatedTokens()
            }
            
            showToast("🚀 Token created and cached locally!")
            
            // Database sync disabled as requested - using local cache only
            
            /*
            // DATABASE SYNC DISABLED - using local cache only
            // Try to save to database in background
            lifecycleScope.launch {
                try {
                    val tokenRecord = TokenRecord(
                        token_name = name,
                        symbol = symbol,
                        description = description,
                        creator_wallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD", // Always use login wallet address
                        status = tokenStatus,
                        launch_price_sol = if (launchType == LaunchType.INSTANT) 0.05 else null
                    )
                    
                    val success = databaseService.insertToken(tokenRecord)
                    
                    if (success) {
                        android.util.Log.d("MyTokens", "Token also saved to database successfully")
                        runOnUiThread {
                            showToast("💾 Token synced to database")
                        }
                    } else {
                        android.util.Log.w("MyTokens", "Failed to save token to database, but cached locally")
                        runOnUiThread {
                            showToast("⚠️ Token cached locally (database sync failed)")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("MyTokens", "Error saving token to database: ${e.message}, but cached locally")
                    runOnUiThread {
                        showToast("⚠️ Token cached locally (database error)")
                    }
                }
            }
            */
            
            // Show appropriate success dialog immediately
            runOnUiThread {
                val title = if (launchType == LaunchType.PRESALE) "🗳️ Token Proposal Created!" else "🎉 Token Created!"
                val message = if (launchType == LaunchType.PRESALE) {
                    "Your token proposal has been created successfully!\n\nTransaction: $displayAddress\n\nIt will appear as a proposal in your profile. Once it gets enough community votes, it can be launched."
                } else {
                    "Your token has been created successfully!\n\nToken Address: $displayAddress"
                }
                val buttonText = if (launchType == LaunchType.PRESALE) "View Proposal in Profile" else "View in Profile"
                
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(buttonText) { _, _ ->
                        bottomNavigation.selectedItemId = R.id.nav_profile
                    }
                    .setNegativeButton("Create Another", null)
                    .show()
            }
                
            // Reset form
            resetCreateTokenForm()
        } else {
            showToast("❌ Failed to create token: ${result.errorMessage}")
        }
    }
    
    private fun resetCreateTokenForm() {
        android.util.Log.d("TokenCreation", "=== RESET: Clearing form and setting to proposal mode ===")
        
        tokenNameInput.text?.clear()
        tokenSymbolInput.text?.clear()
        tokenDescriptionInput.text?.clear()
        tokenChatLinkInput.text?.clear()
        tokenSupplySpinner.setSelection(0) // Reset to 1 Billion
        removeSelectedImage()
        
        // Ensure we're in proposal mode
        launchTypeGroup.clearCheck() // Clear any existing selection
        launchTypeGroup.check(R.id.presaleLaunchRadio) // Default to Propose Token
        updateUIForCommunityPresale() // Update UI to show proposal info
        
        android.util.Log.d("TokenCreation", "RESET: Form cleared, set to proposal mode")
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
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun setupMyTokens() {
        myTokensRecyclerView.layoutManager = LinearLayoutManager(this)
        myCreatedTokensRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // For debugging, make My Tokens always visible
        myTokensCard.visibility = View.VISIBLE
        myCreatedTokensCard.visibility = View.VISIBLE
        
        // Don't call updateMyTokensDisplay() during init - wallet manager isn't ready yet
        // It will be called when wallet manager is set up
    }
    */
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun updateMyTokensDisplay() {
        val connectedWallet = walletManager.getConnectedWalletAddress()
        val FIXED_CREATOR_ADDRESS = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
        
        android.util.Log.d("MyTokens", "updateMyTokensDisplay called")
        android.util.Log.d("MyTokens", "Connected wallet: $connectedWallet")
        android.util.Log.d("MyTokens", "Cached tokens count: ${createdTokens.size}")
        android.util.Log.d("MyTokens", "Fixed creator address: $FIXED_CREATOR_ADDRESS")
        
        // Always show creator tokens regardless of wallet connection status
        // Show locally cached tokens in "Recently Created" section
        if (createdTokens.isNotEmpty()) {
            android.util.Log.d("MyTokens", "Showing ${createdTokens.size} locally cached tokens")
            showMyCreatedTokensWithData(createdTokens)
        } else {
            showMyCreatedTokensEmptyState()
        }
        
        // Load database tokens from fixed creator for main "Creator Tokens" section
        // Use connected wallet address for context, but load by fixed creator
        lifecycleScope.launch {
            loadWalletTokensFromDatabase(connectedWallet ?: "no-wallet")
        }
    }
    */
    
    private suspend fun loadAndMergeWithCachedTokens(walletAddress: String) {
        try {
            android.util.Log.d("MyTokens", "Loading database tokens to merge with ${createdTokens.size} cached tokens")
            
            // Load database tokens using actual connected wallet
            android.util.Log.d("MyTokens", "Using connected wallet address: $walletAddress")
            
            // Get database tokens (created, voted, presale)
            val createdFromDb = databaseService.getTokensByCreator(walletAddress)
            val votedTokenIds = databaseService.getUserVotedTokenIds(walletAddress)
            val presaleTokenIds = databaseService.getUserPresaleTokenIds(walletAddress)
            val allDbTokens = databaseService.getAllTokens()
            val interactedTokens = allDbTokens.filter { token ->
                (votedTokenIds.contains(token.token_id) || presaleTokenIds.contains(token.token_id)) &&
                !createdFromDb.any { it.token_id == token.token_id }
            }
            
            val dbTokens = createdFromDb + interactedTokens
            
            // Convert database tokens to CreatedTokenInfo format
            val dbCreatedTokens = dbTokens.map { dbToken ->
                val isCreated = createdFromDb.any { it.token_id == dbToken.token_id }
                val isVoted = votedTokenIds.contains(dbToken.token_id)
                val isPresaleParticipant = presaleTokenIds.contains(dbToken.token_id)
                
                val status = when {
                    isCreated -> dbToken.status.replaceFirstChar { it.uppercase() }
                    isVoted && isPresaleParticipant -> "Liked & Invested"
                    isVoted -> "Liked"
                    isPresaleParticipant -> "Invested"
                    else -> dbToken.status
                }
                
                CreatedTokenInfo(
                    name = dbToken.token_name,
                    symbol = dbToken.symbol,
                    description = dbToken.description ?: if (isCreated) "Your created token" else "Token you've interacted with",
                    supply = 1_000_000_000L,
                    launchType = if (dbToken.status == "presale" || dbToken.status == "proposal") LaunchType.PRESALE else LaunchType.INSTANT,
                    tokenAddress = dbToken.token_mint_address ?: "Unknown",
                    chatLink = "https://discord.gg/${dbToken.symbol.lowercase()}",
                    imageUrl = dbToken.image_url, // Use database image URL
                    status = status
                )
            }
            
            // Merge cached tokens with database tokens (cached tokens take priority)
            val mergedTokens = (createdTokens + dbCreatedTokens).distinctBy { it.name + it.symbol }
            
            runOnUiThread {
                if (mergedTokens.size > createdTokens.size) {
                    // showMyTokensWithData(mergedTokens) // OLD FUNCTION - COMMENTED OUT
                    val newCount = mergedTokens.size - createdTokens.size
                    showToast("📊 Found ${newCount} additional tokens from database")
                    android.util.Log.d("MyTokens", "Merged: ${createdTokens.size} cached + ${newCount} from DB = ${mergedTokens.size} total")
                } else {
                    android.util.Log.d("MyTokens", "No additional tokens found in database")
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("MyTokens", "Error merging database tokens: ${e.message}")
            // Don't show error to user since cached tokens are already displayed
        }
    }
    
    private suspend fun loadWalletTokensFromDatabase(walletAddress: String) {
        try {
            // Fixed creator address - only show tokens created by this specific address
            val FIXED_CREATOR_ADDRESS = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
            
            android.util.Log.d("MyTokens", "=== DATABASE LOAD: Loading tokens created by fixed address: $FIXED_CREATOR_ADDRESS ===")
            android.util.Log.d("MyTokens", "Connected wallet: $walletAddress (for context only)")
            
            // Only get tokens created by the specific fixed creator address
            val createdTokens = databaseService.getTokensByCreator(FIXED_CREATOR_ADDRESS)
            android.util.Log.d("MyTokens", "Found ${createdTokens.size} tokens created by $FIXED_CREATOR_ADDRESS")
            
            // Use only the tokens created by the fixed creator (no user interactions)
            val walletTokens = createdTokens
            
            android.util.Log.d("MyTokens", "DATABASE LOAD: Total tokens from fixed creator: ${walletTokens.size}")
            
            walletTokens.forEachIndexed { index, token ->
                android.util.Log.d("MyTokens", "Creator Token $index: ${token.token_name} (${token.symbol}) - Status: ${token.status}")
            }
            
            if (walletTokens.isNotEmpty()) {
                android.util.Log.d("MyTokens", "DATABASE LOAD: Converting ${walletTokens.size} tokens to display format")
                
                // Convert to CreatedTokenInfo format for existing adapter
                val displayTokens = walletTokens.map { dbToken ->
                    android.util.Log.d("MyTokens", "Converting Creator Token: ${dbToken.token_name} - Status: ${dbToken.status}")
                    
                    CreatedTokenInfo(
                        name = dbToken.token_name,
                        symbol = dbToken.symbol,
                        description = dbToken.description ?: "Token by ${FIXED_CREATOR_ADDRESS.take(8)}...",
                        supply = 1_000_000_000L, // Default supply
                        launchType = if (dbToken.status == "presale" || dbToken.status == "proposal") LaunchType.PRESALE else LaunchType.INSTANT,
                        tokenAddress = dbToken.token_mint_address ?: "Unknown",
                        chatLink = "https://discord.gg/${dbToken.symbol.lowercase()}",
                        imageUrl = dbToken.image_url, // Use database image URL
                        status = dbToken.status.replaceFirstChar { it.uppercase() }
                    )
                }
                
                runOnUiThread {
                    // showMyTokensWithData(displayTokens) // OLD FUNCTION - COMMENTED OUT
                    showToast("🚀 Found ${walletTokens.size} tokens by creator ${FIXED_CREATOR_ADDRESS.take(8)}...")
                }
                
            } else {
                android.util.Log.d("MyTokens", "No tokens found from fixed creator")
                runOnUiThread {
                    // showMyTokensEmptyState() // OLD FUNCTION - COMMENTED OUT
                    showToast("💡 No tokens found from creator ${FIXED_CREATOR_ADDRESS.take(8)}...")
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("MyTokens", "Error loading wallet tokens: ${e.message}")
            runOnUiThread {
                // showMyTokensEmptyState() // OLD FUNCTION - COMMENTED OUT
                showToast("⚠️ Error loading your tokens")
            }
        }
    }
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun showMyTokensEmptyState() {
        val emptyState = profilePage.findViewById<LinearLayout>(R.id.myTokensEmptyState)
        myTokensRecyclerView.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }
    */
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun showMyTokensWithData(walletTokens: List<CreatedTokenInfo>) {
        android.util.Log.d("MyTokens", "showMyTokensWithData called with ${walletTokens.size} tokens")
        walletTokens.forEachIndexed { index, token ->
            android.util.Log.d("MyTokens", "Token $index: ${token.name} (${token.symbol}) - ${token.status}")
        }
        
        val emptyState = profilePage.findViewById<LinearLayout>(R.id.myTokensEmptyState)
        myTokensRecyclerView.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        
        val adapter = MyTokensAdapter(walletTokens) { token ->
            showTokenDetails(token)
        }
        myTokensRecyclerView.adapter = adapter
        
        android.util.Log.d("MyTokens", "Successfully displayed ${walletTokens.size} wallet tokens")
    }
    */
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun showMyCreatedTokensEmptyState() {
        myCreatedTokensRecyclerView.visibility = View.GONE
        myCreatedTokensEmptyState.visibility = View.VISIBLE
        android.util.Log.d("MyTokens", "Showing empty state for created tokens")
    }
    */
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun showMyCreatedTokensWithData(createdTokensList: List<CreatedTokenInfo>) {
        android.util.Log.d("MyTokens", "showMyCreatedTokensWithData called with ${createdTokensList.size} tokens")
        createdTokensList.forEachIndexed { index, token ->
            android.util.Log.d("MyTokens", "Created Token $index: ${token.name} (${token.symbol}) - ${token.status}")
        }
        
        myCreatedTokensRecyclerView.visibility = View.VISIBLE
        myCreatedTokensEmptyState.visibility = View.GONE
        
        val adapter = MyTokensAdapter(createdTokensList) { token ->
            showTokenDetails(token)
        }
        myCreatedTokensRecyclerView.adapter = adapter
        
        android.util.Log.d("MyTokens", "Successfully displayed ${createdTokensList.size} created tokens")
    }
    */
    
    // OLD PROFILE FUNCTION - COMMENTED OUT (replaced by CreatorProfileManager)
    /*
    private fun updateActivitySummary(walletAddress: String?) {
        // Use the actual connected wallet address or return early if none
        val connectedWallet = walletAddress ?: walletManager.getConnectedWalletAddress()
        if (connectedWallet == null) {
            android.util.Log.d("ActivitySummary", "No wallet connected, skipping summary update")
            return
        }
        
        lifecycleScope.launch {
            try {
                // Get likes given (votes by this wallet)
                val votedTokenIds = databaseService.getUserVotedTokenIds(connectedWallet)
                profileLikesCount.text = votedTokenIds.size.toString()
                
                // Get presales joined  
                val presaleTokenIds = databaseService.getUserPresaleTokenIds(connectedWallet)
                profilePresalesCount.text = presaleTokenIds.size.toString()
                
                // Following count - for now just a placeholder
                // In a real app, you'd have a follows table
                profileFollowingCount.text = "0"
                
                android.util.Log.d("ActivitySummary", "Updated for wallet $connectedWallet: Likes=${votedTokenIds.size}, Presales=${presaleTokenIds.size}")
                
            } catch (e: Exception) {
                android.util.Log.e("ActivitySummary", "Error updating activity summary: ${e.message}")
                // Set default values on error
                profileLikesCount.text = "0"
                profilePresalesCount.text = "0"
                profileFollowingCount.text = "0"
            }
        }
    }
    */
    
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
    
    private fun updateWalletBalanceDisplay() {
        walletBalanceText.text = "${String.format("%.2f", currentWalletBalance)} SOL"
    }
    
    private fun deductBalance(amount: Double): Boolean {
        return if (currentWalletBalance >= amount) {
            currentWalletBalance -= amount
            updateWalletBalanceDisplay()
            true
        } else {
            false
        }
    }
    
    private fun setupCollapsibleProposalGallery() {
        val header = profilePage.findViewById<LinearLayout>(R.id.proposalGalleryHeader)
        val content = profilePage.findViewById<LinearLayout>(R.id.proposalGalleryContent)
        val expandIcon = profilePage.findViewById<TextView>(R.id.expandCollapseIcon)
        
        header?.setOnClickListener {
            if (content?.visibility == View.VISIBLE) {
                content.visibility = View.GONE
                expandIcon?.text = "▶"
            } else {
                content?.visibility = View.VISIBLE
                expandIcon?.text = "▼"
            }
        }
    }
    
    private fun loadRecentlyCreatedTokens() {
        val recentTokensContainer = profilePage.findViewById<LinearLayout>(R.id.recentTokensContainer)
        val emptyState = profilePage.findViewById<TextView>(R.id.recentTokensEmptyState)
        
        recentTokensContainer?.removeAllViews()
        
        // Get recently created tokens (last 5)
        val recentTokens = createdTokens.takeLast(5).reversed()
        
        if (recentTokens.isEmpty()) {
            emptyState?.visibility = View.VISIBLE
            recentTokensContainer?.visibility = View.GONE
        } else {
            emptyState?.visibility = View.GONE
            recentTokensContainer?.visibility = View.VISIBLE
            
            // Create token cards for each recent token
            recentTokens.forEach { token: CreatedTokenInfo ->
                val tokenCard = createRecentTokenCard(token)
                recentTokensContainer?.addView(tokenCard)
            }
        }
    }
    
    private fun createRecentTokenCard(token: CreatedTokenInfo): View {
        val cardView = androidx.cardview.widget.CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(120),
                dpToPx(140)
            ).apply {
                marginEnd = dpToPx(12)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(2).toFloat()
        }
        
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
        }
        
        // Token Icon
        val tokenIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(60), dpToPx(60))
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // Load image from URL if available, otherwise use colored background
            if (!token.imageUrl.isNullOrEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(this@MainActivity)
                        .load(token.imageUrl)
                        .placeholder(android.R.color.darker_gray)
                        .error(android.R.color.holo_red_light)
                        .circleCrop() // Make it circular like other tokens
                        .into(this)
                } catch (e: Exception) {
                    // Fallback to colored background if image loading fails
                    val colors = listOf("#9945FF", "#14B8A6", "#FF6B35", "#34C759", "#007AFF")
                    val colorIndex = kotlin.math.abs(token.name.hashCode()) % colors.size
                    setBackgroundColor(android.graphics.Color.parseColor(colors[colorIndex]))
                    android.util.Log.e("RecentTokenCard", "Failed to load image: ${e.message}")
                }
            } else {
                // Set colored background based on token name as fallback
                val colors = listOf("#9945FF", "#14B8A6", "#FF6B35", "#34C759", "#007AFF")
                val colorIndex = kotlin.math.abs(token.name.hashCode()) % colors.size
                setBackgroundColor(android.graphics.Color.parseColor(colors[colorIndex]))
            }
        }
        
        // Token Name
        val tokenName = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            text = token.name
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#1C1C1E"))
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = android.view.Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        
        // Token Symbol
        val tokenSymbol = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = token.symbol
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#8E8E93"))
            gravity = android.view.Gravity.CENTER
        }
        
        // Status Badge
        val statusBadge = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
            text = token.status
            textSize = 10f
            setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2))
            gravity = android.view.Gravity.CENTER
            
            when (token.status) {
                "Active" -> {
                    setBackgroundColor(android.graphics.Color.parseColor("#34C75920"))
                    setTextColor(android.graphics.Color.parseColor("#34C759"))
                }
                "Voting" -> {
                    setBackgroundColor(android.graphics.Color.parseColor("#FF6B3520"))
                    setTextColor(android.graphics.Color.parseColor("#FF6B35"))
                }
                else -> {
                    setBackgroundColor(android.graphics.Color.parseColor("#8E8E9320"))
                    setTextColor(android.graphics.Color.parseColor("#8E8E93"))
                }
            }
        }
        
        linearLayout.addView(tokenIcon)
        linearLayout.addView(tokenName)
        linearLayout.addView(tokenSymbol)
        linearLayout.addView(statusBadge)
        
        cardView.addView(linearLayout)
        
        // Add click listener
        cardView.setOnClickListener {
            showToast("View ${token.name} details")
        }
        
        return cardView
    }
    
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun setupProfilePage() {
        // Set up wallet connection button
        profileWalletButton.setOnClickListener {
            if (walletManager.isWalletConnected()) {
                // When connected, just show a toast - disconnect is handled by Sign Out button
                showToast("Wallet is connected and ready to use")
            } else {
                // Connect wallet
                lifecycleScope.launch {
                    try {
                        walletManager.connectWallet()
                    } catch (e: Exception) {
                        showToast("Connection failed: ${e.message}")
                    }
                }
            }
        }
        
        
        // Set up disconnect wallet button (in profile details)
        disconnectWalletButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    walletManager.disconnectWallet()
                } catch (e: Exception) {
                    showToast("Disconnection failed: ${e.message}")
                }
            }
        }
        
        // Set up airdrop button
        requestAirdropButton.setOnClickListener {
            if (walletManager.isWalletConnected()) {
                lifecycleScope.launch {
                    try {
                        showToast("Requesting airdrop...")
                        walletManager.requestAirdrop()
                    } catch (e: Exception) {
                        showToast("Airdrop failed: ${e.message}")
                    }
                }
            } else {
                showToast("Please connect your wallet first")
            }
        }
        
        // Set up copy address button
        copyAddressButton.setOnClickListener {
            val address = walletManager.getConnectedWalletAddress()
            if (address != null) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Wallet Address", address)
                clipboard.setPrimaryClip(clip)
                showToast("Address copied to clipboard")
            } else {
                showToast("No wallet connected")
            }
        }
        
        // Initialize UI state
        updateProfileUI(false, null, null)
    }
    
    private fun updateProfileUI(connected: Boolean, fullAddress: String?, displayAddress: String?) {
        if (connected && fullAddress != null && displayAddress != null) {
            // Wallet connected - hide connection card and show profile details
            walletConnectionCard.visibility = View.GONE
            profileDetailsCard.visibility = View.VISIBLE
            activityCard.visibility = View.VISIBLE
            disconnectWalletButton.visibility = View.VISIBLE
            
            // Update profile info - show wallet address (crypto-native style)
            val loginWallet = "W97AHbiw4WJ5RxCMTVD9UKwfesgM5qpNhXufw6tgwfsD"
            profileUserName.text = "${loginWallet.take(6)}...${loginWallet.takeLast(6)}"
            profileWalletAddress.text = displayAddress
            
            // Set default activity stats with random realistic numbers
            val randomLikes = (15..45).random()
            val randomPresales = (3..8).random()
            val randomFollowing = (5..15).random()
            val randomFollowers = (25..85).random()
            
            findViewById<TextView>(R.id.profileLikesCount).text = randomLikes.toString()
            findViewById<TextView>(R.id.profilePresalesCount).text = randomPresales.toString()
            findViewById<TextView>(R.id.profileFollowingCount).text = randomFollowing.toString()
            findViewById<TextView>(R.id.profileFollowersCount).text = randomFollowers.toString()
            
            // Set proposal status stats - fixed values
            findViewById<TextView>(R.id.proposalLikesReceived).text = "380"
            findViewById<TextView>(R.id.proposalInPresale).text = "4"
            findViewById<TextView>(R.id.proposalLaunched).text = "1"
            
            // Set fixed SOL balance
            profileSolBalance.text = "13.72 SOL"
            
            // Note: My tokens display is handled by CreatorProfileManager
        } else {
            // Wallet disconnected - show connection card and hide profile details
            walletConnectionCard.visibility = View.VISIBLE
            profileWalletButton.text = "Connect Solana Wallet"
            walletStatusText.text = "Connect your wallet to see your profile details"
            profileDetailsCard.visibility = View.GONE
            activityCard.visibility = View.GONE
            disconnectWalletButton.visibility = View.GONE
        }
    }
    
    private fun loadProfileAvatar(walletAddress: String) {
        val avatarUrl = "https://api.dicebear.com/9.x/thumbs/svg?seed=${walletAddress}"
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load SVG from URL
                val inputStream = URL(avatarUrl).openStream()
                val svg = SVG.getFromInputStream(inputStream)
                
                // Create a bitmap from the SVG with higher resolution for profile
                val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                svg.renderToCanvas(canvas)
                
                withContext(Dispatchers.Main) {
                    // Set the bitmap to the profile avatar ImageView
                    val drawable = BitmapDrawable(profileAvatar.context.resources, bitmap)
                    profileAvatar.setImageDrawable(drawable)
                    profileAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
                    android.util.Log.d("ProfileAvatar", "Successfully loaded profile avatar for wallet: ${walletAddress.take(8)}...")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Keep the gradient background as fallback
                    android.util.Log.e("ProfileAvatar", "Error loading profile avatar: ${e.message}")
                }
            }
        }
    }
}