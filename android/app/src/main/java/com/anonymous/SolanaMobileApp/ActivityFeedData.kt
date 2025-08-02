package com.anonymous.SolanaMobileApp

data class ActivityFeedData(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val activityType: ActivityType,
    val description: String,
    val timestamp: Long,
    val tokenInfo: TokenActivityInfo? = null
)

data class TokenActivityInfo(
    val tokenName: String,
    val tokenCreator: String,
    val tokenPrice: String
)

enum class ActivityType(val icon: String, val color: String) {
    LIKE("❤️", "#FF6B35"),
    PRESALE("🚀", "#9945FF"),
    LAUNCH("🎉", "#14B8A6"),
    FOLLOW("👥", "#6366F1")
}