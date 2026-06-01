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

    @GetMapping("/admin/events")
    public String adminEvents() {

        return "admin-events";
    }

    @GetMapping("/admin/events/edit")
    public String editEvent() {

        return "edit-event";
    }

    @GetMapping("/events/detail")
    public String eventDetail() {

        return "event-detail";
    }

    @GetMapping("/organizer/events")
    public String organizerEvents() { return "organizer-events"; }

    @GetMapping("/organizer/events/create")
    public String organizerCreateEvent() { return "create-event"; }

    @GetMapping("/organizer/events/edit")
    public String organizerEditEvent() { return "edit-event"; }

    @GetMapping("/admin/organizers")
    public String adminOrganizers() { return "admin-organizers"; }

    @GetMapping("/admin/organizers/register")
    public String adminRegisterOrganizer() { return "admin-register-organizer"; }

    @GetMapping("/admin/inscriptions")
    public String adminInscriptions() { return "admin-inscriptions"; }

    @GetMapping("/admin/register-attendance")
    public String adminRegisterAttendance() { return "admin-register-attendance"; }

    @GetMapping("/admin/spots")
    public String adminSpots() { return "admin-spots"; }

    @GetMapping("/organizer/inscriptions")
    public String organizerInscriptions() { return "organizer-inscriptions"; }

    @GetMapping("/organizer/register-attendance")
    public String organizerRegisterAttendance() { return "organizer-register-attendance"; }

    @GetMapping("/organizer/spots")
    public String organizerSpots() { return "organizer-spots"; }

    @GetMapping("/admin/attendance")
    public String adminAttendance() { return "admin-attendance"; }

    @GetMapping("/organizer/attendance")
    public String organizerAttendance() { return "organizer-attendance"; }

    @GetMapping("/profile")
    public String profile() { return "profile"; }

    @GetMapping("/admin/reports")
    public String adminReports() { return "admin-reports"; }

    @GetMapping("/student/my-report")
    public String studentMyReport() { return "student-report"; }
}