package net.doemges.cameldiscord

import org.apache.camel.Endpoint
import org.apache.camel.support.DefaultComponent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * This component creates a new JDAEndpoint using the provided parameters.
 * The parameters are used to filter messages from the specified guild and channel.
 */
@Component("jda")
class JDAComponent(@Value("\${discord.token}") private val token: String) : DefaultComponent() {
    public override fun createEndpoint(uri: String, remaining: String, parameters: Map<String, Any>): Endpoint {
        val guildId = parameters["guildId"]?.toString()
        val channelId = parameters["channelId"]?.toString()
        return JDAEndpoint(uri, this, token, guildId, channelId)
    }


}