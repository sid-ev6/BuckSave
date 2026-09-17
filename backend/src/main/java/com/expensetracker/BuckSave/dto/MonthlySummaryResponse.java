package com.expensetracker.BuckSave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryResponse {

    private String month;
    private int year;
    private Double totalSpending;
    private Double budget;
    private Double remaining;



}