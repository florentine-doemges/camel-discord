package net.doemges.cameldiscord

import net.dv8tion.jda.api.JDA
import org.apache.camel.Endpoint
import org.apache.camel.support.DefaultComponent
import org.springframework.stereotype.Component

/**
 * This component creates a new JDAEndpoint using the provided parameters.
 * The parameters are used to filter messages from the specified guild and channel.
 */
@Component("jda")
class JDAComponent(private val jdaCore: JDA) : DefaultComponent() {
    public override fun createEndpoint(uri: String, remaining: String, parameters: Map<String, Any>): Endpoint =
        JDAEndpoint(
            uri,
            this,
            parameters["guildId"]?.toString(),
            parameters["channelId"]?.toString(),
            jdaCore
        )


}