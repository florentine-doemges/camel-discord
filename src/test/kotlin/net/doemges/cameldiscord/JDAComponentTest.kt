package net.doemges.cameldiscord

import assertk.all
import assertk.assertThat
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import net.dv8tion.jda.api.JDA
import org.apache.camel.CamelContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JDAComponentTest {

    private val fixture = Fixtures.fixtureWithFaker()

    private lateinit var component: JDAComponent
    private lateinit var jda: JDA
    private lateinit var cc: CamelContext

    @BeforeEach
    fun setup() {
        cc = mockk<CamelContext>().apply {
            every { name } returns fixture()
        }
        jda = mockk<JDA>().apply {
            every { shutdownNow() } just Runs
        }
        component = spyk(JDAComponent("test-token")).apply {
            every { createEndpoint(any(), any(), any()) } returns JDAEndpoint(
                fixture(),
                this,
                fixture(),
                fixture(),
                fixture(),
                jda
            )
        }
    }

    @Test
    fun `JDAComponent should create a valid endpoint when given valid parameters`() {
        // Arrange


        val validParams = mapOf("guildId" to "valid-guildId", "channelId" to "valid-channelId")

        val mockedJDAEndpoint = JDAEndpoint(fixture(), component, fixture(), fixture(), fixture(), jda)

        // Define what should be returned when createEndpoint() method is called on the mock
        every { component.createEndpoint("dummyUri", "dummyRemaining", validParams) } returns mockedJDAEndpoint

        // Act
        val endpoint = component.createEndpoint("dummyUri", "dummyRemaining", validParams)

        // Assert
        assertThat(endpoint).all {
            isNotNull()
            isInstanceOf(JDAEndpoint::class)
        }
    }

    @Test
    fun `JDAComponent should handle null or invalid parameters`() {
        val invalidParams = mapOf("invalidKey" to "invalidValue")

        // Mock the case when invalid parameters are passed
        every {
            component.createEndpoint(
                "dummyUri",
                "dummyRemaining",
                invalidParams
            )
        } answers { throw IllegalArgumentException("Invalid parameters!") }

        // Act and Assert
        assertThat { component.createEndpoint("dummyUri", "dummyRemaining", invalidParams) }
            .isFailure()
            .isInstanceOf(IllegalArgumentException::class)
    }

}
