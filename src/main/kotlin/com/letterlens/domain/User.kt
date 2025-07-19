package com.letterlens.domain

import java.time.LocalDateTime
import java.util.*

data class User(
    val id: UserId,
    val username: String,
    val profileImageUrl: String?,
    val oauthProvider: OAuthProvider,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
    }
    
    fun updateProfile(username: String, profileImageUrl: String?): User {
        require(username.isNotBlank()) { "Username cannot be blank" }
        
        return this.copy(
            username = username,
            profileImageUrl = profileImageUrl,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun isFromProvider(providerType: OAuthProviderType): Boolean {
        return oauthProvider.provider == providerType
    }
    
    companion object {
        fun createWithOAuth(
            username: String,
            profileImageUrl: String?,
            oauthProvider: OAuthProvider
        ): User {
            val now = LocalDateTime.now()
            return User(
                id = UserId.generate(),
                username = username,
                profileImageUrl = profileImageUrl,
                oauthProvider = oauthProvider,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "User ID cannot be blank" }
        // UUID 형식 검증
        require(isValidUUID(value)) { "User ID must be a valid UUID" }
    }
    
    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    companion object {
        fun generate(): UserId {
            return UserId(UUID.randomUUID().toString())
        }
    }
}

data class OAuthProvider(
    val provider: OAuthProviderType,
    val originalId: String,        // 각 OAuth 제공자의 원본 ID
    val email: String?,           // OAuth에서 제공하는 이메일 (선택적)
    val connectedAt: LocalDateTime
) {
    init {
        require(originalId.isNotBlank()) { "Original ID cannot be blank" }
    }
}

enum class OAuthProviderType(val displayName: String) {
    KAKAO("카카오"),
    NAVER("네이버"),
    GOOGLE("구글"),
    GITHUB("깃허브");
    
    companion object {
        fun fromString(value: String): OAuthProviderType? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
