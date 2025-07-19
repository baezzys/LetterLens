package com.letterlens.application.port.`in`

import com.letterlens.domain.UserId
import com.letterlens.domain.AuthProvider

interface UpdateProfileUseCase {
    suspend fun updateProfile(command: UpdateProfileCommand): UserResult
}

data class UpdateProfileCommand(
    val userId: UserId,
    val username: String?,
    val profileImageUrl: String?
)

data class UserResult(
    val userId: UserId,
    val username: String,
    val profileImageUrl: String?,
    val provider: AuthProvider,
    val createdAt: String
)
