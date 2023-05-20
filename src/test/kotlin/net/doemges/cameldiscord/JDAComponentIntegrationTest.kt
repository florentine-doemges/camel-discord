package net.doemges.cameldiscord

import assertk.assertThat
import assertk.assertions.isSuccess
import assertk.assertions.isTrue
import net.doemges.cameldiscord.config.JDAConfig
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.spring.boot.CamelAutoConfiguration
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.TestPropertySource
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@SpringBootTest(
    classes = [
        JDAComponent::class,
        CamelAutoConfiguration::class,
        JDAComponentIntegrationTest.TestConfig::class,
        JDAConfig::class]
)
@TestPropertySource("classpath:application-test.properties")
class JDAComponentIntegrationTest {

    @Autowired
    private lateinit var jdaComponent: JDAComponent

    companion object {
        var messageReceived: AtomicBoolean = AtomicBoolean(false)
        var messageSend: AtomicBoolean = AtomicBoolean(false)
    }

    @Configuration
    class TestConfig {
        @Bean
        fun myRouteBuilder(): RouteBuilder {
            return CustomRouteBuilder(messageReceived, messageSend)
        }
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    fun `Should process the sent message`() {
        val sentMessage = "Test message"

        val producerTemplate = jdaComponent.camelContext.createProducerTemplate()
        producerTemplate.sendBody("direct:start", sentMessage)

        await().atMost(60, TimeUnit.SECONDS).untilAsserted(){
            assertThat { messageSend.get() }
                .isSuccess()
                .isTrue()
            assertThat { messageReceived.get() }
                .isSuccess()
                .isTrue()
        }
    }
}

class CustomRouteBuilder(
    private val messageReceived: AtomicBoolean,
    private val messageSend: AtomicBoolean
) : RouteBuilder() {
    companion object {
        const val uri = "jda://discord?channelId=1102449789146247168"
    }

    override fun configure() {
        from("direct:start")
            .process { messageSend.set(true) }
            .to("log:net.doemges.cameldiscord.JDAComponentIntegrationTest.before?showAll=true&multiline=true")
            .to(uri)

        from(uri)
            .to("log:net.doemges.cameldiscord.JDAComponentIntegrationTest.after?showAll=true&multiline=true")
            .process { messageReceived.set(true) }
    }

}
