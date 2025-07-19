package com.letterlens.infrastructure.security.oauth2

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomOAuth2User(
    private val userId: String,
    private val username: String,
    private val attributes: Map<String, Any>,
    private val authorities: Collection<GrantedAuthority>
) : OAuth2User {
    
    override fun getName(): String = userId
    
    override fun getAttributes(): Map<String, Any> = attributes
    
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    
    fun getUserId(): String = userId
    
    fun getUsername(): String = username
}
