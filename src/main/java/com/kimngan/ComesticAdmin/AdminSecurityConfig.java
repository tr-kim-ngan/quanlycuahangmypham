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
@Profile("admin") // Áp dụng cho profile "admin"
public class AdminSecurityConfig {

    @Bean
    @Order(1)
     SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/login") // Áp dụng cho các URL của admin
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                    .requestMatchers("/", "/index", "/product/**", "/category/**").permitAll() // Cho phép tự do cho URL công khai
                    .requestMatchers("/admin/**").hasAuthority("ADMIN") // Yêu cầu quyền "ADMIN" cho các URL admin
                    .anyRequest().authenticated())
                .formLogin((form) -> form
                    .loginPage("/login") // Trang đăng nhập cho admin
                    .loginProcessingUrl("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/admin/thongke/donhang", true) // Điều hướng đến trang admin sau khi đăng nhập thành công
                    .failureUrl("/login?error=true")
                    .permitAll())
                .logout((logout) -> logout
                    .logoutUrl("/admin/logout") // URL xử lý logout dành cho admin
                    .logoutSuccessUrl("/login?logout") // Điều hướng về trang login sau khi logout
                    .permitAll())
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation().none());

        return http.build();
    }
}
