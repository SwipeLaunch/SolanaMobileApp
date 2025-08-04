package com.anonymous.SolanaMobileApp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class ActivityFeedAdapter(
    private val activities: List<ActivityFeedData>,
    private val onViewTokenClick: (ActivityFeedData) -> Unit = {}
) : RecyclerView.Adapter<ActivityFeedAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val activityTime: TextView = itemView.findViewById(R.id.activityTime)
        val activityTypeIcon: TextView = itemView.findViewById(R.id.activityTypeIcon)
        val activityDescription: TextView = itemView.findViewById(R.id.activityDescription)
        val tokenInfoCard: LinearLayout = itemView.findViewById(R.id.tokenInfoCard)
        val tokenName: TextView = itemView.findViewById(R.id.tokenName)
        val tokenCreator: TextView = itemView.findViewById(R.id.tokenCreator)
        val tokenPrice: TextView = itemView.findViewById(R.id.tokenPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_feed_item, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        
        // Set user info
        android.util.Log.d("ActivityFeedAdapter", "Loading avatar: ${activity.userAvatar} for user: ${activity.userName}")
        Glide.with(holder.itemView.context)
            .load(activity.userAvatar)
            .circleCrop()
            .placeholder(android.R.color.darker_gray)
            .error(android.R.color.holo_red_light)
            .into(holder.userAvatar)
        holder.userName.text = activity.userName
        holder.activityTime.text = formatTimeAgo(activity.timestamp)
        
        // Set activity type
        holder.activityTypeIcon.text = activity.activityType.icon
        holder.activityTypeIcon.setBackgroundColor(Color.parseColor(activity.activityType.color))
        
        // Set activity description
        holder.activityDescription.text = activity.description
        
        // Handle token info
        if (activity.tokenInfo != null) {
            holder.tokenInfoCard.visibility = View.VISIBLE
            holder.tokenName.text = activity.tokenInfo.tokenName
            holder.tokenCreator.text = "by ${activity.tokenInfo.tokenCreator}"
            holder.tokenPrice.text = activity.tokenInfo.tokenPrice
            
            // Make the whole token card clickable for mobile-friendly UX
            holder.tokenInfoCard.setOnClickListener {
                onViewTokenClick(activity)
            }
        } else {
            holder.tokenInfoCard.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = activities.size
    
    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            diff < 604800_000 -> "${diff / 86400_000}d ago"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
        }
    }
}