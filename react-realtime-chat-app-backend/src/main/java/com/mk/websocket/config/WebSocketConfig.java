package com.mk.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration // Tells Spring to treat this as a configuration class to build framework beans at startup
@EnableWebSocketMessageBroker // Activates WebSocket message handling, backed by a higher-level messaging sub-protocol (STOMP)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Keeps any default behavior from the parent class implementation
        WebSocketMessageBrokerConfigurer.super.configureMessageBroker(registry);

        // THE INBOUND DESTINATION (React -> Java Controller)
        // Any message sent from the client that should be processed by your Java code (@MessageMapping)
        // MUST start with this prefix. Example: Client sends to "/app/user.addUser"
        registry.enableSimpleBroker("/user", "/topic");

        // THE OUTBOUND DESTINATION (Broker -> React Clients)
        // Configures an internal, in-memory traffic cop (Message Broker).
        // The client's frontend will "subscribe" to topics starting with these prefixes to listen for updates.
        registry.setApplicationDestinationPrefixes("/app");

        // USER-SPECIFIC DESTINATION ROUTING
        // Allows you to target specific, individual users rather than broadcasting to the whole room.
        // Spring uses this prefix internally to handle point-to-point messaging (e.g., private user cues).
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Keeps any default behavior from the parent class implementation
        WebSocketMessageBrokerConfigurer.super.registerStompEndpoints(registry);

        // This defines the INITIAL HTTP URL that the React frontend hits to initiate the connection handshake.
        // The client targets "ws://localhost:8088/ws"
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173")
                // Fallback mechanism: If a user's web browser or network firewall block native WebSockets,
                // .withSockJS() forces the connection to gracefully downgrade to HTTP Long-Polling so chat still works!
                .withSockJS();
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // We want to force all WebSocket messages to handle data as clean JSON payloads.
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        // Tells Spring: "If no data type is specified, assume it's JSON (application/json)"
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        // Jackson is Java's premier library for converting Java Objects into JSON strings (and vice-versa)
        JsonMapper jsonMapper = JsonMapper.builder().build();
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(jsonMapper);
        converter.setContentTypeResolver(resolver);

        // Adds our custom JSON converter to Spring's pipeline of message processors
        messageConverters.add(converter);

        // Returning false tells Spring: "Replace defaults with mine."
        return false;
    }
}
