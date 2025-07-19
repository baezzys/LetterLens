package com.letterlens.infrastructure.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@Configuration
@EnableR2dbcRepositories(basePackages = ["com.letterlens.infrastructure.persistence.repository"])
class R2dbcConfig : AbstractR2dbcConfiguration() {
    
    override fun connectionFactory(): ConnectionFactory {
        // Spring Boot auto-configuration will handle this
        throw UnsupportedOperationException("Use Spring Boot auto-configuration")
    }
    
    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        
        val populator = ResourceDatabasePopulator()
        // Add any initialization scripts if needed
        
        initializer.setDatabasePopulator(populator)
        return initializer
    }
}
