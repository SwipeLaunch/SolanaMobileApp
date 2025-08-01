package com.anonymous.SolanaMobileApp

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var swipeContainer: FrameLayout
    private val tokenCards = mutableListOf<SwipeableTokenCard>()
    private var currentCardIndex = 0
    
    // Sample token data
    private val tokens = listOf(
        TokenData("1", "MOON Token", "cryptoking", "🚀 To the moon! The next big thing in DeFi. Diamond hands only! 💎🙌", 1247),
        TokenData("2", "DOGE 2.0", "memeLord", "🐕 Much wow, very token! The evolution of meme coins on Solana blockchain.", 856),
        TokenData("3", "SOL CAT", "catWhisperer", "🐱 Purr-fect token for cat lovers! Meow your way to financial freedom with SOL CAT.", 2103),
        TokenData("4", "LAMBO Token", "speedDemon", "🏎️ When lambo? NOW! The token that will actually get you that lambo. Built different.", 3421),
        TokenData("5", "DIAMOND", "gemHunter", "💎 Unbreakable hands, unbreakable token. DIAMOND is forever on Solana blockchain.", 567)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupBottomNavigation()
        setupSwipeCards()
    }
    
    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        swipeContainer = findViewById(R.id.swipeContainer)
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
                    showToast("Profile Tab")
                    true
                }
                else -> false
            }
        }
        
        // Select discover tab by default
        bottomNavigation.selectedItemId = R.id.nav_discover
    }
    
    private fun setupSwipeCards() {
        // Create cards for first 3 tokens (stack of 3)
        for (i in 0 until minOf(3, tokens.size)) {
            createAndAddCard(i)
        }
    }
    
    private fun createAndAddCard(tokenIndex: Int) {
        if (tokenIndex >= tokens.size) return
        
        val card = SwipeableTokenCard(this)
        card.setTokenData(tokens[tokenIndex])
        card.setOnSwipeListener(object : SwipeableTokenCard.OnSwipeListener {
            override fun onSwipeRight(card: SwipeableTokenCard) {
                handleSwipeRight(card, tokenIndex)
            }
            
            override fun onSwipeLeft(card: SwipeableTokenCard) {
                handleSwipeLeft(card, tokenIndex)
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
        
        // Add next card if available
        val nextTokenIndex = tokenIndex + 3 // Skip ahead by stack size
        if (nextTokenIndex < tokens.size) {
            createAndAddCard(nextTokenIndex)
        }
        
        // Update current index
        currentCardIndex++
        
        // If no more cards, show completion message
        if (tokenCards.isEmpty()) {
            showToast("🎉 You've seen all tokens! Great job!")
            // Optionally reset or load more tokens
            setupSwipeCards() // Reset for demo
        }
    }
    
    private fun showDiscoverTab() {
        findViewById<View>(R.id.discoverContent).visibility = View.VISIBLE
        // Hide other tab content when implemented
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}