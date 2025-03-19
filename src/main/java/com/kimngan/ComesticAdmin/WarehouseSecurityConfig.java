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
@Profile("warehouse") 
public class WarehouseSecurityConfig {

    @Bean
    @Order(4)
    public SecurityFilterChain warehouseSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/warehouse/import/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/warehouse/import/login").permitAll()
                        .requestMatchers("/warehouse/import/**").hasAuthority("NHAP_KHO")
                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/warehouse/import/login")
                        .loginProcessingUrl("/warehouse/import/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/warehouse/import/purchaseorder", true)
                        .failureUrl("/warehouse/import/login?error=true")
                        .permitAll())
                .logout((logout) -> logout
                        .logoutUrl("/warehouse/import/logout")
                        .logoutSuccessUrl("/warehouse/import/login?logout")
                        .permitAll());

        return http.build();
    }
}


