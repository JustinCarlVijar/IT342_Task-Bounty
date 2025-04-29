package edu.cit.taskbounty.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@EnableScheduling
public class RateLimitingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Map to store request counts and timestamps per IP address
    private final Map<String, RequestInfo> requestCountsPerIpAddress = new ConcurrentHashMap<>();

    // Maximum requests allowed per time window
    @Value("${rate.limiting.max.requests:5}")
    private int maxRequestsPerMinute;

    // Time window for rate limiting in nanoseconds
    @Value("${rate.limiting.time.window.seconds:60}")
    private long timeWindowSeconds;

    // Inner class to store request count and window start time
    private static class RequestInfo {
        AtomicInteger count;
        long windowStartTime;

        RequestInfo() {
            this.count = new AtomicInteger(0);
            this.windowStartTime = System.nanoTime();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String clientIpAddress = getClientIpAddress(httpServletRequest);
        String requestUri = httpServletRequest.getRequestURI();
        long timeWindowNanos = timeWindowSeconds * 1_000_000_000L;

        RequestInfo requestInfo = requestCountsPerIpAddress.compute(clientIpAddress, (key, value) -> {
            if (value == null || isWindowExpired(value, timeWindowNanos)) {
                return new RequestInfo();
            }
            return value;
        });

        // Increment the request count
        int requests = requestInfo.count.incrementAndGet();

        // Check if the request limit has been exceeded
        if (requests > maxRequestsPerMinute) {
            logger.warn("Rate limit exceeded for IP: {} on endpoint: {}. Requests: {}",
                    clientIpAddress, requestUri, requests);
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setHeader("Retry-After", String.valueOf(timeWindowSeconds));
            httpServletResponse.getWriter().write(
                    "{\"error\": \"Too many requests. Please try again in " + timeWindowSeconds + " seconds.\"}"
            );
            return;
        }

        logger.debug("Request allowed for IP: {} on endpoint: {}. Current count: {}",
                clientIpAddress, requestUri, requests);
        chain.doFilter(request, response);
    }

    // Helper method to check if the time window has expired
    private boolean isWindowExpired(RequestInfo requestInfo, long timeWindowNanos) {
        long currentTime = System.nanoTime();
        return (currentTime - requestInfo.windowStartTime) > timeWindowNanos;
    }

    // Helper method to get client IP address, accounting for proxies
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For (take the first one)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress != null ? ipAddress : "unknown";
    }

    // Scheduled task to clean up stale entries
    @Scheduled(fixedRate = 3_600_000) // Run every hour
    public void cleanupOldEntries() {
        long timeWindowNanos = timeWindowSeconds * 1_000_000_000L;
        int initialSize = requestCountsPerIpAddress.size();
        requestCountsPerIpAddress.entrySet().removeIf(entry -> isWindowExpired(entry.getValue(), timeWindowNanos));
        logger.debug("Cleaned up stale rate limiting entries. Initial size: {}, Final size: {}",
                initialSize, requestCountsPerIpAddress.size());
    }

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) throws ServletException {
        logger.info("RateLimitingFilter initialized with max requests: {} per {} seconds",
                maxRequestsPerMinute, timeWindowSeconds);
    }

    @Override
    public void destroy() {
        requestCountsPerIpAddress.clear();
        logger.info("RateLimitingFilter destroyed");
    }
}