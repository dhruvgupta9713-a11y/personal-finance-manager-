package com.finance.manager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// User entity - stores login info and profile
// username is actually an email address
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // using email as username
    @Column(unique = true)
    @NotBlank
    private String username;

    // stored as bcrypt hash
    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    // phone is optional
    private String phoneNumber;
}
