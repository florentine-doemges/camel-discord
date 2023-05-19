package net.doemges.cameldiscord

import net.dv8tion.jda.api.entities.TextChannel
import org.apache.camel.Exchange
import org.apache.camel.support.DefaultProducer

/**
 * This producer sends messages to a specific channel on Discord.
 * If the channel is not specified or is null, an error will be logged and an exception thrown.
 */
class JDAProducer(endpoint: JDAEndpoint, private val channel: TextChannel?) : DefaultProducer(endpoint) {

    private val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    override fun process(exchange: Exchange) {
        val message = exchange.getIn().getBody(String::class.java)
        if (channel != null) {
            channel.sendMessage(message).queue()
            logger.info("Message sent successfully to channel with id: ${channel.id}")
        } else {
            logger.error("Channel is null. Message cannot be sent.")
            throw RuntimeException("Channel is null. Message cannot be sent.")
        }
    }

}