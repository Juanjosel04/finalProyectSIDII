package com.uniplan.uniplan_backend.dto;

import lombok.AllArgsConstructor;

import lombok.Getter;

import lombok.Setter;



@Getter
@Setter
@AllArgsConstructor
public class EventResponse {

    private String id;

    private String title;

    private String status;
}