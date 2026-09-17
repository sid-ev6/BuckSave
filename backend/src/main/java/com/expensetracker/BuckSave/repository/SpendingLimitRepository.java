package com.expensetracker.BuckSave.repository;


import com.expensetracker.BuckSave.entity.SpendingLimit;
import com.expensetracker.BuckSave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpendingLimitRepository
        extends JpaRepository<SpendingLimit, Long> {

    List<SpendingLimit> findByUser(User user);

    boolean existsByUserAndStartDateAndEndDate(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );
    boolean existsByUserAndStartDateAndEndDateAndIdNot(
            User user,
            LocalDate startDate,
            LocalDate endDate,
            Long id
    );
}