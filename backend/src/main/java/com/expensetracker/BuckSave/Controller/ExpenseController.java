package com.expensetracker.BuckSave.Controller;

import com.expensetracker.BuckSave.dto.*;
import com.expensetracker.BuckSave.service.ExpenseService;
import com.expensetracker.BuckSave.service.SpendingLimitService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final SpendingLimitService spendingLimitService;

    public ExpenseController(ExpenseService expenseService, SpendingLimitService spendingLimitService) {
        this.expenseService = expenseService;
        this.spendingLimitService = spendingLimitService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request) {

        return ResponseEntity.ok(
                expenseService.createExpense(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {

        return ResponseEntity.ok(
                expenseService.getAllExpenses()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                expenseService.getExpenseById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @RequestBody ExpenseRequest request) {

        return ResponseEntity.ok(
                expenseService.updateExpense(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id) {

        expenseService.deleteExpense(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponse> getCurrentMonthSummary() {

        return ResponseEntity.ok(
                expenseService.getCurrentMonthSummary()
        );
    }

    @GetMapping("/category-summary")
    public ResponseEntity<Map<String, Double>> getCurrentMonthCategorySpending() {

        return ResponseEntity.ok(
                expenseService.getCurrentMonthCategorySpending()
        );
    }

    @GetMapping("/highest-category")
    public ResponseEntity<String> getHighestSpendingCategory() {

        return ResponseEntity.ok(
                expenseService.getHighestSpendingCategory()
        );
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ExpenseResponse>> getRecentExpenses() {

        return ResponseEntity.ok(
                expenseService.getRecentExpenses()
        );
    }


    @GetMapping("/monthly-history")
    public ResponseEntity<Map<String, Double>> getMonthlySpendingHistory() {

        return ResponseEntity.ok(
                expenseService.getMonthlySpendingHistory()
        );
    }
    @GetMapping("/month")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByMonth(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                expenseService.getExpensesByMonth(month, year)
        );
    }

    @GetMapping("/range")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                expenseService.getExpensesByDateRange(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<ExpenseResponse>> searchExpenses(
            @RequestParam String description) {

        return ResponseEntity.ok(
                expenseService.searchExpensesByDescription(description)
        );
    }


    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                expenseService.getDashboard()
        );
    }

    @GetMapping("/spending-limit/{id}")
    public SpendingAnalysisResponse getExpensesForSpendingLimit(
            @PathVariable Long id) {

        return spendingLimitService.getSpendingAnalysis(id);
    }



}
