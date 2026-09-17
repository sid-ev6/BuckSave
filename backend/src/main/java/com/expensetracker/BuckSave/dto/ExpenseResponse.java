package com.expensetracker.BuckSave.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;

    private Double amount;

    private String description;

    private LocalDate date;

    private Long categoryId;

    private String categoryName;
}
