package com.letterlens.infrastructure.persistence.entity

import com.letterlens.domain.*
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class UserEntity(
    @Id
    val id: String,
    
    @Column("username")
    val username: String,
    
    @Column("profile_image_url")
    val profileImageUrl: String?,
    
    @Column("oauth_provider")
    val oauthProvider: String,
    
    @Column("oauth_original_id")
    val oauthOriginalId: String,
    
    @Column("oauth_email")
    val oauthEmail: String?,
    
    @Column("oauth_connected_at")
    val oauthConnectedAt: LocalDateTime,
    
    @Column("created_at")
    val createdAt: LocalDateTime,
    
    @Column("updated_at")
    val updatedAt: LocalDateTime
) {
    fun toDomain(): User {
        return User(
            id = UserId(id),
            username = username,
            profileImageUrl = profileImageUrl,
            oauthProvider = OAuthProvider(
                provider = OAuthProviderType.valueOf(oauthProvider),
                originalId = oauthOriginalId,
                email = oauthEmail,
                connectedAt = oauthConnectedAt
            ),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.id.value,
                username = user.username,
                profileImageUrl = user.profileImageUrl,
                oauthProvider = user.oauthProvider.provider.name,
                oauthOriginalId = user.oauthProvider.originalId,
                oauthEmail = user.oauthProvider.email,
                oauthConnectedAt = user.oauthProvider.connectedAt,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
