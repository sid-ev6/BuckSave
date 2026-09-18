package com.expensetracker.BuckSave.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "Category")
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "User_id", nullable = true)
    private User user;
}