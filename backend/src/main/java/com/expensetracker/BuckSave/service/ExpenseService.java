package com.expensetracker.BuckSave.service;

import com.expensetracker.BuckSave.dto.DashboardResponse;
import com.expensetracker.BuckSave.dto.ExpenseRequest;
import com.expensetracker.BuckSave.dto.ExpenseResponse;
import com.expensetracker.BuckSave.dto.MonthlySummaryResponse;
import com.expensetracker.BuckSave.entity.Category;
import com.expensetracker.BuckSave.entity.Expense;
import com.expensetracker.BuckSave.entity.User;
import com.expensetracker.BuckSave.exception.AccessDeniedException;
import com.expensetracker.BuckSave.exception.ResourceNotFoundException;
import com.expensetracker.BuckSave.repository.CategoryRepository;
import com.expensetracker.BuckSave.repository.ExpenseRepository;
import com.expensetracker.BuckSave.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final SpendingLimitService spendingLimitService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            SpendingLimitService spendingLimitService) {

        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.spendingLimitService = spendingLimitService;
    }


    // ==========================================
    // GET LOGGED-IN USER
    // ==========================================

    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));
    }


    // ==========================================
    // CHECK CATEGORY ACCESS
    // ==========================================

    private void validateCategoryAccess(
            Category category,
            User user) {

        /*
         * user == null
         *     → shared default category
         *     → everyone can use it
         *
         * user != null
         *     → custom category
         *     → only its owner can use it
         */

        if (category.getUser() != null
                && !category.getUser()
                .getId()
                .equals(user.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to use this category"
            );
        }
    }


    // ==========================================
    // CREATE EXPENSE
    // ==========================================

    public ExpenseResponse createExpense(
            ExpenseRequest request) {

        User user = getLoggedInUser();


        Category category =
                categoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found"
                                ));


        // Shared defaults are allowed.
        // Another user's custom category is blocked.
        validateCategoryAccess(
                category,
                user
        );


        Expense expense =
                new Expense();

        expense.setAmount(
                request.getAmount()
        );

        expense.setDescription(
                request.getDescription()
        );

        expense.setDate(
                request.getDate()
        );

        expense.setCategory(
                category
        );

        expense.setUser(
                user
        );


        Expense savedExpense =
                expenseRepository.save(expense);


        return new ExpenseResponse(
                savedExpense.getId(),
                savedExpense.getAmount(),
                savedExpense.getDescription(),
                savedExpense.getDate(),
                savedExpense.getCategory().getId(),
                savedExpense.getCategory().getName()
        );
    }


    // ==========================================
    // GET ALL EXPENSES
    // ==========================================

    public List<ExpenseResponse> getAllExpenses() {

        User user = getLoggedInUser();


        List<Expense> expenses =
                expenseRepository.findByUser(user);


        return expenses.stream()
                .sorted(
                        Comparator.comparing(
                                        Expense::getDate
                                )
                                .reversed()
                                .thenComparing(
                                        Expense::getCreatedAt,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                )
                .map(expense ->
                        new ExpenseResponse(
                                expense.getId(),
                                expense.getAmount(),
                                expense.getDescription(),
                                expense.getDate(),
                                expense.getCategory() != null
                                        ? expense.getCategory().getId()
                                        : null,
                                expense.getCategory() != null
                                        ? expense.getCategory().getName()
                                        : null
                        )
                )
                .toList();
    }


    // ==========================================
    // GET EXPENSE BY ID
    // ==========================================

    public ExpenseResponse getExpenseById(
            Long id) {

        User user = getLoggedInUser();


        Expense expense =
                expenseRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Expense not found"
                                ));


        if (!expense.getUser()
                .getId()
                .equals(user.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this expense"
            );
        }


        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getDate(),
                expense.getCategory() != null
                        ? expense.getCategory().getId()
                        : null,
                expense.getCategory() != null
                        ? expense.getCategory().getName()
                        : null
        );
    }


    // ==========================================
    // UPDATE EXPENSE
    // ==========================================

    public ExpenseResponse updateExpense(
            Long id,
            ExpenseRequest request) {

        User user = getLoggedInUser();


        Expense expense =
                expenseRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Expense not found"
                                ));


        if (!expense.getUser()
                .getId()
                .equals(user.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to update this expense"
            );
        }


        Category category =
                categoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found"
                                ));


        // Important:
        // shared default category → allowed
        // another user's custom category → blocked
        validateCategoryAccess(
                category,
                user
        );


        expense.setAmount(
                request.getAmount()
        );

        expense.setDescription(
                request.getDescription()
        );

        expense.setDate(
                request.getDate()
        );

        expense.setCategory(
                category
        );


        Expense updatedExpense =
                expenseRepository.save(expense);


        return new ExpenseResponse(
                updatedExpense.getId(),
                updatedExpense.getAmount(),
                updatedExpense.getDescription(),
                updatedExpense.getDate(),
                updatedExpense.getCategory() != null
                        ? updatedExpense.getCategory().getId()
                        : null,
                updatedExpense.getCategory() != null
                        ? updatedExpense.getCategory().getName()
                        : null
        );
    }


    // ==========================================
    // DELETE EXPENSE
    // ==========================================

    public void deleteExpense(Long id) {

        User user = getLoggedInUser();


        Expense expense =
                expenseRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Expense not found"
                                ));


        if (!expense.getUser()
                .getId()
                .equals(user.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to delete this expense"
            );
        }


        expenseRepository.delete(expense);
    }


    // ==========================================
    // CURRENT MONTH SUMMARY
    // ==========================================

    public MonthlySummaryResponse getCurrentMonthSummary() {

        User user = getLoggedInUser();

        LocalDate today =
                LocalDate.now();

        LocalDate startDate =
                today.withDayOfMonth(1);

        LocalDate endDate =
                today.withDayOfMonth(
                        today.lengthOfMonth()
                );


        List<Expense> expenses =
                expenseRepository.findByUserAndDateBetween(
                        user,
                        startDate,
                        endDate
                );


        Double totalSpending =
                expenses.stream()
                        .map(Expense::getAmount)
                        .reduce(
                                0.0,
                                Double::sum
                        );


        String month =
                today.getMonth()
                        .getDisplayName(
                                java.time.format.TextStyle.FULL,
                                java.util.Locale.ENGLISH
                        );

        int year =
                today.getYear();


        Double budget = 0.0;

        Double remaining =
                budget - totalSpending;


        return new MonthlySummaryResponse(
                month,
                year,
                totalSpending,
                budget,
                remaining
        );
    }


    // ==========================================
    // CURRENT MONTH CATEGORY SPENDING
    // ==========================================

    public Map<String, Double>
    getCurrentMonthCategorySpending() {

        User user = getLoggedInUser();

        LocalDate today =
                LocalDate.now();

        LocalDate startDate =
                today.withDayOfMonth(1);

        LocalDate endDate =
                today.withDayOfMonth(
                        today.lengthOfMonth()
                );


        List<Expense> expenses =
                expenseRepository.findByUserAndDateBetween(
                        user,
                        startDate,
                        endDate
                );


        return expenses.stream()
                .collect(
                        Collectors.groupingBy(
                                expense ->
                                        expense.getCategory()
                                                .getName(),

                                Collectors.summingDouble(
                                        Expense::getAmount
                                )
                        )
                );
    }


    // ==========================================
    // HIGHEST SPENDING CATEGORY
    // ==========================================

    public String getHighestSpendingCategory() {

        Map<String, Double>
                categorySpending =
                getCurrentMonthCategorySpending();


        return categorySpending.entrySet()
                .stream()
                .max(
                        Map.Entry.comparingByValue()
                )
                .map(
                        Map.Entry::getKey
                )
                .orElse(
                        "No spending yet"
                );
    }


    // ==========================================
    // RECENT EXPENSES
    // ==========================================

    public List<ExpenseResponse>
    getRecentExpenses() {

        User user =
                getLoggedInUser();

        LocalDate today =
                LocalDate.now();

        LocalDate startDate =
                today.withDayOfMonth(1);

        LocalDate endDate =
                today.withDayOfMonth(
                        today.lengthOfMonth()
                );


        List<Expense> expenses =
                expenseRepository
                        .findByUserAndDateBetween(
                                user,
                                startDate,
                                endDate
                        );


        return expenses.stream()
                .sorted(
                        Comparator.comparing(
                                        Expense::getDate
                                )
                                .reversed()
                                .thenComparing(
                                        Expense::getCreatedAt,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                )
                .limit(5)
                .map(expense ->
                        new ExpenseResponse(
                                expense.getId(),
                                expense.getAmount(),
                                expense.getDescription(),
                                expense.getDate(),
                                expense.getCategory() != null
                                        ? expense.getCategory().getId()
                                        : null,
                                expense.getCategory() != null
                                        ? expense.getCategory().getName()
                                        : null
                        )
                )
                .toList();
    }


    // ==========================================
    // MONTHLY SPENDING HISTORY
    // ==========================================

    public Map<String, Double>
    getMonthlySpendingHistory() {

        User user =
                getLoggedInUser();


        List<Expense> expenses =
                expenseRepository.findByUser(user);


        return expenses.stream()
                .collect(
                        Collectors.groupingBy(
                                expense ->
                                        expense.getDate()
                                                .getYear()
                                                + "-"
                                                + String.format(
                                                "%02d",
                                                expense.getDate()
                                                        .getMonthValue()
                                        ),

                                Collectors.summingDouble(
                                        Expense::getAmount
                                )
                        )
                );
    }


    // ==========================================
    // GET EXPENSES BY MONTH
    // ==========================================

    public List<ExpenseResponse>
    getExpensesByMonth(
            int month,
            int year) {

        User user =
                getLoggedInUser();


        YearMonth yearMonth =
                YearMonth.of(
                        year,
                        month
                );


        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();


        List<Expense> expenses =
                expenseRepository
                        .findByUserAndDateBetween(
                                user,
                                startDate,
                                endDate
                        );


        return expenses.stream()
                .map(expense ->
                        new ExpenseResponse(
                                expense.getId(),
                                expense.getAmount(),
                                expense.getDescription(),
                                expense.getDate(),
                                expense.getCategory() != null
                                        ? expense.getCategory().getId()
                                        : null,
                                expense.getCategory() != null
                                        ? expense.getCategory().getName()
                                        : null
                        )
                )
                .toList();
    }


    // ==========================================
    // GET EXPENSES BY DATE RANGE
    // ==========================================

    public List<ExpenseResponse>
    getExpensesByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }


        User user =
                getLoggedInUser();


        List<Expense> expenses =
                expenseRepository
                        .findByUserAndDateBetween(
                                user,
                                startDate,
                                endDate
                        );


        return expenses.stream()
                .map(expense ->
                        new ExpenseResponse(
                                expense.getId(),
                                expense.getAmount(),
                                expense.getDescription(),
                                expense.getDate(),
                                expense.getCategory() != null
                                        ? expense.getCategory().getId()
                                        : null,
                                expense.getCategory() != null
                                        ? expense.getCategory().getName()
                                        : null
                        )
                )
                .toList();
    }


    // ==========================================
    // SEARCH EXPENSES
    // ==========================================

    public List<ExpenseResponse>
    searchExpensesByDescription(
            String description) {

        User user =
                getLoggedInUser();


        List<Expense> expenses =
                expenseRepository
                        .findByUserAndDescriptionContainingIgnoreCase(
                                user,
                                description
                        );


        return expenses.stream()
                .map(expense ->
                        new ExpenseResponse(
                                expense.getId(),
                                expense.getAmount(),
                                expense.getDescription(),
                                expense.getDate(),
                                expense.getCategory() != null
                                        ? expense.getCategory().getId()
                                        : null,
                                expense.getCategory() != null
                                        ? expense.getCategory().getName()
                                        : null
                        )
                )
                .toList();
    }


    // ==========================================
    // DASHBOARD
    // ==========================================

    public DashboardResponse getDashboard() {

        User user =
                getLoggedInUser();

        LocalDate today =
                LocalDate.now();


        // 1. GET ALL EXPENSES

        List<Expense> allExpenses =
                expenseRepository.findByUser(user);


        // 2. TOTAL SPENDING

        Double totalSpending =
                allExpenses.stream()
                        .map(Expense::getAmount)
                        .reduce(
                                0.0,
                                Double::sum
                        );


        // 3. CURRENT MONTH SPENDING

        LocalDate startOfMonth =
                today.withDayOfMonth(1);

        LocalDate endOfMonth =
                today.withDayOfMonth(
                        today.lengthOfMonth()
                );


        List<Expense> monthlyExpenses =
                expenseRepository
                        .findByUserAndDateBetween(
                                user,
                                startOfMonth,
                                endOfMonth
                        );


        Double monthlySpending =
                monthlyExpenses.stream()
                        .map(Expense::getAmount)
                        .reduce(
                                0.0,
                                Double::sum
                        );


        // 4. TODAY'S SPENDING

        Double todaySpending =
                allExpenses.stream()
                        .filter(expense ->
                                expense.getDate()
                                        .equals(today)
                        )
                        .map(Expense::getAmount)
                        .reduce(
                                0.0,
                                Double::sum
                        );


        // 5. CATEGORY-WISE SPENDING

        Map<String, Double>
                categorySpending =
                monthlyExpenses.stream()
                        .collect(
                                Collectors.groupingBy(
                                        expense ->
                                                expense.getCategory()
                                                        .getName(),

                                        Collectors.summingDouble(
                                                Expense::getAmount
                                        )
                                )
                        );


        // 6. HIGHEST SPENDING CATEGORY

        String highestSpendingCategory =
                categorySpending.entrySet()
                        .stream()
                        .max(
                                Map.Entry.comparingByValue()
                        )
                        .map(
                                Map.Entry::getKey
                        )
                        .orElse(
                                "No spending yet"
                        );


        // 7. RECENT 5 EXPENSES

        List<ExpenseResponse>
                recentExpenses =
                allExpenses.stream()
                        .sorted(
                                Comparator.comparing(
                                                Expense::getDate
                                        )
                                        .reversed()
                                        .thenComparing(
                                                Expense::getCreatedAt,
                                                Comparator.nullsLast(
                                                        Comparator.reverseOrder()
                                                )
                                        )
                        )
                        .limit(5)
                        .map(expense ->
                                new ExpenseResponse(
                                        expense.getId(),
                                        expense.getAmount(),
                                        expense.getDescription(),
                                        expense.getDate(),
                                        expense.getCategory() != null
                                                ? expense.getCategory().getId()
                                                : null,
                                        expense.getCategory() != null
                                                ? expense.getCategory().getName()
                                                : null
                                )
                        )
                        .toList();


        // 8. REMAINING BUDGET

        Double remainingBudget = 0.0;

        try {

            Map<String, Object>
                    budgetStatus =
                    spendingLimitService
                            .getBudgetStatus();


            Object remaining =
                    budgetStatus.get(
                            "remaining"
                    );


            if (remaining instanceof Number) {

                remainingBudget =
                        ((Number) remaining)
                                .doubleValue();
            }

        } catch (Exception ignored) {

            // No spending limit set
        }


        // 9. CREATE DASHBOARD RESPONSE

        return new DashboardResponse(
                totalSpending,
                monthlySpending,
                todaySpending,
                remainingBudget,
                categorySpending,
                highestSpendingCategory,
                recentExpenses
        );
    }

}