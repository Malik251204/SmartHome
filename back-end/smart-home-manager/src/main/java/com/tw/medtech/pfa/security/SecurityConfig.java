package com.tw.medtech.pfa.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()

                // Rooms: anyone signed in can read; only admins create/
                // rename/delete or manage who's assigned to a room.
                .requestMatchers(HttpMethod.GET, "/api/rooms/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/rooms/*/users/*").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/rooms/*/users/*").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/rooms").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/rooms/*").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/rooms/*").hasAuthority("ADMIN")

                // Devices: any signed-in user can toggle one — matches the
                // frontend, where this was never admin-gated (residents
                // control their own room's devices). This rule must stay
                // ahead of the broader PUT /api/devices/* rule below so it
                // keeps matching first.
                .requestMatchers(HttpMethod.PUT, "/api/devices/*/status").authenticated()

                // Devices: full management (add/edit/remove a room's
                // hardware) is admin-only, same tier as Rooms/Users.
                // Reading the list is open to anyone signed in.
                .requestMatchers(HttpMethod.GET, "/api/devices/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/devices").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/devices/*").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/devices/*").hasAuthority("ADMIN")

                // Sensors: read-only for everyone signed in.
                .requestMatchers(HttpMethod.GET, "/api/sensors/**").authenticated()

                // Users: admin-only, every method.
                .requestMatchers("/api/users/**").hasAuthority("ADMIN")

                // Preferences: any signed-in user can read/write through
                // this layer — ownership (only the author can edit; author
                // or admin can delete) is enforced in
                // PreferenceServiceImpl, not here, since it depends on
                // *which* preference, not just the URL.
                .requestMatchers("/api/preferences/**").authenticated()

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
