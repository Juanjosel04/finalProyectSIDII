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
                                "/admin/events",
                                "/admin/events/create",
                                "/admin/events/edit",
                                "/admin/organizers",
                                "/admin/organizers/register",
                                "/admin/inscriptions",
                                "/admin/register-attendance",
                                "/admin/spots",
                                "/student/home",
                                "/organizer/home",
                                "/organizer/events",
                                "/organizer/events/create",
                                "/organizer/events/edit",
                                "/organizer/inscriptions",
                                "/organizer/register-attendance",
                                "/organizer/spots",
                                "/events/detail"

                        ).permitAll()



                        /*
                         |--------------------------------------------------------------------------
                         | ADMIN API
                         |--------------------------------------------------------------------------
                         */

                        .requestMatchers(

                                "/api/admin/**",
                                "/admin/users/**"

                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/registrations"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/registrations/admin"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/registrations/organizer"
                        ).hasAnyRole("ORGANIZER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/registrations/organizer"
                        ).hasAnyRole("ORGANIZER")



                        /*
                         |--------------------------------------------------------------------------
                         | STUDENT API
                         |--------------------------------------------------------------------------
                         */

                        .requestMatchers(
                                "/api/student/**"
                        ).hasRole("STUDENT")

                        /*
                         | POST /registrations  — inscribirse
                         | DELETE /registrations/{id}  — cancelar
                         | GET /registrations/my  — mis inscripciones
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/registrations"
                        ).hasRole("STUDENT")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/registrations/**"
                        ).hasRole("STUDENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/registrations/my"
                        ).hasRole("STUDENT")

                        /*
                         | GET /registrations/event/{eventId}  — inscritos de un evento
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/registrations/event/**"
                        ).hasAnyRole("ADMIN", "ORGANIZER")



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