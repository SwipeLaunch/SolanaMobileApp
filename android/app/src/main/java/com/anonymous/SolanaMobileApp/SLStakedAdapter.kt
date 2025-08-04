package com.anonymous.SolanaMobileApp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SLStakedAdapter(
    private val stakedUsers: List<SLTokenStakedData>
) : RecyclerView.Adapter<SLStakedAdapter.StakedViewHolder>() {

    class StakedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankBadge: TextView = itemView.findViewById(R.id.rankBadge)
        val userAvatar: View = itemView.findViewById(R.id.userAvatar)
        val displayName: TextView = itemView.findViewById(R.id.displayName)
        val stakeDuration: TextView = itemView.findViewById(R.id.stakeDuration)
        val walletAddress: TextView = itemView.findViewById(R.id.walletAddress)
        val stakedAmount: TextView = itemView.findViewById(R.id.stakedAmount)
        val rewardsEarned: TextView = itemView.findViewById(R.id.rewardsEarned)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StakedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_staked_item, parent, false)
        return StakedViewHolder(view)
    }

    override fun onBindViewHolder(holder: StakedViewHolder, position: Int) {
        val user = stakedUsers[position]
        
        // Rank badge with special colors for top 3
        holder.rankBadge.text = user.rank.toString()
        when (user.rank) {
            1 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#FFD700")) // Gold
            2 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#C0C0C0")) // Silver
            3 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#CD7F32")) // Bronze
            else -> holder.rankBadge.setBackgroundColor(Color.parseColor("#9945FF")) // Purple
        }
        
        holder.displayName.text = user.getDisplayName()
        val votingRights = user.getVotingRightsText()
        holder.stakeDuration.text = votingRights
        holder.walletAddress.text = "${user.walletAddress.take(4)}...${user.walletAddress.takeLast(4)}"
        holder.stakedAmount.text = user.getFormattedSLBalance()
        holder.rewardsEarned.text = "${user.dailyVotingRightsRemaining} likes left"
        
        android.util.Log.d("SLStakedAdapter", "${user.getDisplayName()}: Voting rights = $votingRights, SL = ${user.getFormattedSLBalance()}")
        
        // Generate random color for user avatar placeholder
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#FDA7DF")
        val colorIndex = position % colors.size
        holder.userAvatar.setBackgroundColor(Color.parseColor(colors[colorIndex]))
    }

    override fun getItemCount(): Int = stakedUsers.size
}