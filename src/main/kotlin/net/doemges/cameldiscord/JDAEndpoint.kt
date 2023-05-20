package net.doemges.cameldiscord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.apache.camel.Processor
import org.apache.camel.Producer
import org.apache.camel.support.DefaultConsumer
import org.apache.camel.support.DefaultEndpoint
import org.slf4j.LoggerFactory
import javax.security.auth.login.LoginException


/**
 * This class represents an endpoint for the JDA (Java Discord API). It provides the basic structure
 * for creating consumers and producers to interact with Discord.
 *
 * An instance of JDA is created using the provided token and the consumer or producer is built
 * depending on the guildId and channelId. If these are not provided, messages from all channels
 * and guilds will be processed by the consumer, while the producer will throw an error.
 *
 * The class also handles the graceful shutdown of the JDA instance upon JVM termination.
 */
class JDAEndpoint(
    uri: String,
    component: JDAComponent,
    private val guildId: String?,
    private val channelId: String?,
    private val jda: JDA
) :
    DefaultEndpoint(uri, component) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun isLenientProperties(): Boolean = true

    init {
        logger.info("Creating JDAEndpoint with guildId: $guildId and channelId: $channelId")
        jda.awaitReady()
        logger.info("JDA is ready")
        Runtime.getRuntime()
            .addShutdownHook(Thread {
                jda.shutdownNow()
            })
    }

    override fun createConsumer(processor: Processor): DefaultConsumer =
        JDAConsumer(this, processor, jda, guildId, channelId)

    override fun createProducer(): Producer {
        logger.info("Creating producer for channel with id: $channelId")
        if (channelId == null) {
            logger.error("Channel ID is not set")
            error("Channel ID is not set")
        }
        logger.info("Channel ID is set to: $channelId")
        return JDAProducer(this, jda, channelId)
    }
}