package com.anonymous.SolanaMobileApp

data class TokenData(
    val id: String,
    val name: String,
    val creator: String,
    val description: String,
    val likes: Int,
    // Creator detailed info
    val creatorWallet: String,
    val creatorTwitter: String,
    val creatorSolanaHandle: String,
    val slTokenStaked: Int,
    val totalLikesReceived: Int,
    val tokensLaunched: Int,
    val communityLink: String
)