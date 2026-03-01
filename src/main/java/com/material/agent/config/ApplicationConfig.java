package com.material.agent.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 应用配置
 * 启用异步、安全配置
 */
@Configuration
@EnableAsync
public class ApplicationConfig {

    /**
     * 安全配置
     * 生产环境应配置更严格的规则
     */
    @Configuration
    @EnableWebSecurity
    public static class SecurityConfig {
        
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    // Actuator 端点需要认证
                    .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
                        .permitAll()
                    .requestMatchers("/api/**").permitAll() // TODO: 生产环境改为需要认证
                    .anyRequest().authenticated()
                );
            
            return http.build();
        }
    }
}
