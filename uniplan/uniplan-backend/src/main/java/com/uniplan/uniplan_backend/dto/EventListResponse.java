package com.uniplan.uniplan_backend.dto;

import lombok.AllArgsConstructor;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;

import java.time.LocalDateTime;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventListResponse {

    private String id;
    private String title;

    private String type;

    private String location;
    private Integer capacity;
    private LocalDateTime startDate;
    private String status;
}