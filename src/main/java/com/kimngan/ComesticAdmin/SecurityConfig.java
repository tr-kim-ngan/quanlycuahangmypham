//package com.kimngan.ComesticAdmin;
//
//import com.kimngan.ComesticAdmin.services.NguoiDungDetailsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.session.SessionRegistry;
//import org.springframework.security.core.session.SessionRegistryImpl;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private NguoiDungDetailsService nguoiDungDetailsService;
//
//    // Cấu hình bảo mật cho admin
//    @Bean
//    @Order(1)
//    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
//        http.securityMatcher("/admin/**", "/login")
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests((requests) -> requests
//                    .requestMatchers("/", "/index", "/product/**", "/category/**").permitAll()
//                    .requestMatchers("/admin/**").hasAuthority("ADMIN")
//                    .anyRequest().authenticated())
//                .formLogin((form) -> form
//                    .loginPage("/login")
//                    .loginProcessingUrl("/login")
//                    .usernameParameter("username")
//                    .passwordParameter("password")
//                    .defaultSuccessUrl("/admin", true)
//                    .failureUrl("/login?error=true")
//                    .permitAll())
//                .logout((logout) -> logout
//                    .logoutUrl("/admin/logout")
//                    .logoutSuccessUrl("/login?logout")
//                    .permitAll())
//                .sessionManagement(session -> session
//                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                    .sessionFixation().none());
//
//        return http.build();
//    }
//
//    @Bean
//    @Order(2)
//    public SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {
//        http.securityMatcher("/**")
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests((requests) -> requests
//                .requestMatchers("/", "/index", "/customer/register", "/customer/login", "/product/**", "/category/**", "/search/**").permitAll()
//                .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
//                .anyRequest().authenticated())
//            .formLogin((form) -> form
//                .loginPage("/customer/login")
//                .loginProcessingUrl("/customer/login")
//                .usernameParameter("username")
//                .passwordParameter("password")
//                .defaultSuccessUrl("/", true)
//                .failureUrl("/customer/login?error=true")
//                .permitAll())
//            .logout((logout) -> logout
//                .logoutUrl("/customer/logout")
//                .logoutSuccessUrl("/")
//                .permitAll())
//            .sessionManagement(session -> session
//                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                .sessionFixation().none());
//
//        return http.build();
//    }
//
//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring().requestMatchers("/fe/**", "/static/**", "/assets/**", "/upload/**");
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
//
//
//
