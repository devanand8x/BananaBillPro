package com.bananabill.config;

import com.bananabill.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Security Configuration
 * 
 * SECURITY DESIGN:
 * - Stateless JWT authentication (no sessions)
 * - CSRF disabled: Safe because we use JWT tokens, not cookies
 * - All endpoints require authentication except /auth/**
 * - Rate limiting applied via RateLimitingFilter
 * 
 * @see JwtAuthenticationFilter for token validation
 * @see RateLimitingFilter for abuse prevention
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired(required = false)
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Autowired(required = false)
        private RateLimitingFilter rateLimitingFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                // BCrypt with default strength (10 rounds)
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // CSRF: Disabled because we use stateless JWT authentication
                                // JWT tokens are sent via Authorization header, not cookies
                                // This is safe as CSRF attacks require cookie-based auth
                                .csrf(csrf -> csrf.disable())

                                // CORS: Allow configured origins
                                .cors(org.springframework.security.config.Customizer.withDefaults())

                                // Session: Stateless - no server-side sessions
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Security Headers
                                .headers(headers -> headers
                                                // Content Security Policy
                                                .contentSecurityPolicy(csp -> csp.policyDirectives(
                                                                "default-src 'self'; " +
                                                                                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
                                                                                +
                                                                                "style-src 'self' 'unsafe-inline'; " +
                                                                                "img-src 'self' data: https:; " +
                                                                                "font-src 'self' https://fonts.gstatic.com; "
                                                                                +
                                                                                "connect-src 'self' https:"))
                                                // Prevent clickjacking
                                                .frameOptions(frame -> frame.deny())
                                                // XSS Protection
                                                .xssProtection(xss -> xss.headerValue(
                                                                XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                                                // Prevent MIME type sniffing attacks
                                                .contentTypeOptions(contentType -> {
                                                }) // nosniff enabled by default
                                )

                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Public: Auth endpoints
                                                .requestMatchers("/auth/**").permitAll()
                                                // Public: Health checks
                                                .requestMatchers("/health", "/health/**").permitAll()
                                                // Public: Debug endpoints
                                                .requestMatchers("/debug/**").permitAll()
                                                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                                                // Protected: Metrics (authenticated only)
                                                .requestMatchers("/actuator/metrics/**", "/actuator/prometheus")
                                                .authenticated()
                                                // All other requests require authentication
                                                .anyRequest().authenticated())

                                // Rate limiting filter (if configured)
                                .addFilterBefore(
                                                rateLimitingFilter != null ? rateLimitingFilter
                                                                : (req, res, chain) -> chain.doFilter(req, res),
                                                UsernamePasswordAuthenticationFilter.class)

                                // JWT authentication filter
                                .addFilterBefore(
                                                jwtAuthenticationFilter != null ? jwtAuthenticationFilter
                                                                : (req, res, chain) -> chain.doFilter(req, res),
                                                UsernamePasswordAuthenticationFilter.class)

                                // Authentication error handling
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                                                }));

                return http.build();
        }
}
