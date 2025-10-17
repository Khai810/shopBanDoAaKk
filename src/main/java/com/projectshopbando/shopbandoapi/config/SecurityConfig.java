package com.projectshopbando.shopbandoapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Public endpoints - no authentication required
    private final String[] PUBLIC_URLS = {
            "/api/auth/login",
            "/api/categories",
            "/api/categories/{id}",
            "/api/products/landing",
            "/api/products/search",
            "/api/products/{id}",
            "/api/customers",
            "/api/orders",
            "/api/orders/{id}/status",
            "/api/payment/vnpay-ipn"
    };

    // Authenticated endpoints - require valid JWT
    private final String[] AUTHENTICATED_URLS = {
            "/api/accounts/**",
            "/api/customers/{id}",
            "/api/orders/customers",
            "/api/staffs/{id}"
    };

    // Admin/Staff only endpoints
    private final String[] ADMIN_STAFF_URLS = {
            "/api/categories/admin/**",
            "/api/products/admin/**",
            "/api/customers/admin/**",
            "/api/orders/admin/**",
            "/api/staffs/**",
            "/api/upload/**"
    };


    @Autowired
    private CustomJwtDecoder jwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RateLimiterFilter rateLimiterFilter) throws Exception {
        http.authorizeHttpRequests(request -> {
            request

                    // Public endpoints
                    .requestMatchers(HttpMethod.GET, "/categories", "/categories/{id}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/products/landing", "/products/search", "/products/{id}").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/customers").permitAll()
                    .requestMatchers(HttpMethod.POST, "/orders").permitAll()
                    .requestMatchers(HttpMethod.GET, "/orders/{id}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/orders/{id}/status").permitAll()
                    .requestMatchers(HttpMethod.GET, "/payment/vnpay-ipn").permitAll()
                    .anyRequest().authenticated();
        });

        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        http.addFilterBefore(rateLimiterFilter, UsernamePasswordAuthenticationFilter.class);
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults());
        return http.build();
    }


    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }


}
