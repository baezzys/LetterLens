package com.letterlens.application.port.`in`

import com.letterlens.domain.AuthProvider
import com.letterlens.domain.UserId

interface LoginUseCase {
    suspend fun loginWithOAuth(command: OAuthLoginCommand): LoginResult
}

data class OAuthLoginCommand(
    val provider: AuthProvider,
    val originalId: String,
    val email: String?,
    val username: String,
    val profileImageUrl: String?
)

data class LoginResult(
    val userId: UserId,
    val username: String,
    val profileImageUrl: String?,
    val isNewUser: Boolean
)
