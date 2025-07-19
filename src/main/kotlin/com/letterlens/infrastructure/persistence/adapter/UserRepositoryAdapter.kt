package com.letterlens.infrastructure.persistence.adapter

import com.letterlens.application.port.out.UserRepository
import com.letterlens.domain.*
import com.letterlens.infrastructure.persistence.entity.UserEntity
import com.letterlens.infrastructure.persistence.repository.UserR2dbcRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val userR2dbcRepository: UserR2dbcRepository
) : UserRepository {
    
    override suspend fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        val savedEntity = userR2dbcRepository.save(entity).awaitSingle()
        return savedEntity.toDomain()
    }
    
    override suspend fun findById(id: UserId): User? {
        return userR2dbcRepository.findById(id.value)
            .map { it.toDomain() }
            .awaitSingleOrNull()
    }
    
    override suspend fun findByOAuthProvider(provider: OAuthProviderType, originalId: String): User? {
        return userR2dbcRepository.findByOauthProviderAndOauthOriginalId(provider.name, originalId)
            .map { it.toDomain() }
            .awaitSingleOrNull()
    }
    
    override suspend fun existsByUsername(username: String): Boolean {
        // 추후 username 중복 체크가 필요하면 구현
        return false
    }
}
