package com.expensetracker.BuckSave.repository;

import com.expensetracker.BuckSave.entity.Category;
import com.expensetracker.BuckSave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    // User's custom categories
    List<Category> findByUser(User user);

    // Shared default categories
    List<Category> findByUserIsNull();

    // Check whether user already has a category with this name
    boolean existsByNameIgnoreCaseAndUser(
            String name,
            User user
    );

    // Check whether a shared default category already exists
    boolean existsByNameIgnoreCaseAndUserIsNull(
            String name
    );
}