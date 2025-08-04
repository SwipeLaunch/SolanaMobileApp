package com.anonymous.SolanaMobileApp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.text.SimpleDateFormat
import java.util.*

class TokenLaunchedAdapter(
    private val tokens: List<TokenLaunchedData>
) : RecyclerView.Adapter<TokenLaunchedAdapter.TokenViewHolder>() {

    class TokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankBadge: TextView = itemView.findViewById(R.id.rankBadge)
        val tokenLogo: ImageView = itemView.findViewById(R.id.tokenLogo)
        val tokenName: TextView = itemView.findViewById(R.id.tokenName)
        val tokenSymbol: TextView = itemView.findViewById(R.id.tokenSymbol)
        val creatorName: TextView = itemView.findViewById(R.id.creatorName)
        val marketCapSOL: TextView = itemView.findViewById(R.id.marketCapSOL)
        val marketCapUSD: TextView = itemView.findViewById(R.id.marketCapUSD)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_token_item, parent, false)
        return TokenViewHolder(view)
    }

    override fun onBindViewHolder(holder: TokenViewHolder, position: Int) {
        val token = tokens[position]
        
        // Rank badge with special colors for top 3
        holder.rankBadge.text = token.rank.toString()
        when (token.rank) {
            1 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#FFD700")) // Gold
            2 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#C0C0C0")) // Silver
            3 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#CD7F32")) // Bronze
            else -> holder.rankBadge.setBackgroundColor(Color.parseColor("#9945FF")) // Purple
        }
        
        holder.tokenName.text = token.tokenName
        holder.tokenSymbol.text = token.tokenSymbol
        holder.creatorName.text = "by ${token.creator}"
        // Show market cap in USD as main value
        val formattedMarketCap = token.getFormattedMarketCapUSD()
        holder.marketCapSOL.text = formattedMarketCap
        android.util.Log.d("TokenAdapter", "${token.tokenName}: Raw=${token.marketCapUSD} → Formatted=${formattedMarketCap}")
        
        // Show launch date as secondary info
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        holder.marketCapUSD.text = "Launched ${dateFormat.format(Date(token.launchDate))}"
        
        // Load token logo image with SVG support
        if (!token.logoUrl.isNullOrEmpty()) {
            if (token.logoUrl.contains(".svg") || token.logoUrl.contains("/svg?")) {
                // Load SVG using custom loader
                loadSvgImage(token.logoUrl, holder.tokenLogo)
            } else {
                // Load regular image using Glide
                Glide.with(holder.itemView.context)
                    .load(token.logoUrl)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.holo_red_light)
                    .into(holder.tokenLogo)
            }
        } else {
            // Generate random color for token logo placeholder if no image
            val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#FDA7DF")
            val colorIndex = position % colors.size
            holder.tokenLogo.setBackgroundColor(Color.parseColor(colors[colorIndex]))
        }
    }

    override fun getItemCount(): Int = tokens.size
    
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