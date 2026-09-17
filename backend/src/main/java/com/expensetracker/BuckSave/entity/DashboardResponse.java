package com.expensetracker.BuckSave.entity;

import com.expensetracker.BuckSave.dto.ExpenseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Double totalSpending;
    private Double monthlySpending;
    private Double todaySpending;
    private Double remainingBudget;
    private Map<String, Double> categorySpending;
    private String highestSpendingCategory;
    private List<ExpenseResponse> recentExpenses;


}
