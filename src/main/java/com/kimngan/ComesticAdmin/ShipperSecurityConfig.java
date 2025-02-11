package com.kimngan.ComesticAdmin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("shipper") // Áp dụng cho profile "shipper"
public class ShipperSecurityConfig {

    @Bean
    @Order(3)
    public SecurityFilterChain shipperSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**") // Áp dụng bảo mật cho Shipper
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/login").permitAll() // Cho phép truy cập trang đăng nhập
                        .requestMatchers("/shipper/**").hasAuthority("SHIPPER") // Chỉ Shipper mới truy cập được
                        .anyRequest().authenticated()) // Các request khác đều phải đăng nhập
                .formLogin((form) -> form
                        .loginPage("/shipper/login") // Trang đăng nhập shipper
                        .loginProcessingUrl("/shipper/login")
                        .usernameParameter("username") // Dùng username thay vì email
                        .passwordParameter("password")
                        .defaultSuccessUrl("/shipper/index", true) // Điều hướng về trang chính sau khi đăng nhập
                        .failureUrl("/shipper/login?error=true")
                        .permitAll())
                .logout((logout) -> logout
                        .logoutUrl("/shipper/logout")
                        .logoutSuccessUrl("/shipper/login?logout")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().none());

        return http.build();
    }
}

