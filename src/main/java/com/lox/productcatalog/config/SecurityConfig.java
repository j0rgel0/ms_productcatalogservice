//// src/main/java/com/lox/authservice/security/SecurityConfig.java
//
//package com.lox.productcatalog.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.ReactiveAuthenticationManager;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
//import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
//import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//
//@Configuration
//@EnableWebFluxSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final ReactiveAuthenticationManager authenticationManager;
//    private final SecurityContextRepository securityContextRepository;
//
//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//        return http
//                .csrf(CsrfSpec::disable)
//                .httpBasic(HttpBasicSpec::disable)
//                .formLogin(FormLoginSpec::disable)
//                .authenticationManager(authenticationManager)
//                .securityContextRepository(securityContextRepository)
//                .cors(cors -> cors.configurationSource(request -> {
//                    CorsConfiguration config = new CorsConfiguration();
//                    config.addAllowedOrigin("http://localhost:8072");
//                    config.addAllowedMethod("*");
//                    config.addAllowedHeader("*");
//                    config.setAllowCredentials(true);
//                    return config;
//                }))
//                .authorizeExchange(exchange -> exchange
//                        .pathMatchers(HttpMethod.POST, "/lox-auth-service/actuator/refresh")
//                        .permitAll()
//                        .pathMatchers(HttpMethod.POST, "/actuator/refresh", "/actuator/refresh/")
//                        .permitAll()
//                        .pathMatchers("/favicon.ico")
//                        .permitAll()
//                        .pathMatchers("/actuator/**").permitAll()
//                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
//                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
//                        .pathMatchers("/api/v1/public/**").permitAll()
//                        .pathMatchers("/lox-auth-service/api/v1/auth/login").permitAll()
//                        .pathMatchers("/lox-auth-service/api/v1/auth/register").permitAll()
//                        .pathMatchers("/lox-auth-service/api/v1/public/**").permitAll()
//                        .anyExchange().authenticated()
//                )
//                .build();
//    }
//
//}
