package com.anonymous.SolanaMobileApp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TokenLaunchedAdapter(
    private val tokens: List<TokenLaunchedData>
) : RecyclerView.Adapter<TokenLaunchedAdapter.TokenViewHolder>() {

    class TokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankBadge: TextView = itemView.findViewById(R.id.rankBadge)
        val tokenLogo: View = itemView.findViewById(R.id.tokenLogo)
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
        holder.marketCapSOL.text = token.getFormattedMarketCap()
        holder.marketCapUSD.text = token.getFormattedMarketCapUSD()
        
        // Generate random color for token logo placeholder
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#FDA7DF")
        val colorIndex = position % colors.size
        holder.tokenLogo.setBackgroundColor(Color.parseColor(colors[colorIndex]))
    }

    override fun getItemCount(): Int = tokens.size
}