package com.uniplan.uniplan_backend.controllers;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String root() {

        return "login";
    }



    @GetMapping("/login")
    public String login() {

        return "login";
    }



    @GetMapping("/register")
    public String register() {

        return "register";
    }



    @GetMapping("/student/home")
    public String studentHome() {

        return "student-home";
    }



    @GetMapping("/admin/home")
    public String adminHome() {

        return "admin-home";
    }



    @GetMapping("/organizer/home")
    public String organizerHome() {

        return "organizer-home";
    }

    @GetMapping("/admin/events/create")
    public String createEvent() {

        return "create-event";
    }
}