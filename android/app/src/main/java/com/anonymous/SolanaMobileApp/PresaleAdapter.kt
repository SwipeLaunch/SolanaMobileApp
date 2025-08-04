package com.anonymous.SolanaMobileApp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class PresaleAdapter(
    private val presaleTokens: List<PresaleTokenData>,
    private val onBuyClick: (PresaleTokenData) -> Unit
) : RecyclerView.Adapter<PresaleAdapter.PresaleViewHolder>() {

    class PresaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tokenName: TextView = itemView.findViewById(R.id.tokenName)
        val tokenSymbol: TextView = itemView.findViewById(R.id.tokenSymbol)
        val tokenPrice: TextView = itemView.findViewById(R.id.tokenPrice)
        val tokenDescription: TextView = itemView.findViewById(R.id.tokenDescription)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val progressPercentage: TextView = itemView.findViewById(R.id.progressPercentage)
        val raisedAmount: TextView = itemView.findViewById(R.id.raisedAmount)
        val hardCap: TextView = itemView.findViewById(R.id.hardCap)
        val timeRemaining: TextView = itemView.findViewById(R.id.timeRemaining)
        val buyButton: Button = itemView.findViewById(R.id.buyButton)
        val tokenLogo: ImageView = itemView.findViewById(R.id.tokenLogo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.presale_item, parent, false)
        return PresaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: PresaleViewHolder, position: Int) {
        val token = presaleTokens[position]
        
        holder.tokenName.text = token.name
        holder.tokenSymbol.text = token.symbol
        
        // Load token image
        if (!token.logoUrl.isNullOrEmpty()) {
            // Check if it's an SVG image
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
            // Set default placeholder if no image URL
            holder.tokenLogo.setBackgroundColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }
        
        // Show tokens per SOL (how many tokens you get for 1 SOL)
        val tokensPerSol = token.getTokensPerSol()
        holder.tokenPrice.text = "1 SOL = ${String.format("%.0f", tokensPerSol)} ${token.symbol}"
        holder.tokenDescription.text = token.description
        
        // Progress
        val progress = token.getProgressPercentage()
        holder.progressBar.progress = progress
        holder.progressPercentage.text = "$progress%"
        
        // Amounts - Fixed 100 SOL target
        val raised = token.getRaisedAmount()
        holder.raisedAmount.text = "Raised: ${String.format("%.1f", raised)} SOL"
        holder.hardCap.text = "Target: 100 SOL"
        
        // Time existed
        val timeExisted = token.getTimeExisted()
        holder.timeRemaining.text = timeExisted
        
        // Buy button - always enabled for active tokens
        holder.buyButton.isEnabled = token.isActive
        holder.buyButton.text = "Buy Now"
        holder.buyButton.setOnClickListener {
            if (token.isActive) {
                onBuyClick(token)
            }
        }
        
        // Color coding for time existed (green = established, orange = recent)
        when {
            timeExisted == "Just started" -> {
                holder.timeRemaining.setTextColor(0xFF9945FF.toInt()) // Purple for just started
            }
            timeExisted.contains("d") -> {
                holder.timeRemaining.setTextColor(0xFF4CAF50.toInt()) // Green for days
            }
            timeExisted.contains("h") -> {
                holder.timeRemaining.setTextColor(0xFFFF9800.toInt()) // Orange for hours
            }
            else -> {
                holder.timeRemaining.setTextColor(0xFF2196F3.toInt()) // Blue for minutes
            }
        }
    }

    override fun getItemCount(): Int = presaleTokens.size
    
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
                    imageView.setBackgroundColor(imageView.context.getColor(R.color.light_gray))
                }
            }
        }
    }
}