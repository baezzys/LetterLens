package com.letterlens.domain

import java.util.*

@JvmInline
value class ShareToken(val value: String) {
    init {
        require(value.isNotBlank()) { "Share token cannot be blank" }
    }
    
    companion object {
        fun generate(): ShareToken {
            return ShareToken(UUID.randomUUID().toString())
        }
    }
}
