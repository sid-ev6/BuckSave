package com.expensetracker.BuckSave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data

public class DashboardResponse {

    private Double totalSpending;
    private Double monthlySpending;
    private Double todaySpending;
    private Double remainingBudget;
    private Map<String, Double> categorySpending;
    private String highestSpendingCategory;
    private List<ExpenseResponse> recentExpenses;

    public DashboardResponse() {
    }

    public DashboardResponse(
            Double totalSpending,
            Double monthlySpending,
            Double todaySpending,
            Double remainingBudget,
            Map<String, Double> categorySpending,
            String highestSpendingCategory,
            List<ExpenseResponse> recentExpenses) {

        this.totalSpending = totalSpending;
        this.monthlySpending = monthlySpending;
        this.todaySpending = todaySpending;
        this.remainingBudget = remainingBudget;
        this.categorySpending = categorySpending;
        this.highestSpendingCategory = highestSpendingCategory;
        this.recentExpenses = recentExpenses;
    }
}