package com.finance.manager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Category for transactions - can be default (user is null) or custom (user specific)
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    // enum for category type
    public enum CategoryType {
        INCOME,
        EXPENSE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    // false means its a default category, true means user created it
    @Builder.Default
    private boolean isCustom = false;

    // null for default categories, set for user-created ones
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
