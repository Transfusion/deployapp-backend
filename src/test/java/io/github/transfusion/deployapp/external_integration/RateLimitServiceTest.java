package io.github.transfusion.deployapp.external_integration;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.transfusion.deployapp.config.bucket4j.Bucket4JCacheConfiguration;
import io.github.transfusion.deployapp.config.bucket4j.Bucket4JRedisConfiguration;
import io.github.transfusion.deployapp.services.RateLimitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"rate-limit-test"})
@SpringBootTest
@ExtendWith({/*SpringExtension.class,*/ MockitoExtension.class})
@ContextConfiguration(classes = {Bucket4JRedisConfiguration.class})
@Import({RateLimitService.class, Bucket4JCacheConfiguration.class, Bucket4JRedisConfiguration.class})
public class RateLimitServiceTest {

    @Autowired
    private RateLimitService rateLimitService;

    @Test
    public void testHitLimit() throws InterruptedException {
        Bucket b = rateLimitService.resolveBucket("testHitLimit",
                "foo", RateLimitService.AVAILABLE_CONFIGURATIONS.INTEGRATION_TEST_RATELIMIT);

        // consume two tokens in rapid succession
        ConsumptionProbe probe = b.tryConsumeAndReturnRemaining(1);
        assertTrue(probe.isConsumed());
        probe = b.tryConsumeAndReturnRemaining(1);
        assertTrue(probe.isConsumed());
        // the third attempt to consume should return false
        probe = b.tryConsumeAndReturnRemaining(1);
        assertFalse(probe.isConsumed());
        // sleep for 1s
        Thread.sleep(1000);
        probe = b.tryConsumeAndReturnRemaining(1);
        assertTrue(probe.isConsumed());
    }

}
