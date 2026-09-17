package com.expensetracker.BuckSave.dto;

import com.expensetracker.BuckSave.dto.ExpenseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpendingAnalysisResponse {

    private Double totalSpent;
    private Double remainingAmount;
    private List<ExpenseResponse> expenses;
}