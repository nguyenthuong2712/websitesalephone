package org.example.websitesalephone.config;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers(
                                "/api/user/create",
                                "/api/user/update",
                                "/api/user/delete/**",
                                "/api/user/search",
                                "/api/order/search",
                                "/api/order/update",
                                "/api/order/count-order-staff",
                                "/api/order/dashboard/**",
                                "/api/dynamic/**",
                                "/api/product/create-product",
                                "/api/product/create-product-detail",
                                "/api/product/update",
                                "/api/product/deleted/**",
                                "/api/product/deleted-product-detail/**",
                                "/api/product/deleted-image/**",
                                "/api/product/create-image",
                                "/api/product/update-image")
                        .hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/product-images/upload").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/product-images/update").hasAnyAuthority("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/api/product-images/**").hasAnyAuthority("ADMIN", "STAFF")
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5175",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174",
                "http://127.0.0.1:5175"
        ));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
