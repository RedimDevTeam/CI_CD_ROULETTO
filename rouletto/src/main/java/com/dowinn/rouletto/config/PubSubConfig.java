package com.dowinn.rouletto.config;

import com.dowinn.rouletto.pubsub.Listener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class PubSubConfig {

    @Value("${redis.host}")
    private String redisHostName;

    @Value("${redis.port}")
    private Integer redisPort;

    @Bean
     JedisConnectionFactory pubsubJedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHostName);
        redisConfig.setPort(redisPort);
       // redisConfig.setDatabase(2);
        return new JedisConnectionFactory(redisConfig);
    }



    @Bean
    @Primary
     RedisTemplate<String, Object> pubsubRedisTemplate(JedisConnectionFactory pubsubJedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(pubsubJedisConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }


    @Bean
    Listener listener() {
        return new Listener();
    }

    @Bean
    MessageListenerAdapter messageListenerAdapter(Listener listener) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(listener,"handleMessage");
        messageListenerAdapter.setSerializer(new GenericJackson2JsonRedisSerializer());
        return messageListenerAdapter;
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(MessageListenerAdapter listener,JedisConnectionFactory pubsubJedisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(pubsubJedisConnectionFactory);
        container.addMessageListener(listener, ChannelTopic.of("playerSession"));
        return container;
    }


}
