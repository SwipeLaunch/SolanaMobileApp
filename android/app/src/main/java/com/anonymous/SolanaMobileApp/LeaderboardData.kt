package com.anonymous.SolanaMobileApp

// Token launched with marketcap data
data class TokenLaunchedData(
    val rank: Int,
    val tokenName: String,
    val tokenSymbol: String,
    val marketCap: Double, // in SOL
    val marketCapUSD: Double,
    val creator: String,
    val launchDate: Long,
    val logoUrl: String = ""
) {
    fun getFormattedMarketCap(): String {
        return when {
            marketCap >= 1_000_000 -> "${String.format("%.2f", marketCap / 1_000_000)}M SOL"
            marketCap >= 1_000 -> "${String.format("%.1f", marketCap / 1_000)}K SOL"
            else -> "${String.format("%.0f", marketCap)} SOL"
        }
    }
    
    fun getFormattedMarketCapUSD(): String {
        return when {
            marketCapUSD >= 1_000_000 -> "$${String.format("%.2f", marketCapUSD / 1_000_000)}M"
            marketCapUSD >= 1_000 -> "$${String.format("%.1f", marketCapUSD / 1_000)}K"
            else -> "$${String.format("%.0f", marketCapUSD)}"
        }
    }
}

// SL Token staked leaderboard data
data class SLTokenStakedData(
    val rank: Int,
    val walletAddress: String,
    val twitterHandle: String?, // Optional Twitter handle
    val solanaDomain: String?, // Optional .sol domain
    val stakedAmount: Double, // Amount of SL tokens staked
    val stakingDuration: Long, // How long they've been staking (ms)
    val rewardsEarned: Double
) {
    fun getDisplayName(): String {
        return when {
            solanaDomain != null -> solanaDomain
            twitterHandle != null -> "@$twitterHandle"
            else -> "${walletAddress.take(4)}...${walletAddress.takeLast(4)}"
        }
    }
    
    fun getFormattedStakedAmount(): String {
        return when {
            stakedAmount >= 1_000_000 -> "${String.format("%.2f", stakedAmount / 1_000_000)}M SL"
            stakedAmount >= 1_000 -> "${String.format("%.1f", stakedAmount / 1_000)}K SL"
            else -> "${String.format("%.0f", stakedAmount)} SL"
        }
    }
    
    fun getStakingDurationText(): String {
        val days = stakingDuration / (1000 * 60 * 60 * 24)
        return when {
            days > 30 -> "${days / 30}mo"
            days > 0 -> "${days}d"
            else -> "New"
        }
    }
}

// Creator with most likes received
data class CreatorMostLikesData(
    val rank: Int,
    val creatorName: String,
    val twitterHandle: String?,
    val totalLikes: Int,
    val totalTokensCreated: Int,
    val averageLikesPerToken: Double,
    val topTokenName: String, // Their most liked token
    val topTokenLikes: Int
) {
    fun getDisplayName(): String {
        return if (twitterHandle != null) "@$twitterHandle" else creatorName
    }
    
    fun getFormattedLikes(): String {
        return when {
            totalLikes >= 1_000_000 -> "${String.format("%.1f", totalLikes / 1_000_000.0)}M"
            totalLikes >= 1_000 -> "${String.format("%.1f", totalLikes / 1_000.0)}K"
            else -> totalLikes.toString()
        }
    }
}

// Creator with most tokens launched
data class CreatorMostLaunchedData(
    val rank: Int,
    val creatorName: String,
    val twitterHandle: String?,
    val totalTokensLaunched: Int,
    val totalMarketCap: Double, // Combined market cap of all tokens
    val successRate: Double, // Percentage of successful launches (>1K SOL market cap)
    val bestPerformingToken: String,
    val bestTokenMarketCap: Double
) {
    fun getDisplayName(): String {
        return if (twitterHandle != null) "@$twitterHandle" else creatorName
    }
    
    fun getFormattedTotalMarketCap(): String {
        return when {
            totalMarketCap >= 1_000_000 -> "${String.format("%.2f", totalMarketCap / 1_000_000)}M SOL"
            totalMarketCap >= 1_000 -> "${String.format("%.1f", totalMarketCap / 1_000)}K SOL"
            else -> "${String.format("%.0f", totalMarketCap)} SOL"
        }
    }
    
    fun getSuccessRateText(): String {
        return "${String.format("%.0f", successRate * 100)}% success"
    }
}