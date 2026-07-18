package com.fulfillflow.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${fulfillflow.jwt.secret}") String secret) {
        return NimbusJwtEncoder.withSecretKey(key(secret)).build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${fulfillflow.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(key(secret)).build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("authorities");
        authorities.setAuthorityPrefix("");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    private SecretKeySpec key(String secret) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Configuration
    @ConditionalOnProperty(name = "fulfillflow.security.enabled", havingValue = "true", matchIfMissing = true)
    static class ProtectedApi {
        @Bean
        SecurityFilterChain protectedFilterChain(
                HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/login", "/actuator/health", "/error",
                                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/inventory/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PATCH, "/api/inventory/**").hasRole("ADMIN")
                            .requestMatchers("/api/users/**").hasRole("ADMIN")
                            .requestMatchers("/api/**").hasAnyRole("ADMIN", "WORKER")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                            jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                    .build();
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "fulfillflow.security.enabled", havingValue = "false")
    static class OpenApiForTests {
        @Bean
        SecurityFilterChain openFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }
}
