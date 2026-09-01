package com.dowinn.rouletto.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class Sender {
    private static final String channel = "playerSession";
    @Autowired
    private RedisTemplate<String,Object> pubsubRedisTemplate;

    public Long sendMessage(Map<String,Object> message)  {
        return pubsubRedisTemplate.convertAndSend(channel,message);
    }

}
