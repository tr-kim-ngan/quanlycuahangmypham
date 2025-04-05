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
@Profile("seller") 
public class SellerSecurityConfig {

    @Bean
    @Order(5)
    public SecurityFilterChain sellerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/seller/**") // áp dụng bảo mật cho các URL bắt đầu bằng /seller
            .csrf(csrf -> csrf.disable())
           
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) 
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/seller/login", "/seller/logout").permitAll() // cho phép không cần đăng nhập
                .requestMatchers("/seller/**").hasAuthority("NHAN_VIEN_BAN_HANG") // yêu cầu quyền đúng
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/seller/login")
                .loginProcessingUrl("/seller/login") // nơi form submit tới
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/seller/orders", true)
                .failureUrl("/seller/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/seller/logout")
                .logoutSuccessUrl("/seller/login?logout")
                .permitAll());

        return http.build();
    }
}

