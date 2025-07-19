package com.letterlens.application.port.`in`

import com.letterlens.domain.User
import com.letterlens.domain.UserId

interface UserUseCase {
    suspend fun getMyProfile(query: GetMyProfileQuery): User
}

data class GetMyProfileQuery(
    val userId: UserId
)
