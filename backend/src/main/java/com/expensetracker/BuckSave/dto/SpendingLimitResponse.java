package com.expensetracker.BuckSave.dto;

import com.expensetracker.BuckSave.entity.LimitType;
import com.expensetracker.BuckSave.entity.Month;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SpendingLimitResponse {

    private Long id;
    private Double amount;
    private LimitType limitType;
    private Month month;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;


}