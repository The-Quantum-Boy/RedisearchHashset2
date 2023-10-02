package com.redisearch.config;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
@Data
public class RedisConfiguration {

    @Bean
    JedisPooled jedisPooled() {
        return new JedisPooled("localhost", 6379);
    }


}
