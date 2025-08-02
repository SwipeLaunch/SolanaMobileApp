package com.anonymous.SolanaMobileApp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VoterAdapter(
    private val voters: MutableList<VoterData>,
    private val onFollowClick: (VoterData, Int) -> Unit
) : RecyclerView.Adapter<VoterAdapter.VoterViewHolder>() {

    class VoterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val voterAvatar: View = itemView.findViewById(R.id.voterAvatar)
        val voterWallet: TextView = itemView.findViewById(R.id.voterWallet)
        val voterTokens: TextView = itemView.findViewById(R.id.voterTokens)
        val followButton: Button = itemView.findViewById(R.id.followButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.voter_item, parent, false)
        return VoterViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoterViewHolder, position: Int) {
        val voter = voters[position]
        
        // Format wallet address
        val formattedWallet = if (voter.walletAddress.length >= 8) {
            "${voter.walletAddress.take(4)}...${voter.walletAddress.takeLast(4)}"
        } else {
            voter.walletAddress
        }
        
        holder.voterWallet.text = formattedWallet
        holder.voterTokens.text = "voted on ${voter.tokensVoted} tokens"
        
        // Set avatar color based on wallet address
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#FDA7DF")
        val colorIndex = voter.walletAddress.hashCode() % colors.size
        holder.voterAvatar.setBackgroundColor(Color.parseColor(colors[kotlin.math.abs(colorIndex)]))
        
        // Set follow button state
        holder.followButton.text = if (voter.isFollowed) "Following" else "Follow"
        holder.followButton.setBackgroundResource(if (voter.isFollowed) R.drawable.follow_button_following else R.drawable.follow_button_background)
        holder.followButton.setTextColor(Color.parseColor(if (voter.isFollowed) "#4CAF50" else "#9945FF"))
        
        // Set click listener
        holder.followButton.setOnClickListener {
            onFollowClick(voter, position)
        }
    }

    override fun getItemCount(): Int = voters.size
}