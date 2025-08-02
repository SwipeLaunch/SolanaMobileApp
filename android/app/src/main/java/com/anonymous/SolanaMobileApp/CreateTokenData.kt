package com.anonymous.SolanaMobileApp

import android.net.Uri

data class CreateTokenData(
    val name: String,
    val symbol: String,
    val description: String,
    val initialSupply: Long,
    val imageUri: Uri? = null,
    val launchType: LaunchType,
    val presaleDurationHours: Int? = null,
    val chatLink: String? = null
)

enum class LaunchType {
    INSTANT,
    PRESALE
}

data class TokenCreationResult(
    val success: Boolean,
    val tokenAddress: String? = null,
    val transactionSignature: String? = null,
    val errorMessage: String? = null
)

data class CreatedTokenInfo(
    val name: String,
    val symbol: String,
    val description: String,
    val supply: Long,
    val launchType: LaunchType,
    val tokenAddress: String,
    val chatLink: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Active" // Active, Voting, Presale, etc.
)