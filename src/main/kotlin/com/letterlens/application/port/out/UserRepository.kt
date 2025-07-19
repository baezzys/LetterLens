package com.letterlens.application.port.out

import com.letterlens.domain.*

interface UserRepository {
    
    suspend fun save(user: User): User
    
    suspend fun findById(userId: UserId): User?
    
    suspend fun findByOAuthProvider(provider: OAuthProviderType, originalId: String): User?
    
    suspend fun existsByUsername(username: String): Boolean
}
