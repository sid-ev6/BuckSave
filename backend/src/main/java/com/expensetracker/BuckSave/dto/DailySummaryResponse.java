package com.expensetracker.BuckSave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryResponse {

    private String date;
    private Double totalSpending;
    private Double budget;
    private Double remaining;
}