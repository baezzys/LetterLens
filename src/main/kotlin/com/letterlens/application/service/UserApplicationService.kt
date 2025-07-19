package com.letterlens.application.service

import com.letterlens.application.port.`in`.*
import com.letterlens.application.port.out.*
import com.letterlens.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class UserApplicationService(
    private val userRepository: UserRepository,
    private val userService: UserService
) : UserUseCase, LoginUseCase, UpdateProfileUseCase {
    
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    override suspend fun loginWithOAuth(command: OAuthLoginCommand): LoginResult {
        // 1. 기존 사용자 확인
        val existingUser = userRepository.findByOAuthProvider(
            command.provider, 
            command.originalId
        )
        
        if (existingUser != null) {
            // 기존 사용자 로그인
            return LoginResult(
                userId = existingUser.id,
                username = existingUser.username,
                profileImageUrl = existingUser.profileImageUrl,
                isNewUser = false
            )
        }
        
        // 2. 새 사용자 생성
        val oauthProvider = OAuthProvider(
            provider = command.provider,
            originalId = command.originalId,
            email = command.email,
            connectedAt = LocalDateTime.now()
        )
        
        val newUser = userService.createUserFromOAuth(
            oauthProvider = oauthProvider,
            username = command.username,
            profileImageUrl = command.profileImageUrl
        )
        
        // 3. 저장
        val savedUser = userRepository.save(newUser)
        
        return LoginResult(
            userId = savedUser.id,
            username = savedUser.username,
            profileImageUrl = savedUser.profileImageUrl,
            isNewUser = true
        )
    }
    
    override suspend fun updateProfile(command: UpdateProfileCommand): UserResult {
        // 1. 사용자 조회
        val user = userRepository.findById(command.userId)
            ?: throw IllegalArgumentException("User not found")
        
        // 2. 프로필 업데이트
        val updatedUser = userService.updateUserProfile(
            user = user,
            username = command.username,
            profileImageUrl = command.profileImageUrl
        )
        
        // 3. 저장
        val savedUser = userRepository.save(updatedUser)
        
        return mapToUserResult(savedUser)
    }
    
    override suspend fun getMyProfile(query: GetMyProfileQuery): User {
        val user = userRepository.findById(query.userId)
            ?: throw IllegalArgumentException("User not found")
        
        return user
    }
    
    override suspend fun getUserByShareToken(query: GetUserByShareTokenQuery): UserResult? {
        // ShareToken으로 사용자를 찾는 로직은 Resume 도메인에 의존하므로
        // 현재는 구현하지 않음. 추후 필요시 ResumeRepository를 주입받아 구현
        return null
    }
    
    private fun mapToUserResult(user: User): UserResult {
        return UserResult(
            userId = user.id,
            username = user.username,
            profileImageUrl = user.profileImageUrl,
            provider = user.oauthProvider.provider,
            createdAt = user.createdAt.format(dateTimeFormatter)
        )
    }
}
