package com.uniplan.uniplan_backend.services;

import com.uniplan.uniplan_backend.config.JwtService;

import com.uniplan.uniplan_backend.dto.AuthResponse;
import com.uniplan.uniplan_backend.dto.LoginRequest;
import com.uniplan.uniplan_backend.dto.RegisterRequest;

import com.uniplan.uniplan_backend.model.relational.uniplan.User;

import com.uniplan.uniplan_backend.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import java.util.List;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;



    public AuthResponse register(
            RegisterRequest request
    ) {

        if (
                userRepository.existsByEmail(
                        request.getEmail()
                )
        ) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }



        User user = User.builder()

                .id(UUID.randomUUID())

                .email(request.getEmail())

                .passwordHash(

                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .role(request.getRole())

                /*
                 * FK -> universidad.students.id
                 */

                .studentId(
                        request.getStudentId()
                )

                /*
                 * FK -> universidad.employees.id
                 */

                .employeeId(
                        request.getEmployeeId()
                )

                .status("ACTIVE")

                .build();



        userRepository.save(user);



        UserDetails userDetails =

                new org.springframework.security
                        .core.userdetails.User(

                        user.getEmail(),

                        user.getPasswordHash(),

                        List.of()
                );



        String token =
                jwtService.generateToken(
                        userDetails
                );



        return new AuthResponse(

                token,

                user.getRole(),

                user.getEmail()
        );
    }



    public AuthResponse login(
            LoginRequest request
    ) {

        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        request.getEmail(),

                        request.getPassword()
                )
        );



        User user =

                userRepository.findByEmail(
                        request.getEmail()
                ).orElseThrow(

                        () -> new RuntimeException(
                                "User not found"
                        )
                );



        UserDetails userDetails =

                new org.springframework.security
                        .core.userdetails.User(

                        user.getEmail(),

                        user.getPasswordHash(),

                        List.of()
                );



        String token =
                jwtService.generateToken(
                        userDetails
                );



        return new AuthResponse(

                token,

                user.getRole(),

                user.getEmail()
        );
    }
}