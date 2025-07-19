package com.letterlens.domain

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService {
    
    fun createUserFromOAuth(
        oauthProvider: OAuthProvider,
        username: String,
        profileImageUrl: String?
    ): User {
        validateUsername(username)
        
        return User.createWithOAuth(
            username = username,
            profileImageUrl = profileImageUrl,
            oauthProvider = oauthProvider
        )
    }
    
    // 현재 설계상 User는 하나의 OAuth Provider만 가질 수 있음
    // 추후 여러 Provider 연동이 필요하면 User 모델 수정 필요

    fun updateUserProfile(user: User, username: String, profileImageUrl: String?): User {
        validateUsername(username)
        
        return user.updateProfile(username, profileImageUrl)
    }
    
    fun canUserAccessResume(user: User, resume: Resume): Boolean {
        return resume.canBeAccessedBy(user.id)
    }
    
    fun canUserEditResume(user: User, resume: Resume): Boolean {
        return resume.isOwnedBy(user.id)
    }
    
    fun canUserDeleteResume(user: User, resume: Resume): Boolean {
        return resume.isOwnedBy(user.id)
    }
    
    private fun validateUsername(username: String) {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length >= 2) { "Username must be at least 2 characters" }
        require(username.length <= 50) { "Username must be at most 50 characters" }
        require(!username.contains("관리자") && !username.contains("시스템")) {
            "Username cannot contain reserved words"
        }
    }
}
