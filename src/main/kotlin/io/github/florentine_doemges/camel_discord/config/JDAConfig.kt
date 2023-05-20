package io.github.florentine_doemges.camel_discord.config

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.security.auth.login.LoginException

@Configuration
class JDAConfig(@Value("\${discord.token}") private val token: String) {

    companion object{
        const val MAX_RETRIES = 5
        const val RETRY_DELAY_MS = 5000L
    }

    private val logger = LoggerFactory.getLogger(JDAConfig::class.java)

    @Bean
    fun jdaCore(): JDA {
        var retryCount = 0
        while (true) {
            try {
                return JDABuilder.createDefault(token).build()
            } catch (e: LoginException) {
                if (retryCount++ >= MAX_RETRIES) {
                    throw e
                }
                logger.warn("Login failed, retrying...")
                Thread.sleep(RETRY_DELAY_MS)
            }
        }
    }

}