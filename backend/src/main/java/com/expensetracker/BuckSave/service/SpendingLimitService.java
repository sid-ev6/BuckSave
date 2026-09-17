package com.expensetracker.BuckSave.service;

import com.expensetracker.BuckSave.dto.ExpenseResponse;
import com.expensetracker.BuckSave.dto.SpendingAnalysisResponse;
import com.expensetracker.BuckSave.dto.SpendingLimitRequest;
import com.expensetracker.BuckSave.dto.SpendingLimitResponse;
import com.expensetracker.BuckSave.entity.*;
import com.expensetracker.BuckSave.repository.ExpenseRepository;
import com.expensetracker.BuckSave.repository.SpendingLimitRepository;
import com.expensetracker.BuckSave.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class SpendingLimitService {

    private final SpendingLimitRepository spendingLimitRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public SpendingLimitService(
            SpendingLimitRepository spendingLimitRepository,
            UserRepository userRepository, ExpenseRepository expenseRepository) {

        this.spendingLimitRepository = spendingLimitRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public SpendingLimitResponse createSpendingLimit(
            SpendingLimitRequest request) {

        User user = getLoggedInUser();

        LocalDate today = LocalDate.now();

        SpendingLimit spendingLimit = new SpendingLimit();

        spendingLimit.setAmount(request.getAmount());
        spendingLimit.setLimitType(request.getLimitType());
        spendingLimit.setUser(user);

        // Month and year when the limit was created
        spendingLimit.setMonth(
                Month.valueOf(today.getMonth().name())
        );
        spendingLimit.setYear(today.getYear());

        // DAILY
        if (request.getLimitType() == LimitType.DAILY) {

            spendingLimit.setStartDate(today);
            spendingLimit.setEndDate(today);
        }

        // WEEKLY
        else if (request.getLimitType() == LimitType.WEEKLY) {

            LocalDate startDate =
                    today.with(DayOfWeek.MONDAY);

            LocalDate endDate =
                    today.with(DayOfWeek.SUNDAY);

            spendingLimit.setStartDate(startDate);
            spendingLimit.setEndDate(endDate);
        }

        // MONTHLY
        else if (request.getLimitType() == LimitType.MONTHLY) {

            LocalDate startDate =
                    today.withDayOfMonth(1);

            LocalDate endDate =
                    today.withDayOfMonth(today.lengthOfMonth());

            spendingLimit.setStartDate(startDate);
            spendingLimit.setEndDate(endDate);
        }

        if (spendingLimitRepository.existsByUserAndStartDateAndEndDate(
                user,
                spendingLimit.getStartDate(),
                spendingLimit.getEndDate())) {

            throw new RuntimeException(
                    "A spending limit already exists for this period"
            );
        }

        SpendingLimit savedLimit =
                spendingLimitRepository.save(spendingLimit);

        return convertToResponse(savedLimit);
    }
    // GET ALL MY LIMITS
    public List<SpendingLimitResponse> getMySpendingLimits() {

        User user = getLoggedInUser();

        List<SpendingLimit> limits =
                spendingLimitRepository.findByUser(user);

        return limits.stream()
                .map(this::convertToResponse)
                .toList();
    }


    // GET ONE
    public SpendingLimitResponse getSpendingLimitById(Long id) {

        User user = getLoggedInUser();

        SpendingLimit spendingLimit =
                spendingLimitRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Spending limit not found"));

        if (!spendingLimit.getUser().getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to access this spending limit");
        }

        return convertToResponse(spendingLimit);
    }


    // UPDATE

    @Transactional
    public SpendingLimitResponse updateSpendingLimit(
            Long id,
            SpendingLimitRequest request) {

        User user = getLoggedInUser();

        SpendingLimit spendingLimit =
                spendingLimitRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Spending limit not found"
                                ));

        // Ownership check
        if (!spendingLimit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You are not allowed to update this spending limit"
            );
        }

        // Only update the amount
        spendingLimit.setAmount(request.getAmount());

        // Do NOT update:
        // spendingLimit.setLimitType(...)
        // spendingLimit.setMonth(...)
        // spendingLimit.setYear(...)
        // spendingLimit.setStartDate(...)
        // spendingLimit.setEndDate(...)

        SpendingLimit updatedLimit =
                spendingLimitRepository.save(spendingLimit);

        return convertToResponse(updatedLimit);
    }
    // DELETE
    public void deleteSpendingLimit(Long id) {

        User user = getLoggedInUser();

        SpendingLimit spendingLimit =
                spendingLimitRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Spending limit not found"));

        if (!spendingLimit.getUser().getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to delete this spending limit");
        }

        spendingLimitRepository.delete(spendingLimit);
    }


    // GET LOGGED-IN USER
    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }


    // ENTITY → RESPONSE
    private SpendingLimitResponse convertToResponse(
            SpendingLimit spendingLimit) {

        return new SpendingLimitResponse(
                spendingLimit.getId(),
                spendingLimit.getAmount(),
                spendingLimit.getLimitType(),
                spendingLimit.getMonth(),
                spendingLimit.getYear(),
                spendingLimit.getStartDate(),
                spendingLimit.getEndDate()
        );
    }
    public SpendingAnalysisResponse getSpendingAnalysis(Long id) {

        User user = getLoggedInUser();

        SpendingLimit spendingLimit =
                spendingLimitRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Spending limit not found"
                                ));

        // Ownership check
        if (!spendingLimit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You are not allowed to access this spending limit"
            );
        }

        List<Expense> expenses =
                expenseRepository.findByUserAndDateBetween(
                        user,
                        spendingLimit.getStartDate(),
                        spendingLimit.getEndDate()
                );

        double totalSpent = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        double remainingAmount =
                spendingLimit.getAmount() - totalSpent;

        List<ExpenseResponse> expenseResponses =
                expenses.stream()
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
                        .map(expense -> new ExpenseResponse(
                                expense.getId(),
                                expense.getAmount(),
                                expense.getDescription(),
                                expense.getDate(),
                                expense.getCategory().getId(),
                                expense.getCategory().getName()
                        ))
                        .toList();

        return new SpendingAnalysisResponse(
                totalSpent,
                remainingAmount,
                expenseResponses
        );
    }

    public Map<String, Object> getBudgetStatus() {

        User user = getLoggedInUser();

        LocalDate today = LocalDate.now();

        List<SpendingLimit> limits =
                spendingLimitRepository.findByUser(user);

        // Find the MONTHLY limit that is active today
        SpendingLimit activeLimit = limits.stream()
                .filter(limit ->
                        limit.getLimitType() == LimitType.MONTHLY)
                .filter(limit ->
                        limit.getStartDate() != null &&
                                limit.getEndDate() != null)
                .filter(limit ->
                        !today.isBefore(limit.getStartDate()) &&
                                !today.isAfter(limit.getEndDate()))
                .findFirst()
                .orElse(null);

        if (activeLimit == null) {
            return Map.of(
                    "message", "No active monthly spending limit set"
            );
        }

        // Get expenses for the monthly limit period
        List<Expense> expenses =
                expenseRepository.findByUserAndDateBetween(
                        user,
                        activeLimit.getStartDate(),
                        activeLimit.getEndDate()
                );

        Double spent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(0.0, Double::sum);

        Double limitAmount =
                activeLimit.getAmount();

        Double remaining =
                limitAmount - spent;

        boolean exceeded =
                spent > limitAmount;

        return Map.of(
                "limit", limitAmount,
                "spent", spent,
                "remaining", remaining,
                "exceeded", exceeded
        );
    }
}