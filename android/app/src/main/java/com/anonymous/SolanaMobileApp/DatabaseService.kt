package com.anonymous.SolanaMobileApp

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class TokenRecord(
    val token_id: Int? = null,
    val token_name: String,
    val symbol: String,
    val description: String? = null,
    val image_url: String? = null,
    val creator_wallet: String,
    val token_mint_address: String? = null,
    val status: String = "active",
    val vote_count: Int = 0,
    val sol_raised: Double = 0.0,
    val target_sol_amount: Double = 100.0,
    val launch_price_sol: Double? = null,
    val created_at: String? = null,
    val presale_started_at: String? = null,
    val launched_at: String? = null
)

@Serializable
data class UserRecord(
    val wallet_address: String,
    val twitter_handle: String? = null,
    val solana_name: String? = null,
    val sol_balance: Double = 1.0, // SOL balance for the user
    val sl_token_balance: Long = 0,
    val daily_voting_rights_remaining: Int = 0,
    val daily_voting_rights_total: Int = 0,
    val created_at: String? = null
)

@Serializable
data class UserVoteRecord(
    val user_wallet: String,
    val token_id: Int,
    val voted_at: String? = null
)

@Serializable
data class PresaleParticipantRecord(
    val id: Int? = null,
    val user_wallet: String,
    val token_id: Int,
    val sol_contributed: Double,
    val participated_at: String? = null
)

class DatabaseService {
    
    private val supabase = SupabaseConfig.client
    
    suspend fun getAllTokens(): List<TokenRecord> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Attempting to read tokens...")
                val result = supabase.from("tokens")
                    .select()
                    .decodeList<TokenRecord>()
                android.util.Log.d("DatabaseService", "Successfully read ${result.size} tokens")
                result
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error reading tokens: ${e.message}")
                android.util.Log.e("DatabaseService", "Error details: ${e.stackTraceToString()}")
                // Return empty list if table doesn't exist or error occurs
                emptyList()
            }
        }
    }
    
    // Test with correct database structure
    suspend fun getRawTokenData(): String {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Getting tokens with correct structure...")
                val result = supabase.from("tokens")
                    .select()
                    .decodeList<TokenRecord>()
                
                android.util.Log.d("DatabaseService", "Token count: ${result.size}")
                if (result.isNotEmpty()) {
                    val firstRecord = result.first()
                    android.util.Log.d("DatabaseService", "First token: id=${firstRecord.token_id}, name=${firstRecord.token_name}, symbol=${firstRecord.symbol}, votes=${firstRecord.vote_count}")
                }
                
                "Found ${result.size} tokens in database"
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error reading tokens: ${e.message}")
                "Error: ${e.message}"
            }
        }
    }
    
    suspend fun insertToken(token: TokenRecord): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("tokens")
                    .insert(token)
                true
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error inserting token: ${e.message}")
                false
            }
        }
    }
    
    suspend fun likeToken(tokenId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // In a real implementation, you'd increment likes count
                // For now, just return success
                true
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error liking token: ${e.message}")
                false
            }
        }
    }
    
    suspend fun getUserVotes(): List<UserVoteRecord> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("user_votes")
                    .select()
                    .decodeList<UserVoteRecord>()
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error reading user votes: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun getUsers(): List<UserRecord> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("users")
                    .select()
                    .decodeList<UserRecord>()
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error reading users: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun getPresaleParticipants(): List<PresaleParticipantRecord> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("presale_participants")
                    .select()
                    .decodeList<PresaleParticipantRecord>()
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error reading presale participants: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Testing connection to Supabase...")
                
                // Simple connection test - just try to get a response
                val result = supabase.from("tokens").select().decodeList<TokenRecord>()
                android.util.Log.d("DatabaseService", "✅ Successfully connected and got ${result.size} tokens!")
                
                true
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Connection test failed: ${e.message}")
                false
            }
        }
    }
    
    suspend fun discoverTableStructure(): String {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Discovering table structure...")
                
                // Just log the URL that would be called
                android.util.Log.d("DatabaseService", "Would call: https://hhihnakkhihvfxyhuetq.supabase.co/rest/v1/tokens")
                
                "Table discovery test - check Supabase dashboard for actual structure"
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Table discovery failed: ${e.message}")
                "Error discovering table structure"
            }
        }
    }
    
    suspend fun getUserVotedTokenIds(walletAddress: String): List<Int> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Getting voted tokens for wallet: $walletAddress")
                val votes = supabase.from("user_votes")
                    .select()
                    .decodeList<UserVoteRecord>()
                    .filter { it.user_wallet == walletAddress }
                
                val tokenIds = votes.map { it.token_id }
                android.util.Log.d("DatabaseService", "Found ${tokenIds.size} voted tokens for wallet")
                tokenIds
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error getting voted tokens: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun getUserPresaleTokenIds(walletAddress: String): List<Int> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Getting presale tokens for wallet: $walletAddress")
                val participants = supabase.from("presale_participants")
                    .select()
                    .decodeList<PresaleParticipantRecord>()
                    .filter { it.user_wallet == walletAddress }
                
                val tokenIds = participants.map { it.token_id }
                android.util.Log.d("DatabaseService", "Found ${tokenIds.size} presale tokens for wallet")
                tokenIds
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error getting presale tokens: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun addPresaleParticipant(tokenId: Int, walletAddress: String, solAmount: Double): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Adding presale participant: tokenId=$tokenId, wallet=$walletAddress, amount=$solAmount")
                
                val participantRecord = PresaleParticipantRecord(
                    token_id = tokenId,
                    user_wallet = walletAddress,
                    sol_contributed = solAmount
                )
                
                supabase.from("presale_participants")
                    .insert(participantRecord)
                
                android.util.Log.d("DatabaseService", "Successfully added presale participant")
                true
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error adding presale participant: ${e.message}")
                false
            }
        }
    }
    
    suspend fun updateTokenSolRaised(tokenId: Int, additionalSol: Double): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Updating sol_raised for token $tokenId by +$additionalSol")
                
                // First get current sol_raised amount
                val currentToken = supabase.from("tokens")
                    .select()
                    .decodeList<TokenRecord>()
                    .find { it.token_id == tokenId }
                
                if (currentToken != null) {
                    val newSolRaised = (currentToken.sol_raised ?: 0.0) + additionalSol
                    
                    // Update the token
                    supabase.from("tokens")
                        .update(mapOf("sol_raised" to newSolRaised)) {
                            filter {
                                eq("token_id", tokenId)
                            }
                        }
                    
                    android.util.Log.d("DatabaseService", "Successfully updated sol_raised from ${currentToken.sol_raised} to $newSolRaised")
                    true
                } else {
                    android.util.Log.e("DatabaseService", "Token with id $tokenId not found")
                    false
                }
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error updating sol_raised: ${e.message}")
                false
            }
        }
    }
    
    suspend fun getUserSolBalance(walletAddress: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("DatabaseService", "Getting SOL balance for wallet: $walletAddress")
                
                val user = supabase.from("users")
                    .select()
                    .decodeList<UserRecord>()
                    .find { it.wallet_address == walletAddress }
                
                val balance = user?.sol_balance ?: 1.0 // Default to 1.0 SOL if user not found
                android.util.Log.d("DatabaseService", "SOL balance for $walletAddress: $balance")
                balance
            } catch (e: Exception) {
                android.util.Log.e("DatabaseService", "Error getting SOL balance: ${e.message}")
                1.0 // Default balance on error
            }
        }
    }
}