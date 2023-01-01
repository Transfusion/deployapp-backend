package io.github.transfusion.deployapp.config.bucket4j;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4JRedisConfiguration {

    @Value("${spring.redis.host}")
    private String redisHostName;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean("bucket4jRedisClient")
    public RedisClient bucket4jRedisClient() {
        RedisURI redisURI = RedisURI.create(redisHostName, redisPort);
        redisURI.setPassword(redisPassword);
        return RedisClient.create(redisURI);
    }

}
