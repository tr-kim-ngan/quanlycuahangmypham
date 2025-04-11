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
@Profile("customer") // Áp dụng cho profile "customer"
public class CustomerSecurityConfig {

    @Bean
    @Order(2)
     SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**") // Áp dụng cho toàn bộ URL của khách hàng
            .csrf(csrf -> csrf.disable())
            
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) 
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/index", 
                		"/brands/**",
                		"/customer/register", 
                		"/customer/login", 
                		"/product/**", 
                		"/category/**", 
                		"/search/**",
                		"/api/payment/**",
                		"/shipping-fee",
                		"/api/chatbot/**"
                		
                		
                		
                		).permitAll() // Cho phép truy cập tự do cho đăng ký, đăng nhập, chi tiết sản phẩm, danh mục và tìm kiếm
                .requestMatchers("/customer/**").hasAuthority("CUSTOMER") // Yêu cầu quyền "CUSTOMER" cho các URL khách hàng
                .anyRequest().authenticated())
            .formLogin((form) -> form
                .loginPage("/customer/login") // Trang đăng nhập cho khách hàng
                .loginProcessingUrl("/customer/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/customer/login?error=true")
                .permitAll())
            .logout((logout) -> logout
                .logoutUrl("/customer/logout") // URL xử lý logout dành cho khách hàng
                .logoutSuccessUrl("/")
                .permitAll())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().none());

        return http.build();
    }
    
    
    
    
}
