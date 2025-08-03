package com.anonymous.SolanaMobileApp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

data class LikedTokenInfo(
    val id: Int,
    val name: String,
    val symbol: String,
    val status: String,
    val description: String?,
    val creator: String,
    val logoUrl: String = ""
)

class LikedTokenAdapter(
    private val likedTokens: List<LikedTokenInfo>,
    private val onInfoClick: (LikedTokenInfo) -> Unit
) : RecyclerView.Adapter<LikedTokenAdapter.LikedTokenViewHolder>() {

    class LikedTokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tokenIcon: ImageView = itemView.findViewById(R.id.tokenIcon)
        val tokenName: TextView = itemView.findViewById(R.id.tokenName)
        val tokenSymbol: TextView = itemView.findViewById(R.id.tokenSymbol)
        val tokenStatus: TextView = itemView.findViewById(R.id.tokenStatus)
        val infoButton: Button = itemView.findViewById(R.id.infoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedTokenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.liked_token_item, parent, false)
        return LikedTokenViewHolder(view)
    }

    override fun onBindViewHolder(holder: LikedTokenViewHolder, position: Int) {
        val token = likedTokens[position]
        
        holder.tokenName.text = token.name
        holder.tokenSymbol.text = token.symbol
        holder.tokenStatus.text = token.status.uppercase()
        
        // Load token icon image with SVG support
        if (!token.logoUrl.isNullOrEmpty()) {
            if (token.logoUrl.contains(".svg") || token.logoUrl.contains("/svg?")) {
                // Load SVG using custom loader
                loadSvgImage(token.logoUrl, holder.tokenIcon)
            } else {
                // Load regular image using Glide
                Glide.with(holder.itemView.context)
                    .load(token.logoUrl)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.holo_red_light)
                    .into(holder.tokenIcon)
            }
        } else {
            // Set icon color based on token name if no image
            val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#FDA7DF")
            val colorIndex = token.name.hashCode() % colors.size
            holder.tokenIcon.setBackgroundColor(Color.parseColor(colors[kotlin.math.abs(colorIndex)]))
        }
        
        // Set status color
        val statusColor = when (token.status.lowercase()) {
            "active" -> "#9945FF"
            "presale" -> "#FF6B35"
            "launched" -> "#4CAF50"
            else -> "#666666"
        }
        holder.tokenStatus.setTextColor(Color.parseColor(statusColor))
        
        // Set click listener
        holder.infoButton.setOnClickListener {
            onInfoClick(token)
        }
    }

    override fun getItemCount(): Int = likedTokens.size
    
    private fun loadSvgImage(svgUrl: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load SVG from URL
                val inputStream = URL(svgUrl).openStream()
                val svg = SVG.getFromInputStream(inputStream)
                
                // Create a bitmap from the SVG
                val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                svg.renderToCanvas(canvas)
                
                withContext(Dispatchers.Main) {
                    // Set the bitmap to the ImageView
                    val drawable = BitmapDrawable(imageView.context.resources, bitmap)
                    imageView.setImageDrawable(drawable)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Set placeholder on error
                    imageView.setBackgroundColor(imageView.context.getColor(android.R.color.holo_red_light))
                }
            }
        }
    }
}