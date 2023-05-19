package net.doemges.cameldiscord

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.camel.Processor
import org.apache.camel.support.DefaultConsumer
import org.slf4j.LoggerFactory

/**
 * This consumer receives messages from the specified guild and channel, and then processes them.
 * It does this in a non-blocking manner by using coroutines.
 */
class JDAConsumer(
    endpoint: JDAEndpoint,
    processor: Processor,
    jda: JDA,
    private val guildId: String? = null,
    private val channelId: String? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : DefaultConsumer(endpoint, processor), CoroutineScope by scope {

    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        jda.addEventListener(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                launch(CoroutineExceptionHandler { _, exception ->
                    logger.error("Exception in coroutine: ${exception.message}", exception)
                }) {
                    if (shouldProcessMessage(event)) {
                        try {
                            val exchange = endpoint.createExchange()
                            exchange.getIn().body = event.message
                            processor.process(exchange)
                            logger.info("Message processed successfully from channel with id: ${event.channel.id}")
                        } catch (exception: Exception) {
                            logger.error("Exception occurred while processing message: ${exception.message}", exception)
                            throw exception
                        }
                    }
                }
            }

            private fun shouldProcessMessage(event: MessageReceivedEvent): Boolean =
                guildId?.let { event.guild.id == it } ?: true && channelId?.let { event.channel.id == it } ?: true
        })
    }
}