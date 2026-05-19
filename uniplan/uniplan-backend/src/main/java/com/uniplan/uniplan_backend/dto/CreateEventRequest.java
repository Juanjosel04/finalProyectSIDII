package com.uniplan.uniplan_backend.dto;

import lombok.Getter;

import lombok.Setter;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Map;



@Getter
@Setter
public class CreateEventRequest {

    private String title;

    private String description;

    private String type;

    private String location;

    private Integer capacity;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private List<String> tags;

    private Map<String, Object> metadata;
}