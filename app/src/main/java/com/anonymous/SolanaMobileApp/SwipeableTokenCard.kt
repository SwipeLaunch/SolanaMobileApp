package com.anonymous.SolanaMobileApp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
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
    private lateinit var imagePlaceholder: View
    private lateinit var likeOverlay: View
    private lateinit var passOverlay: View

    companion object {
        private const val SWIPE_THRESHOLD = 300f
        private const val ROTATION_FACTOR = 0.1f
        private const val ALPHA_FACTOR = 0.002f
    }

    interface OnSwipeListener {
        fun onSwipeRight(card: SwipeableTokenCard)
        fun onSwipeLeft(card: SwipeableTokenCard)
    }

    init {
        // Inflate the card layout
        inflate(context, R.layout.swipeable_token_card, this)
        
        // Initialize views
        tokenName = findViewById(R.id.tokenName)
        creator = findViewById(R.id.creator)
        description = findViewById(R.id.description)
        likes = findViewById(R.id.likes)
        imagePlaceholder = findViewById(R.id.imagePlaceholder)
        likeOverlay = findViewById(R.id.likeOverlay)
        passOverlay = findViewById(R.id.passOverlay)

        // Set up gesture detector
        gestureDetector = GestureDetector(context, SwipeGestureListener())
    }

    fun setTokenData(token: TokenData) {
        tokenName.text = token.name
        creator.text = "by @${token.creator}"
        description.text = token.description
        likes.text = "❤️ ${token.likes}"
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
            
            // Only handle horizontal swipes
            if (abs(deltaX) > abs(deltaY)) {
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
            
            if (abs(velocityX) > 1000 || abs(deltaX) > SWIPE_THRESHOLD) {
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
        
        // Rotate card
        rotation = deltaX * ROTATION_FACTOR
        
        // Update overlays
        val normalizedX = deltaX / SWIPE_THRESHOLD
        
        if (deltaX > 0) {
            // Swiping right - show like
            likeOverlay.alpha = min(1f, normalizedX)
            passOverlay.alpha = 0f
        } else {
            // Swiping left - show pass
            passOverlay.alpha = min(1f, -normalizedX)
            likeOverlay.alpha = 0f
        }
    }

    private fun swipeRight() {
        isAnimating = true
        val animator = ObjectAnimator.ofFloat(this, "translationX", translationX, width * 2f)
        animator.duration = 300
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
        animator.duration = 300
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onSwipeListener?.onSwipeLeft(this@SwipeableTokenCard)
                isAnimating = false
            }
        })
        animator.start()
    }

    private fun resetCard() {
        if (isAnimating) return
        
        animate()
            .translationX(0f)
            .rotation(0f)
            .setDuration(200)
            .start()
            
        likeOverlay.animate().alpha(0f).setDuration(200).start()
        passOverlay.animate().alpha(0f).setDuration(200).start()
    }
}