package com.rushtix.core.config;

import com.rushtix.core.feature.seat.api.SeatMapLiveController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisPubSubConfig {

    public static final String SEAT_UPDATE_CHANNEL = "seat-updates";

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic(SEAT_UPDATE_CHANNEL);
    }

    @Bean
    public MessageListenerAdapter messageListener(SeatMapLiveController seatController) {
        return new MessageListenerAdapter((org.springframework.data.redis.connection.MessageListener) (message, pattern) -> {
            String jsonPayload = new String(message.getBody());

            String eventId = extractEventIdFromJson(jsonPayload);

            // Push to browsers!
            seatController.broadcastUpdateToClients(eventId, jsonPayload);
        });
    }

    // 3. Register the listener with Spring Boot's Redis Container
    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic());
        return container;
    }

    private String extractEventIdFromJson(String json) {
        // Quick regex/string parse for demo - Use Jackson ObjectMapper to do this cleanly
        try {
            String[] parts = json.split("\"eventId\":\"");
            return parts[1].split("\"")[0];
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}