package com.capston.matching_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IdealMatchListResponse {
    private List<IdealMatchDTO> items;
}
