package com.letterlens.adapter.`in`.web

import com.letterlens.application.port.`in`.GetMyProfileQuery
import com.letterlens.application.port.`in`.UserUseCase
import com.letterlens.domain.UserId
import com.letterlens.infrastructure.security.jwt.JwtTokenProvider
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userUseCase: UserUseCase,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    @GetMapping("/me")
    suspend fun getMyProfile(
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<UserResponse> {
        val result = userUseCase.getMyProfile(
            GetMyProfileQuery(UserId(userId))
        )
        
        return ResponseEntity.ok(
            UserResponse(
                userId = result.userId.value,
                username = result.username,
                profileImageUrl = result.profileImageUrl,
                provider = result.provider.name,
                createdAt = result.createdAt
            )
        )
    }
    
    data class UserResponse(
        val userId: String,
        val username: String,
        val profileImageUrl: String?,
        val provider: String,
        val createdAt: String
    )
}
