package com.exituity.documentparser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for APIs
            .csrf(csrf -> csrf.disable())
            // Disable default login form and basic auth
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            // Authorize requests
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/parse/**").permitAll()  // allow your parser endpoint
                .anyRequest().permitAll()  // allow all others temporarily
            );

        // Return the built filter chain
        return http.build();
    }
}
