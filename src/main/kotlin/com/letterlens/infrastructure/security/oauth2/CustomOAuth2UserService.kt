package com.letterlens.infrastructure.security.oauth2

import com.letterlens.application.port.out.UserRepository
import com.letterlens.domain.OAuthProvider
import com.letterlens.domain.OAuthProviderType
import com.letterlens.domain.User
import kotlinx.coroutines.reactor.mono
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository
) : DefaultReactiveOAuth2UserService() {
    
    override fun loadUser(userRequest: OAuth2UserRequest): Mono<OAuth2User> {
        return super.loadUser(userRequest)
            .flatMap { oAuth2User ->
                mono {
                    processOAuth2User(userRequest, oAuth2User)
                }
            }
    }
    
    private suspend fun processOAuth2User(
        userRequest: OAuth2UserRequest,
        oAuth2User: OAuth2User
    ): CustomOAuth2User {
        val providerType = OAuthProviderType.fromString(
            userRequest.clientRegistration.registrationId
        ) ?: throw OAuth2AuthenticationException("Unsupported provider")
        
        val oAuth2UserInfo = when (providerType) {
            OAuthProviderType.KAKAO -> KakaoOAuth2UserInfo(oAuth2User.attributes)
            else -> throw OAuth2AuthenticationException("Unsupported provider: $providerType")
        }
        
        val user = findOrCreateUser(providerType, oAuth2UserInfo)
        
        return CustomOAuth2User(
            userId = user.id.value,
            username = user.username,
            attributes = oAuth2User.attributes,
            authorities = oAuth2User.authorities
        )
    }
    
    private suspend fun findOrCreateUser(
        providerType: OAuthProviderType,
        userInfo: OAuth2UserInfo
    ): User {
        val existingUser = userRepository.findByOAuthProvider(
            providerType,
            userInfo.getId()
        )
        
        return existingUser ?: createNewUser(providerType, userInfo)
    }
    
    private suspend fun createNewUser(
        providerType: OAuthProviderType,
        userInfo: OAuth2UserInfo
    ): User {
        val oauthProvider = OAuthProvider(
            provider = providerType,
            originalId = userInfo.getId(),
            email = userInfo.getEmail(),
            connectedAt = LocalDateTime.now()
        )
        
        val username = generateUniqueUsername(userInfo.getName())
        
        val newUser = User.createWithOAuth(
            username = username,
            profileImageUrl = userInfo.getImageUrl(),
            oauthProvider = oauthProvider
        )
        
        return userRepository.save(newUser)
    }
    
    private suspend fun generateUniqueUsername(baseName: String): String {
        var username = baseName
        var counter = 1
        
        while (userRepository.existsByUsername(username)) {
            username = "${baseName}_${counter}"
            counter++
        }
        
        return username
    }
}
