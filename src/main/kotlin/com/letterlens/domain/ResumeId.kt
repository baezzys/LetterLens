package com.letterlens.domain

@JvmInline
value class ResumeId(val value: Long) {
    init {
        require(value > 0) { "Resume ID must be positive" }
    }
}
