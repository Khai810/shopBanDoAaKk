package com.projectshopbando.shopbandoapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_KEY = "rate_limit::";
    private static final int RATE_LIMIT_PERIOD = 60;
    private static final int RATE_LIMIT_MAX_REQUESTS = 2000;

    public boolean isAllowed(String ipAddress) {
        String key = RATE_LIMIT_KEY + ipAddress;

        Long count = redisTemplate.opsForValue().increment(key);
        if(count == (1)){
            redisTemplate.expire(key, RATE_LIMIT_PERIOD, TimeUnit.SECONDS);
        }
        return count <= RATE_LIMIT_MAX_REQUESTS;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        if(!isAllowed(ipAddress)){
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
