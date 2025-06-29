package com.buttontrack.service

import com.buttontrack.DatabaseFactory.dbQuery
import com.buttontrack.models.User
import com.buttontrack.models.UserTable
import com.buttontrack.models.UserAuth
import com.buttontrack.models.UserAuthTable
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

@Serializable
data class UserInfo(
    val id: Int,
    val email: String,
    val name: String
)

@Serializable
data class UserSession(
    val userId: Int,
    val email: String,
    val name: String,
    val sessionId: String = UUID.randomUUID().toString()
)

class AuthService(private val googleClientId: String) {
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(listOf(googleClientId))
        .build()
        
    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(UserTable, UserAuthTable)
        }
    }

    suspend fun verifyGoogleTokenAndCreateSession(idToken: String): UserSession? = dbQuery {
        try {
            val googleIdToken = verifier.verify(idToken)
            if (googleIdToken != null) {
                val payload = googleIdToken.payload
                val googleUserId = payload.subject
                val email = payload.email
                val name = payload["name"] as String? ?: ""
                
                // Find or create user
                val userAuth = UserAuth.find { 
                    (UserAuthTable.provider eq "google") and 
                    (UserAuthTable.providerUserId eq googleUserId) 
                }.firstOrNull()
                
                val user = if (userAuth != null) {
                    // Existing user, update info if needed
                    userAuth.email = email
                    userAuth.updatedAt = Instant.now()
                    userAuth.user.apply {
                        this.name = name
                        this.updatedAt = Instant.now()
                    }
                } else {
                    // New user, create both user and auth records
                    val newUser = User.new {
                        this.name = name
                        this.createdAt = Instant.now()
                        this.updatedAt = Instant.now()
                    }
                    
                    UserAuth.new {
                        this.userId = newUser.id
                        this.provider = "google"
                        this.providerUserId = googleUserId
                        this.email = email
                        this.createdAt = Instant.now()
                        this.updatedAt = Instant.now()
                    }
                    
                    newUser
                }
                
                UserSession(
                    userId = user.id.value,
                    email = email,
                    name = name,
                    sessionId = UUID.randomUUID().toString()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getUserById(userId: Int): UserInfo? = dbQuery {
        User.findById(userId)?.let { user ->
            val userAuth = UserAuth.find { UserAuthTable.userId eq userId }.firstOrNull()
            if (userAuth != null) {
                UserInfo(
                    id = user.id.value,
                    email = userAuth.email,
                    name = user.name ?: ""
                )
            } else {
                null
            }
        }
    }
}