package com.letterlens.infrastructure.security.oauth2

import com.letterlens.infrastructure.security.jwt.JwtTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${app.oauth2.redirect-uri}") private val redirectUri: String
) : ServerAuthenticationSuccessHandler {
    
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {
        val oAuth2User = authentication.principal as CustomOAuth2User
        
        val token = jwtTokenProvider.createToken(
            oAuth2User.getUserId(),
            oAuth2User.getUsername()
        )
        
        val targetUrl = "$redirectUri?token=$token"
        
        val response = webFilterExchange.exchange.response
        response.statusCode = org.springframework.http.HttpStatus.FOUND
        response.headers.location = URI.create(targetUrl)
        
        return response.setComplete()
    }
}
