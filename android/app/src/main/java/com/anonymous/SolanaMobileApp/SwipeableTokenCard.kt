package com.anonymous.SolanaMobileApp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SwipeableTokenCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var onSwipeListener: OnSwipeListener? = null
    private val gestureDetector: GestureDetector
    private var isAnimating = false
    
    // UI elements
    private lateinit var tokenName: TextView
    private lateinit var creator: TextView
    private lateinit var description: TextView
    private lateinit var likes: TextView
    private lateinit var tokenImage: ImageView
    private lateinit var likeOverlay: View
    private lateinit var passOverlay: View
    private lateinit var likeButton: Button
    private lateinit var dislikeButton: Button
    private lateinit var infoButton: Button
    
    // Card flip views
    private lateinit var frontCard: View
    private lateinit var backCard: View
    private var isFlipped = false
    
    // Creator info views
    private lateinit var creatorNameBack: TextView
    private lateinit var solanaHandleBack: TextView
    private lateinit var slTokenStakedBack: TextView
    private lateinit var totalLikesBack: TextView
    private lateinit var tokensLaunchedBack: TextView
    private lateinit var walletAddressBack: TextView
    private lateinit var twitterHandleBack: TextView
    private lateinit var communityButton: Button
    private lateinit var backButton: Button

    companion object {
        private const val SWIPE_THRESHOLD = 60f   // VERY easy to swipe - just 60px
        private const val VELOCITY_THRESHOLD = 400f  // Very low velocity needed
        private const val ROTATION_FACTOR = 0.03f  // Reduced rotation for smoother animation
        private const val ALPHA_FACTOR = 0.008f
    }

    interface OnSwipeListener {
        fun onSwipeRight(card: SwipeableTokenCard)
        fun onSwipeLeft(card: SwipeableTokenCard)
        fun onLikeButton(card: SwipeableTokenCard)
        fun onDislikeButton(card: SwipeableTokenCard)
    }

    init {
        // Inflate the card layout
        inflate(context, R.layout.swipeable_token_card, this)
        
        // Initialize front/back views
        frontCard = findViewById(R.id.frontCard)
        backCard = findViewById(R.id.backCard)
        
        // Initialize front card views
        tokenName = frontCard.findViewById(R.id.tokenName)
        creator = frontCard.findViewById(R.id.creator)
        description = frontCard.findViewById(R.id.description)
        likes = frontCard.findViewById(R.id.likes)
        tokenImage = frontCard.findViewById(R.id.tokenImage)
        likeButton = frontCard.findViewById(R.id.likeButton)
        dislikeButton = frontCard.findViewById(R.id.dislikeButton)
        infoButton = frontCard.findViewById(R.id.infoButton)
        
        // Initialize overlays
        likeOverlay = findViewById(R.id.likeOverlay)
        passOverlay = findViewById(R.id.passOverlay)
        
        // Initialize back card views
        creatorNameBack = backCard.findViewById(R.id.creatorName)
        solanaHandleBack = backCard.findViewById(R.id.solanaHandle)
        slTokenStakedBack = backCard.findViewById(R.id.slTokenStaked)
        totalLikesBack = backCard.findViewById(R.id.totalLikes)
        tokensLaunchedBack = backCard.findViewById(R.id.tokensLaunched)
        walletAddressBack = backCard.findViewById(R.id.walletAddress)
        twitterHandleBack = backCard.findViewById(R.id.twitterHandle)
        communityButton = backCard.findViewById(R.id.communityButton)
        backButton = backCard.findViewById(R.id.backButton)

        // Set up button listeners
        likeButton.setOnClickListener { likeAction() }
        dislikeButton.setOnClickListener { dislikeAction() }
        infoButton.setOnClickListener { flipCard() }
        backButton.setOnClickListener { flipCard() }
        
        // Allow clicking anywhere on the back card to flip back to front
        backCard.setOnClickListener { 
            if (isFlipped) {
                flipCard()
            }
        }

        // Set up gesture detector
        gestureDetector = GestureDetector(context, SwipeGestureListener())
    }

    fun setTokenData(token: TokenData) {
        // Set front card data
        tokenName.text = token.name
        creator.text = "by @${token.creator}"
        description.text = token.description
        likes.text = "❤️ ${token.likes}"
        
        // Load token image
        android.util.Log.d("SwipeableTokenCard", "Setting token data for: ${token.name}")
        android.util.Log.d("SwipeableTokenCard", "Image URL: ${token.imageUrl}")
        
        if (!token.imageUrl.isNullOrEmpty()) {
            android.util.Log.d("SwipeableTokenCard", "Loading image from URL: ${token.imageUrl}")
            // Check if it's an SVG image
            if (token.imageUrl.contains(".svg") || token.imageUrl.contains("/svg?")) {
                android.util.Log.d("SwipeableTokenCard", "Detected SVG image, using custom loader")
                // Load SVG using custom loader
                loadSvgImage(token.imageUrl)
            } else {
                android.util.Log.d("SwipeableTokenCard", "Loading regular image with Glide")
                // Load regular image using Glide
                Glide.with(context)
                    .load(token.imageUrl)
                    .centerCrop()
                    .placeholder(R.color.light_gray)
                    .error(R.color.light_gray)
                    .into(tokenImage)
            }
        } else {
            android.util.Log.d("SwipeableTokenCard", "No image URL found, setting placeholder")
            // Set default placeholder if no image URL
            tokenImage.setBackgroundColor(context.getColor(android.R.color.darker_gray))
        }
        
        // Set back card data
        creatorNameBack.text = "@${token.creator}"
        solanaHandleBack.text = token.creatorSolanaHandle
        slTokenStakedBack.text = token.slTokenStaked.toString()
        totalLikesBack.text = token.totalLikesReceived.toString()
        tokensLaunchedBack.text = token.tokensLaunched.toString()
        walletAddressBack.text = "🔑 ${token.creatorWallet}"
        twitterHandleBack.text = "🐦 @${token.creatorTwitter}"
        
        // Set up community button
        communityButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(token.communityLink))
            context.startActivity(intent)
        }
    }

    fun setOnSwipeListener(listener: OnSwipeListener) {
        this.onSwipeListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isAnimating) return true
        
        gestureDetector.onTouchEvent(event)
        
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            resetCard()
        }
        
        return true
    }

    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isAnimating) return false
            
            val deltaX = e2.x - (e1?.x ?: 0f)
            val deltaY = e2.y - (e1?.y ?: 0f)
            
            // Handle any swipe that has horizontal movement (more permissive)
            if (abs(deltaX) > 10f) {  // Very small threshold for horizontal detection
                updateCardPosition(deltaX)
                return true
            }
            
            return false
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (isAnimating) return false
            
            val deltaX = e2.x - (e1?.x ?: 0f)
            
            if (abs(velocityX) > VELOCITY_THRESHOLD || abs(deltaX) > SWIPE_THRESHOLD) {
                if (deltaX > 0) {
                    swipeRight()
                } else {
                    swipeLeft()
                }
                return true
            }
            
            return false
        }
    }

    private fun updateCardPosition(deltaX: Float) {
        // Move card
        translationX = deltaX
        
        // Rotate card (reduced for smoother animation)
        rotation = deltaX * ROTATION_FACTOR
        
        // Update overlays with better scaling
        val normalizedX = abs(deltaX) / SWIPE_THRESHOLD
        val alpha = min(1f, normalizedX * 1.2f) // Make overlays appear faster
        
        if (deltaX > 0) {
            // Swiping right - show like overlay
            likeOverlay.alpha = alpha
            passOverlay.alpha = 0f
            likeOverlay.scaleX = 0.8f + (alpha * 0.2f) // Scale animation
            likeOverlay.scaleY = 0.8f + (alpha * 0.2f)
        } else {
            // Swiping left - show pass overlay  
            passOverlay.alpha = alpha
            likeOverlay.alpha = 0f
            passOverlay.scaleX = 0.8f + (alpha * 0.2f) // Scale animation
            passOverlay.scaleY = 0.8f + (alpha * 0.2f)
        }
    }

    private fun swipeRight() {
        isAnimating = true
        val animator = ObjectAnimator.ofFloat(this, "translationX", translationX, width * 2f)
        animator.duration = 200  // Faster animation
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onSwipeListener?.onSwipeRight(this@SwipeableTokenCard)
                isAnimating = false
            }
        })
        animator.start()
    }

    private fun swipeLeft() {
        isAnimating = true
        val animator = ObjectAnimator.ofFloat(this, "translationX", translationX, -width * 2f)
        animator.duration = 200  // Faster animation
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onSwipeListener?.onSwipeLeft(this@SwipeableTokenCard)
                isAnimating = false
            }
        })
        animator.start()
    }

    fun likeAction() {
        if (isAnimating) return
        swipeRight()  // Trigger the same animation as swiping right
    }

    fun dislikeAction() {
        if (isAnimating) return
        swipeLeft()  // Trigger the same animation as swiping left
    }

    private fun resetCard() {
        if (isAnimating) return
        
        animate()
            .translationX(0f)
            .rotation(0f)
            .setDuration(150)  // Faster reset
            .start()
            
        // Reset overlays with scale
        likeOverlay.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(150)
            .start()
        
        passOverlay.animate()
            .alpha(0f)
            .scaleX(0.8f) 
            .scaleY(0.8f)
            .setDuration(150)
            .start()
    }
    
    private fun flipCard() {
        if (isAnimating) return
        
        val currentCard = if (isFlipped) backCard else frontCard
        val nextCard = if (isFlipped) frontCard else backCard
        
        // First half of flip - hide current card
        currentCard.animate()
            .scaleX(0f)
            .setDuration(150)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentCard.visibility = View.GONE
                    nextCard.visibility = View.VISIBLE
                    nextCard.scaleX = 0f
                    
                    // Second half of flip - show next card
                    nextCard.animate()
                        .scaleX(1f)
                        .setDuration(150)
                        .setListener(null)
                        .start()
                }
            })
            .start()
            
        isFlipped = !isFlipped
    }
    
    private fun loadSvgImage(svgUrl: String) {
        android.util.Log.d("SwipeableTokenCard", "Starting SVG load for URL: $svgUrl")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("SwipeableTokenCard", "Opening input stream for SVG")
                // Load SVG from URL
                val inputStream = URL(svgUrl).openStream()
                android.util.Log.d("SwipeableTokenCard", "Parsing SVG from input stream")
                val svg = SVG.getFromInputStream(inputStream)
                
                android.util.Log.d("SwipeableTokenCard", "Creating bitmap for SVG rendering")
                // Create a bitmap from the SVG
                val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                android.util.Log.d("SwipeableTokenCard", "Rendering SVG to canvas")
                svg.renderToCanvas(canvas)
                
                withContext(Dispatchers.Main) {
                    android.util.Log.d("SwipeableTokenCard", "Setting SVG bitmap to ImageView")
                    // Set the bitmap to the ImageView
                    val drawable = BitmapDrawable(context.resources, bitmap)
                    tokenImage.setImageDrawable(drawable)
                    tokenImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    android.util.Log.d("SwipeableTokenCard", "SVG image successfully loaded!")
                }
            } catch (e: Exception) {
                android.util.Log.e("SwipeableTokenCard", "Error loading SVG: ${e.message}")
                android.util.Log.e("SwipeableTokenCard", "SVG error details: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    // Set placeholder on error
                    tokenImage.setBackgroundColor(context.getColor(R.color.light_gray))
                    android.util.Log.d("SwipeableTokenCard", "Set error placeholder for SVG")
                }
            }
        }
    }
}