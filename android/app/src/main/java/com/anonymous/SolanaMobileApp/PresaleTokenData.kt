package com.anonymous.SolanaMobileApp

data class PresaleTokenData(
    val id: String,
    val name: String,
    val symbol: String,
    val description: String,
    val totalSupply: Long,
    val raisedSol: Double, // Current SOL raised (0-100)
    val startTime: Long,
    val endTime: Long,
    val creatorAddress: String,
    val tokenAddress: String,
    val logoUrl: String = "", // Placeholder for now
    val isActive: Boolean = true
) {
    companion object {
        const val TARGET_SOL = 100.0 // Fixed target: 100 SOL
        const val USER_SUPPLY_PERCENT = 0.42 // Users get 42% of supply
    }
    
    fun getProgressPercentage(): Int {
        return ((raisedSol / TARGET_SOL) * 100).toInt().coerceIn(0, 100)
    }
    
    fun getRaisedAmount(): Double {
        return raisedSol
    }
    
    fun getTokensPerSol(): Double {
        // 42% of total supply divided by 100 SOL target
        val availableTokens = totalSupply * USER_SUPPLY_PERCENT
        return availableTokens / TARGET_SOL
    }
    
    fun getTokenPrice(): Double {
        // Price per token = 1 SOL / tokens per SOL
        return 1.0 / getTokensPerSol()
    }
    
    fun getTimeExisted(): String {
        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - startTime
        
        if (timeElapsed <= 0) return "Just started"
        
        val days = timeElapsed / (1000 * 60 * 60 * 24)
        val hours = (timeElapsed % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (timeElapsed % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            days > 0 -> "${days}d ${hours}h ago"
            hours > 0 -> "${hours}h ${minutes}m ago"
            else -> "${minutes}m ago"
        }
    }
}