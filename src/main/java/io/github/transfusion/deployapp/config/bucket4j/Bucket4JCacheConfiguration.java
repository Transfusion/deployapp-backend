package io.github.transfusion.deployapp.config.bucket4j;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Bucket4JCacheConfiguration {
//    @Bean(name="SpringCM")
//    public CacheManager cacheManager(Config config) {
//        CacheManager manager = Caching.getCachingProvider().getCacheManager();
//        manager.createCache("cache", RedissonConfiguration.fromConfig(config));
//        manager.createCache("userList", RedissonConfiguration.fromConfig(config));
//        return manager;
//    }

    @Autowired
    @Qualifier("bucket4jRedisClient")
    private RedisClient bucket4jRedisClient;

    @Bean
    LettuceBasedProxyManager proxyManager() {
        return LettuceBasedProxyManager.builderFor(bucket4jRedisClient)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
                .build();
    }
}
