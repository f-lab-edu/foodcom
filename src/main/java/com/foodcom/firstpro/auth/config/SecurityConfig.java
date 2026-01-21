package com.foodcom.firstpro.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcom.firstpro.auth.filter.JwtAuthenticationFilter;
import com.foodcom.firstpro.auth.util.JwtTokenProvider;
import com.foodcom.firstpro.controller.advice.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtTokenProvider jwtTokenProvider;
        private final ObjectMapper objectMapper;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> {
                                }) // Enable CORS (uses Spring MVC config by default)
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                                        response.setContentType("application/json;charset=UTF-8");

                                                        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(
                                                                        "인증 실패",
                                                                        "Access Token이 유효하지 않거나 필요합니다.");

                                                        String jsonResponse = objectMapper
                                                                        .writeValueAsString(errorResponse);
                                                        response.getWriter().write(jsonResponse);
                                                })

                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(HttpStatus.FORBIDDEN.value());
                                                        response.setContentType("application/json;charset=UTF-8");

                                                        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(
                                                                        "권한 없음",
                                                                        "자원에 접근할 권한이 없습니다.");

                                                        String jsonResponse = objectMapper
                                                                        .writeValueAsString(errorResponse);
                                                        response.getWriter().write(jsonResponse);
                                                }))

                                // 인증 설정
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.POST, "/members").permitAll()
                                                .requestMatchers("/actuator/**").permitAll() // Cloud Run Health Check
                                                .requestMatchers(HttpMethod.GET, "/posts", "/posts/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/posts", "/posts/**").permitAll()
                                                .requestMatchers(
                                                                "/login",
                                                                "/auth/reissue",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs",
                                                                "/v3/api-docs/**",
                                                                "/webjars/**",
                                                                "/h2-console/**")
                                                .permitAll()
                                                .anyRequest().authenticated())

                                .addFilterBefore(
                                                new JwtAuthenticationFilter(jwtTokenProvider),
                                                UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}