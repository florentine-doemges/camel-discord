package io.github.florentine_doemges.camel_discord

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.TextChannel
import org.apache.camel.Exchange
import org.apache.camel.support.DefaultProducer
import org.slf4j.LoggerFactory

class JDAProducer(
    endpoint: JDAEndpoint,
    jda: JDA,
    private val channelId: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : DefaultProducer(endpoint), CoroutineScope by scope {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private var channel: TextChannel

    companion object{
        const val MAX_RETRIES = 5
        const val RETRY_DELAY_MS = 5000L
    }

    init {
        jda.awaitReady()
        channel = jda.getTextChannelById(channelId) ?: error("unknown channel id: $channelId")
        logger.info("Channel with id: $channelId found")
    }

    override fun process(exchange: Exchange) {
        val message = exchange.getIn()
            .getBody(String::class.java)

        if (message != null) {
            logger.info("Preparing to send message to channel with id: ${channel.id}")
            try {
                launch {
                    var retryCount = 0
                    while (true) {
                        try {
                            val messageAction = channel.sendMessage(message)
                                .complete()
                            logger.info("Message successfully sent to channel with id ${channel.id}: ${messageAction.referencedMessage?.contentDisplay}")
                            break
                        } catch (throwable: Exception) {
                            if (retryCount++ >= MAX_RETRIES) {
                                logger.error("Failed to send message to channel with id: ${channel.id}", throwable)
                                throw throwable
                            }
                            logger.warn("Sending failed, retrying...")
                            delay(RETRY_DELAY_MS)
                        }
                    }

                }
            } catch (e: Exception) {
                logger.error("Exception occurred while sending message to channel with id: ${channel.id}", e)
            }
        } else {
            logger.warn("No message received for sending to channel with id: ${channel.id}")
        }
    }
}
