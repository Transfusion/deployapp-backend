package io.github.transfusion.deployapp.utils;

import io.github.bucket4j.ConsumptionProbe;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

public class RateLimitUtils {
    public static <T> ResponseEntity<T> tooManyRequestsResponse(ConsumptionProbe probe,
                                                                @Nullable T body) {
        if (probe.isConsumed()) throw new IllegalArgumentException("Probe was successfully consumed.");
        long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(responseHeaders).body(null);
    }
}
