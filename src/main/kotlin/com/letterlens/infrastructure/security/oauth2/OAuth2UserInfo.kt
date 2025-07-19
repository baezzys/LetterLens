package com.letterlens.infrastructure.security.oauth2

abstract class OAuth2UserInfo(
    protected val attributes: Map<String, Any>
) {
    abstract fun getId(): String
    abstract fun getName(): String
    abstract fun getEmail(): String?
    abstract fun getImageUrl(): String?
}

class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {
    
    override fun getId(): String {
        return attributes["id"].toString()
    }
    
    override fun getName(): String {
        val properties = attributes["properties"] as? Map<*, *>
        return properties?.get("nickname")?.toString() ?: "카카오사용자"
    }
    
    override fun getEmail(): String? {
        val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
        return kakaoAccount?.get("email")?.toString()
    }
    
    override fun getImageUrl(): String? {
        val properties = attributes["properties"] as? Map<*, *>
        return properties?.get("profile_image")?.toString()
    }
}
