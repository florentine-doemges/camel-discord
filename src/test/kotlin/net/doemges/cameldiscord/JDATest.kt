package net.doemges.cameldiscord

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.apache.camel.CamelConfiguration
import org.apache.camel.spring.boot.CamelAutoConfiguration
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@SpringBootTest(classes = [JDAComponent::class, CamelAutoConfiguration::class])
@TestPropertySource(properties = ["discord.token=YOUR_DISCORD_TOKEN"])
class JDATest {

    @Autowired
    private lateinit var jdaComponent: JDAComponent

    @Test
    fun `send and receive message test`() {
        val testGuildId = "YOUR_GUILD_ID"
        val testChannelId = "YOUR_CHANNEL_ID"
        val testMessage = "Test message"

        val receivedMessageRef = AtomicReference<String>()

        // Create consumer
        val jdaConsumer = jdaComponent.createEndpoint(
            "jda:test",
            "test",
            mapOf("guildId" to testGuildId, "channelId" to testChannelId)
        )
            .createConsumer { exchange ->
                val receivedMessage = exchange.`in`.getBody(String::class.java)
                receivedMessageRef.set(receivedMessage)
            }
        jdaConsumer.start()

        // Create producer
        val jdaProducer = jdaComponent.createEndpoint(
            "jda:test",
            "test",
            mapOf("channelId" to testChannelId)
        )
            .createProducer()
        jdaProducer.start()

        // Send a test message
        val exchange = jdaProducer.endpoint.createExchange()
        exchange.`in`.body = testMessage
        jdaProducer.process(exchange)

        // Wait for the message to be received and processed
        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted {
                assertThat(receivedMessageRef.get()).isEqualTo(testMessage)
            }

        // Cleanup
        jdaConsumer.stop()
        jdaProducer.stop()
    }
}
