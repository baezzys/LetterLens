package com.letterlens.infrastructure.persistence.repository

import com.letterlens.infrastructure.persistence.entity.UserEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface UserR2dbcRepository : ReactiveCrudRepository<UserEntity, String> {
    fun findByOauthProviderAndOauthOriginalId(provider: String, originalId: String): Mono<UserEntity>
}
