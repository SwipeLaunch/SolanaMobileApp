package com.anonymous.SolanaMobileApp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class LikedTokenInfo(
    val id: Int,
    val name: String,
    val symbol: String,
    val status: String,
    val description: String?,
    val creator: String
)

class LikedTokenAdapter(
    private val likedTokens: List<LikedTokenInfo>,
    private val onInfoClick: (LikedTokenInfo) -> Unit
) : RecyclerView.Adapter<LikedTokenAdapter.LikedTokenViewHolder>() {

    class LikedTokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tokenIcon: View = itemView.findViewById(R.id.tokenIcon)
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
        
        // Set icon color based on token name
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#FDA7DF")
        val colorIndex = token.name.hashCode() % colors.size
        holder.tokenIcon.setBackgroundColor(Color.parseColor(colors[kotlin.math.abs(colorIndex)]))
        
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
}