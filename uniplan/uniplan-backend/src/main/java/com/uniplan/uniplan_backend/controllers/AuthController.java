package com.uniplan.uniplan_backend.controllers;

import com.uniplan.uniplan_backend.dto.AuthResponse;

import com.uniplan.uniplan_backend.dto.LoginRequest;

import com.uniplan.uniplan_backend.dto.RegisterRequest;

import com.uniplan.uniplan_backend.services.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(

            @RequestBody RegisterRequest request
    ) {

        return ResponseEntity.ok(

                authService.register(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(

            @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(

                authService.login(request)
        );
    }
}