package com.anonymous.SolanaMobileApp

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MyTokensAdapter(
    private val tokens: List<CreatedTokenInfo>,
    private val onViewClick: (CreatedTokenInfo) -> Unit
) : RecyclerView.Adapter<MyTokensAdapter.TokenViewHolder>() {

    class TokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tokenIcon: ImageView = itemView.findViewById(R.id.tokenIcon)
        val tokenName: TextView = itemView.findViewById(R.id.tokenName)
        val tokenSymbol: TextView = itemView.findViewById(R.id.tokenSymbol)
        val tokenSupply: TextView = itemView.findViewById(R.id.tokenSupply)
        val tokenStatus: TextView = itemView.findViewById(R.id.tokenStatus)
        val viewTokenButton: Button = itemView.findViewById(R.id.viewTokenButton)
        val chatLinkButton: Button = itemView.findViewById(R.id.chatLinkButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_token_item, parent, false)
        return TokenViewHolder(view)
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        val token = tokens[position]
        
        // Create a simple initial-based icon with colored background
        val initials = token.name.take(2).uppercase()
        android.util.Log.d("MyTokensAdapter", "Setting up token: ${token.name}, creating initials: $initials")
        
        // Set colored background based on token name hash
        val colors = listOf("#9945FF", "#14B8A6", "#FF6B35", "#34C759", "#007AFF")
        val colorIndex = kotlin.math.abs(token.name.hashCode()) % colors.size
        val backgroundColor = android.graphics.Color.parseColor(colors[colorIndex])
        
        // Create a drawable with the initial letter
        holder.tokenIcon.setBackgroundColor(backgroundColor)
        holder.tokenIcon.scaleType = ImageView.ScaleType.CENTER
        
        // Try to load SVG as overlay, but show colored background immediately
        val iconUrl = "https://api.dicebear.com/9.x/thumbs/svg?seed=${token.name}"
        loadSvgImage(iconUrl, holder.tokenIcon)
        
        // Set token info
        holder.tokenName.text = token.name
        holder.tokenSymbol.text = token.symbol
        holder.tokenSupply.text = formatSupply(token.supply)
        holder.tokenStatus.text = token.status
        
        // Set iOS-style status colors
        when (token.status) {
            "Active" -> {
                holder.tokenStatus.setBackgroundResource(R.drawable.ios_status_badge)
                holder.tokenStatus.setTextColor(0xFF34C759.toInt())
            }
            "Voting" -> {
                holder.tokenStatus.setBackgroundColor(0xFFFFE8E8.toInt())
                holder.tokenStatus.setTextColor(0xFFDC2626.toInt())
            }
            "Presale" -> {
                holder.tokenStatus.setBackgroundColor(0xFFF59E0B20.toInt())
                holder.tokenStatus.setTextColor(0xFFD97706.toInt())
            }
        }
        
        // Handle chat link button
        if (!token.chatLink.isNullOrEmpty()) {
            holder.chatLinkButton.visibility = View.VISIBLE
            holder.chatLinkButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(token.chatLink))
                try {
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(holder.itemView.context, "Cannot open chat link", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.chatLinkButton.visibility = View.GONE
        }
        
        // Handle view button
        holder.viewTokenButton.setOnClickListener {
            onViewClick(token)
        }
    }

    override fun getItemCount() = tokens.size
    
    private fun formatSupply(supply: Long): String {
        return when {
            supply >= 1_000_000_000 -> "${supply / 1_000_000_000}B"
            supply >= 1_000_000 -> "${supply / 1_000_000}M"
            supply >= 1_000 -> "${supply / 1_000}K"
            else -> supply.toString()
        }
    }
    
    private fun loadSvgImage(svgUrl: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("MyTokensAdapter", "Loading SVG from: $svgUrl")
                
                // Load SVG from URL
                val inputStream = URL(svgUrl).openStream()
                val svg = SVG.getFromInputStream(inputStream)
                
                // Create a bitmap from the SVG with white background
                val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                
                // Fill with white background first
                canvas.drawColor(android.graphics.Color.WHITE)
                
                // Render SVG
                svg.renderToCanvas(canvas)
                
                withContext(Dispatchers.Main) {
                    // Clear any background and set the bitmap
                    imageView.background = null
                    val drawable = BitmapDrawable(imageView.context.resources, bitmap)
                    imageView.setImageDrawable(drawable)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    android.util.Log.d("MyTokensAdapter", "Successfully loaded SVG image")
                }
            } catch (e: Exception) {
                android.util.Log.e("MyTokensAdapter", "Failed to load SVG: ${e.message}")
                withContext(Dispatchers.Main) {
                    // Clear any previous image and set colorful background
                    imageView.setImageDrawable(null)
                    imageView.setBackgroundResource(R.drawable.gradient_solana_bg)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }
        }
    }
}