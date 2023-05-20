package net.doemges.cameldiscord

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
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
) : DefaultConsumer(endpoint, processor), EventListener, CoroutineScope by scope {

    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object{
        const val MAX_RETRIES = 5
        const val RETRY_DELAY_MS = 5000L
    }


    init {
        logger.info("launching event Listener")
        jda.addEventListener(this)
        jda.awaitReady()
        logger.info("jda is ready")
    }

    private fun onMessageReceived(event: MessageReceivedEvent) {
        logger.info("Message received: ${event.message.contentDisplay}")
        launch(CoroutineExceptionHandler { _, exception ->
            logger.error("Exception in coroutine: ${exception.message}", exception)
        }) {
            if (shouldProcessMessage(event)) {
                var retryCount = 0
                while (true) {
                    try {
                        val exchange = endpoint.createExchange()
                        exchange.getIn().body = event.message
                        logger.info("Start processing message from channel with id: ${event.channel.id}")
                        processor.process(exchange)
                        logger.info("Message processed successfully from channel with id: ${event.channel.id}")
                        break
                    } catch (exception: Exception) {
                        if (retryCount++ >= MAX_RETRIES) {
                            logger.error("Exception occurred while processing message: ${exception.message}", exception)
                            throw exception
                        }
                        logger.warn("Processing failed, retrying...")
                        delay(RETRY_DELAY_MS)
                    }
                }
            } else {
                logger.info("Message from channel ${event.channel.id} ignored")
            }
        }
    }

    private fun shouldProcessMessage(event: MessageReceivedEvent): Boolean =
        guildId?.let { event.guild.id == it } ?: true && channelId?.let { event.channel.id == it } ?: true

    override fun onEvent(event: GenericEvent) {
        logger.info("Event received: ${event.javaClass.simpleName}")
        if (event is MessageReceivedEvent) {
            logger.info("Message received: ${event.message.contentDisplay}")
            onMessageReceived(event)
        }
    }

}
