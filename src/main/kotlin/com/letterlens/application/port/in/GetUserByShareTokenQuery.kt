package com.letterlens.application.port.`in`

import com.letterlens.domain.ShareToken

data class GetUserByShareTokenQuery(
    val shareToken: ShareToken
)
