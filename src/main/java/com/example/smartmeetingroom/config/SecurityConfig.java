package com.example.smartmeetingroom.config;

import com.example.smartmeetingroom.filter.JwtFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(c ->
                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(c -> c

                        // Super admin only
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/admin",
                                "/api/roles"
                        ).hasRole("SUPER_ADMIN").requestMatchers(
                                HttpMethod.GET,
                                "/api/roles"
                        ).hasRole("SUPER_ADMIN").requestMatchers(
                                HttpMethod.DELETE,
                                "/api/roles/*"
                        ).hasRole("SUPER_ADMIN")

                        // Technician
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/techniciain/*"
                        ).hasRole("TECHNICIAN")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/techniciain/**"
                        ).hasRole("TECHNICIAN")

                        // Everyone
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/*",
                                "/api/meeting-rooms/details",
                                "/api/notifications/*"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/bookings",
                                "/api/users/me/email-change",
                                "/api/notifications/*"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/users",
                                "/api/bookings/*"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/me"
                        ).authenticated()

                        // Super Admin and Admin only
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/meeting-rooms",
                                "/api/users",
                                "/api/assets",
                                "/api/asset-types",
                                "/api/asset-service/**"
                                ).hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users",
                                "/api/assets/**",
                                "/api/meeting-rooms",
                                "/api/asset-service/**"
                                ).hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/*",
                                "/api/asset-types/*",
                                "/api/meeting-rooms/*",
                                "/api/assets/*"
                        ).hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/asset-types/*",
                                "/api/assets/*",
                                "/api/users/*/role",
                                "/api/meeting-rooms/*"
                        ).hasAnyRole("SUPER_ADMIN", "ADMIN")


                        .anyRequest().permitAll())

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling( c ->{
                    c.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                    c.accessDeniedHandler((request, response, accessDeniedException) ->
                            response.setStatus(HttpStatus.FORBIDDEN.value()));
                });

        return http.build();
    }
}
