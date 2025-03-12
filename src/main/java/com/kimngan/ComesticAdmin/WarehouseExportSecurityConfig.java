package com.kimngan.ComesticAdmin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("export") 
public class WarehouseExportSecurityConfig {

    @Bean
    @Order(5)
    public SecurityFilterChain warehouseExportSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/warehouse/export/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/warehouse/export/login").permitAll()
                        .requestMatchers("/warehouse/export/**").hasAuthority("XUAT_KHO")
                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/warehouse/export/login")
                        .loginProcessingUrl("/warehouse/export/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/warehouse/export/orders", true)
                        .failureUrl("/warehouse/export/login?error=true")
                        .permitAll())
                .logout((logout) -> logout
                        .logoutUrl("/warehouse/export/logout")
                        .logoutSuccessUrl("/warehouse/export/login?logout")
                        .permitAll());

        return http.build();
    }
}
