package com.expensetracker.BuckSave.service;

import com.expensetracker.BuckSave.dto.*;
import com.expensetracker.BuckSave.entity.Expense;
import com.expensetracker.BuckSave.entity.SpendingLimit;
import com.expensetracker.BuckSave.entity.User;
import com.expensetracker.BuckSave.repository.ExpenseRepository;
import com.expensetracker.BuckSave.repository.SpendingLimitRepository;
import com.expensetracker.BuckSave.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final SpendingLimitRepository spendingLimitRepository;
    private final UserRepository userRepository;



    public DashboardService(
            ExpenseRepository expenseRepository,
            SpendingLimitRepository spendingLimitRepository,
            UserRepository userRepository) {

        this.expenseRepository = expenseRepository;
        this.spendingLimitRepository = spendingLimitRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse getDashboard() {


        // Get logged-in user's email from JWT
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // Find the logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Get all expenses of this user
        List<Expense> expenses =
                expenseRepository.findByUser(user);

        LocalDate today = LocalDate.now();

        // Current month
        YearMonth currentMonth =
                YearMonth.now();

        LocalDate monthStart =
                currentMonth.atDay(1);

        LocalDate monthEnd =
                currentMonth.atEndOfMonth();

        // Total spending
        Double totalSpending =
                expenses.stream()
                        .mapToDouble(Expense::getAmount)
                        .sum();

        // Monthly spending
        Double monthlySpending =
                expenses.stream()
                        .filter(expense ->
                                !expense.getDate().isBefore(monthStart)
                                        &&
                                        !expense.getDate().isAfter(monthEnd))
                        .mapToDouble(Expense::getAmount)
                        .sum();

        // Today's spending
        Double todaySpending =
                expenses.stream()
                        .filter(expense ->
                                expense.getDate().equals(today))
                        .mapToDouble(Expense::getAmount)
                        .sum();

        // Find active spending limit
        List<SpendingLimit> limits =
                spendingLimitRepository.findByUser(user);

        Double budget = limits.stream()
                .filter(limit ->
                        limit.getStartDate() != null &&
                                limit.getEndDate() != null)
                .filter(limit ->
                        !today.isBefore(limit.getStartDate())
                                &&
                                !today.isAfter(limit.getEndDate()))
                .map(SpendingLimit::getAmount)
                .findFirst()
                .orElse(0.0);
        // Remaining budget
        Double remainingBudget =
                budget - monthlySpending;

        // Spending by category
        Map<String, Double> categorySpending =
                expenses.stream()
                        .filter(expense ->
                                expense.getCategory() != null)
                        .collect(Collectors.groupingBy(
                                expense ->
                                        expense.getCategory().getName(),
                                LinkedHashMap::new,
                                Collectors.summingDouble(
                                        Expense::getAmount
                                )
                        ));

        // Highest spending category
        String highestSpendingCategory =
                categorySpending.entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("No spending yet");

        // Recent 5 expenses of the current month
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        List<ExpenseResponse> recentExpenses =
                expenses.stream()
                        .filter(expense ->
                                !expense.getDate().isBefore(startOfMonth)
                                        && !expense.getDate().isAfter(endOfMonth)
                        )
                        .sorted(
                                Comparator.comparing(Expense::getDate)
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
    private User getLoggedInUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
    public MonthlySummaryResponse getMonthlySummary(
            int month,
            int year) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Month must be between 1 and 12"
            );
        }
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException(
                    "Year must be between 2000 and 2100"
            );
        }
        User user = getLoggedInUser();

        YearMonth yearMonth =
                YearMonth.of(year, month);

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        List<Expense> expenses =
                expenseRepository.findByUserAndDateBetween(
                        user,
                        startDate,
                        endDate
                );

        Double totalSpending =
                expenses.stream()
                        .mapToDouble(Expense::getAmount)
                        .sum();

        List<SpendingLimit> limits =
                spendingLimitRepository.findByUser(user);

        Double budget = limits.stream()
                .filter(limit ->
                        limit.getStartDate() != null &&
                                limit.getEndDate() != null)
                .filter(limit ->
                        !startDate.isBefore(limit.getStartDate())
                                &&
                                !endDate.isAfter(limit.getEndDate())
                )
                .map(SpendingLimit::getAmount)
                .findFirst()
                .orElse(0.0);

        Double remaining =
                budget - totalSpending;

        return new MonthlySummaryResponse(
                yearMonth.getMonth().name(),
                year,
                totalSpending,
                budget,
                remaining
        );
    }

    public DailySummaryResponse getDailySummary(LocalDate date) {

        User user = getLoggedInUser();

        // Get all expenses for this day
        List<Expense> expenses =
                expenseRepository.findByUserAndDateBetween(
                        user,
                        date,
                        date
                );

        // Calculate total spending
        Double totalSpending =
                expenses.stream()
                        .mapToDouble(Expense::getAmount)
                        .sum();

        // Find active spending limit
        List<SpendingLimit> limits =
                spendingLimitRepository.findByUser(user);

        Double budget = limits.stream()
                .filter(limit ->
                        limit.getStartDate() != null &&
                                limit.getEndDate() != null)
                .filter(limit ->
                        !date.isBefore(limit.getStartDate())
                                &&
                                !date.isAfter(limit.getEndDate())
                )
                .map(SpendingLimit::getAmount)
                .findFirst()
                .orElse(0.0);

        // Calculate remaining budget
        Double remaining =
                budget - totalSpending;

        return new DailySummaryResponse(
                date.toString(),
                totalSpending,
                budget,
                remaining
        );
    }


    public WeeklySummaryResponse getWeeklySummary(LocalDate date) {

        User user = getLoggedInUser();

        // Find Monday and Sunday of the requested week
        LocalDate weekStart =
                date.with(DayOfWeek.MONDAY);

        LocalDate weekEnd =
                date.with(DayOfWeek.SUNDAY);

        // Get expenses for the week
        List<Expense> expenses =
                expenseRepository.findByUserAndDateBetween(
                        user,
                        weekStart,
                        weekEnd
                );

        // Calculate total spending
        Double totalSpending =
                expenses.stream()
                        .mapToDouble(Expense::getAmount)
                        .sum();

        // Find active spending limit
        List<SpendingLimit> limits =
                spendingLimitRepository.findByUser(user);

        Double budget = limits.stream()
                .filter(limit ->
                        limit.getStartDate() != null &&
                                limit.getEndDate() != null)
                .filter(limit ->
                        !weekStart.isBefore(limit.getStartDate())
                                &&
                                !weekEnd.isAfter(limit.getEndDate())
                )
                .map(SpendingLimit::getAmount)
                .findFirst()
                .orElse(0.0);

        // Calculate remaining budget
        Double remaining =
                budget - totalSpending;

        return new WeeklySummaryResponse(
                weekStart.toString(),
                weekEnd.toString(),
                totalSpending,
                budget,
                remaining
        );
    }


}