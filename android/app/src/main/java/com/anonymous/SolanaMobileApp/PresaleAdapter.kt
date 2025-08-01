package com.anonymous.SolanaMobileApp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        val tokenLogo: View = itemView.findViewById(R.id.tokenLogo)
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
}