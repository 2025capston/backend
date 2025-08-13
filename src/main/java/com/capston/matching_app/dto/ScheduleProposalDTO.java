package com.capston.matching_app.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ScheduleProposalDTO {
    private List<LocalDate> dates;
    private List<String> places;
    private List<LocalTime> times;
}
