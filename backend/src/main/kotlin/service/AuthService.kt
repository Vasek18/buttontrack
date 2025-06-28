package com.buttontrack.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: String,
    val email: String,
    val name: String
)

class AuthService {
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(listOf(System.getenv("GOOGLE_CLIENT_ID")))
        .build()

    suspend fun verifyToken(idToken: String): UserInfo? {
        return try {
            val googleIdToken = verifier.verify(idToken)
            if (googleIdToken != null) {
                val payload = googleIdToken.payload
                UserInfo(
                    id = payload.subject,
                    email = payload.email,
                    name = payload["name"] as String? ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}