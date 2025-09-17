package com.l10s.PoCRedis.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisEventConfig {

    private RedisExpirationListener redisExpirationListener;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Suscribirse a eventos de expiración (__keyevent@*__:expired)
        // El patrón __keyevent@*__:expired captura cuando cualquier clave expira
        container.addMessageListener(redisExpirationListener, new PatternTopic("__keyevent@*__:expired"));
        
        return container;
    }
}
