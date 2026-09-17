package com.expensetracker.BuckSave.repository;

import com.expensetracker.BuckSave.entity.Category;
import com.expensetracker.BuckSave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser(User user);

    boolean existsByNameIgnoreCaseAndUser(
            String name,
            User user
    );
}