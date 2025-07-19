package com.letterlens

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LetterlensApplication

fun main(args: Array<String>) {
    runApplication<LetterlensApplication>(*args)
}
