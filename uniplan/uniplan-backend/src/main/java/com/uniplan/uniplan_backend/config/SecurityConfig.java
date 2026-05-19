package com.uniplan.uniplan_backend.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;



    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                /*
                 |--------------------------------------------------------------------------
                 | CSRF
                 |--------------------------------------------------------------------------
                 */

                .csrf(csrf -> csrf.disable())



                /*
                 |--------------------------------------------------------------------------
                 | EXCEPTION HANDLER
                 |--------------------------------------------------------------------------
                 */

                .exceptionHandling(exception ->

                        exception.authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
                )



                /*
                 |--------------------------------------------------------------------------
                 | SESSION
                 |--------------------------------------------------------------------------
                 */

                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )



                /*
                 |--------------------------------------------------------------------------
                 | AUTHORIZATION
                 |--------------------------------------------------------------------------
                 */

                .authorizeHttpRequests(auth -> auth



                        /*
                         |--------------------------------------------------------------------------
                         | PUBLIC RESOURCES
                         |--------------------------------------------------------------------------
                         */

                        .requestMatchers(

                                "/",

                                "/login",

                                "/register",

                                "/auth/**",

                                "/css/**",

                                "/js/**",

                                "/images/**"

                        ).permitAll()



                        /*
                         |--------------------------------------------------------------------------
                         | HTML VIEWS
                         |--------------------------------------------------------------------------
                         |
                         | Las vistas HTML se protegen
                         | desde frontend usando JWT
                         | almacenado en sessionStorage.
                         |
                         */

                        .requestMatchers(

                                "/admin/home",

                                "/student/home",

                                "/organizer/home",

                                "/admin/events/create"

                        ).permitAll()



                        /*
                         |--------------------------------------------------------------------------
                         | ADMIN API
                         |--------------------------------------------------------------------------
                         */

                        .requestMatchers(

                                "/api/admin/**"

                        ).hasRole("ADMIN")



                        /*
                         |--------------------------------------------------------------------------
                         | STUDENT API
                         |--------------------------------------------------------------------------
                         */

                        .requestMatchers(

                                "/api/student/**",

                                "/registrations/**"

                        ).hasRole("STUDENT")



                        /*
                         |--------------------------------------------------------------------------
                         | ORGANIZER API
                         |--------------------------------------------------------------------------
                         */

                        .requestMatchers(

                                "/api/organizer/**",

                                "/events/update/**"

                        ).hasAnyRole(

                                "ADMIN",

                                "ORGANIZER"
                        )



                        /*
                         |--------------------------------------------------------------------------
                         | CREATE EVENT API
                         |--------------------------------------------------------------------------
                         */

                        .requestMatchers(

                                HttpMethod.POST,

                                "/events"

                        ).hasAnyRole(

                                "ADMIN",

                                "ORGANIZER"
                        )



                        /*
                         |--------------------------------------------------------------------------
                         | ANY OTHER REQUEST
                         |--------------------------------------------------------------------------
                         */

                        .anyRequest()

                        .authenticated()
                )



                /*
                 |--------------------------------------------------------------------------
                 | JWT FILTER
                 |--------------------------------------------------------------------------
                 */

                .addFilterBefore(

                        jwtAuthenticationFilter,

                        UsernamePasswordAuthenticationFilter.class
                );



        return http.build();
    }



    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }



    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}