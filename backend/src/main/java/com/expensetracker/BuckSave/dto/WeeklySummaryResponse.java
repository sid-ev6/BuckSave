package com.expensetracker.BuckSave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummaryResponse {

    private String weekStart;
    private String weekEnd;
    private Double totalSpending;
    private Double budget;
    private Double remaining;
}