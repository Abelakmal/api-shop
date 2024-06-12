package com.abel.ecommerce.security;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.abel.ecommerce.security.jjwt.AuthEntryPointJwt;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import com.abel.ecommerce.security.jjwt.AuthTokenFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    @Autowired
    private AuthEntryPointJwt unAuthEntryPointJwt;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()).csrf(csrf -> csrf
                .disable())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(unAuthEntryPointJwt))
                .sessionManagement(managament -> managament
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests().requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/**").permitAll().anyRequest().authenticated();
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authentication) throws Exception {
        return authentication.getAuthenticationManager();
    }

    // @Bean
    // public FilterRegistrationBean<CorsFilter> corsFilter() {
    // UrlBasedCorsConfigurationSource source = new
    // UrlBasedCorsConfigurationSource();
    // CorsConfiguration config = new CorsConfiguration();
    // config.setAllowedOrigins(Collections.singletonList("htpps://localhost:3000"));
    // config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    // config.setAllowedHeaders(Collections.singletonList("*"));

    // source.registerCorsConfiguration("/**", config);
    // CorsFilter corsFilter = new CorsFilter(source);

    // FilterRegistrationBean<CorsFilter> filterRegistrationBean = new
    // FilterRegistrationBean<>(corsFilter);
    // filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

    // return filterRegistrationBean;
    // }

    @Bean
    CorsConfigurationSource configurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
