package com.anonymous.SolanaMobileApp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CreatorLikesAdapter(
    private val creators: List<CreatorMostLikesData>
) : RecyclerView.Adapter<CreatorLikesAdapter.CreatorViewHolder>() {

    class CreatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankBadge: TextView = itemView.findViewById(R.id.rankBadge)
        val creatorAvatar: View = itemView.findViewById(R.id.creatorAvatar)
        val creatorName: TextView = itemView.findViewById(R.id.creatorName)
        val primaryMetric: TextView = itemView.findViewById(R.id.primaryMetric)
        val secondaryMetric: TextView = itemView.findViewById(R.id.secondaryMetric)
        val mainStat: TextView = itemView.findViewById(R.id.mainStat)
        val subStat: TextView = itemView.findViewById(R.id.subStat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_creator_item, parent, false)
        return CreatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: CreatorViewHolder, position: Int) {
        val creator = creators[position]
        
        // Rank badge with special colors for top 3
        holder.rankBadge.text = creator.rank.toString()
        when (creator.rank) {
            1 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#FFD700")) // Gold
            2 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#C0C0C0")) // Silver
            3 -> holder.rankBadge.setBackgroundColor(Color.parseColor("#CD7F32")) // Bronze
            else -> holder.rankBadge.setBackgroundColor(Color.parseColor("#9945FF")) // Purple
        }
        
        holder.creatorName.text = creator.getDisplayName()
        holder.primaryMetric.text = "${creator.getFormattedLikes()} likes"
        holder.secondaryMetric.text = "${creator.totalTokensCreated} tokens"
        holder.mainStat.text = creator.getFormattedLikes()
        holder.subStat.text = "avg ${String.format("%.0f", creator.averageLikesPerToken)}/token"
        
        // Generate random color for creator avatar placeholder
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#FDA7DF")
        val colorIndex = position % colors.size
        holder.creatorAvatar.setBackgroundColor(Color.parseColor(colors[colorIndex]))
    }

    override fun getItemCount(): Int = creators.size
}