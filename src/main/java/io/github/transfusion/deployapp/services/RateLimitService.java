package io.github.transfusion.deployapp.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Works together with {@link io.github.transfusion.deployapp.config.bucket4j.Bucket4JCacheConfiguration}
 */
@Service
public class RateLimitService {

    public enum AVAILABLE_CONFIGURATIONS {
        EMAIL_RATELIMIT,
        INTEGRATION_TEST_RATELIMIT,
    }

    @Autowired
    private ProxyManager<byte[]> proxyManager;

    @Value("${custom_app.rate_limit_key_prefix}")
    private String rateLimitKeyPrefix;

    public Bucket resolveBucket(String methodName, String key, AVAILABLE_CONFIGURATIONS config) {
        String finalKey = rateLimitKeyPrefix + methodName + "_" + key;
        return proxyManager.builder().build(finalKey.getBytes(StandardCharsets.UTF_8), getConfigSupplier(config));
    }

    @Value("${custom_app.email_retry_rate_limit_duration}")
    private Integer emailRetryRateLimitDuration;

    @Value("${custom_app.email_retry_rate_limit_capacity}")
    private Integer email_retry_rate_limit_capacity;

    private Supplier<BucketConfiguration> getConfigSupplier(AVAILABLE_CONFIGURATIONS config) {
        switch (config) {
            case INTEGRATION_TEST_RATELIMIT:
                return () -> BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(2, Duration.ofSeconds(1))).build();
            case EMAIL_RATELIMIT:
            default:
                return () -> BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(email_retry_rate_limit_capacity, Duration.ofMinutes(emailRetryRateLimitDuration))).build();
        }
    }
}
