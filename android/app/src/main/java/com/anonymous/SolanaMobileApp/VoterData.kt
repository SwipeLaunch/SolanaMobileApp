package com.anonymous.SolanaMobileApp

data class VoterData(
    val walletAddress: String,
    val tokensVoted: Int,
    val isFollowed: Boolean = false
)