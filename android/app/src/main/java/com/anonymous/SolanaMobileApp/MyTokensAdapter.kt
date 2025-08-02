package com.anonymous.SolanaMobileApp

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MyTokensAdapter(
    private val tokens: List<CreatedTokenInfo>,
    private val onViewClick: (CreatedTokenInfo) -> Unit
) : RecyclerView.Adapter<MyTokensAdapter.TokenViewHolder>() {

    class TokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tokenIcon: TextView = itemView.findViewById(R.id.tokenIcon)
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
        
        // Set token info
        holder.tokenName.text = token.name
        holder.tokenSymbol.text = token.symbol
        holder.tokenSupply.text = formatSupply(token.supply)
        holder.tokenStatus.text = token.status
        
        // Set status color
        when (token.status) {
            "Active" -> {
                holder.tokenStatus.setBackgroundColor(0xFF10B98120.toInt())
                holder.tokenStatus.setTextColor(0xFF059669.toInt())
            }
            "Voting" -> {
                holder.tokenStatus.setBackgroundColor(0xFFEF444420.toInt())
                holder.tokenStatus.setTextColor(0xFFDC2626.toInt())
            }
            "Presale" -> {
                holder.tokenStatus.setBackgroundColor(0xFFF59E0B20.toInt())
                holder.tokenStatus.setTextColor(0xFFD97706.toInt())
            }
        }
        
        // Set launch type icon
        holder.tokenIcon.text = when (token.launchType) {
            LaunchType.INSTANT -> "🚀"
            LaunchType.PRESALE -> "🗳️"
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
}