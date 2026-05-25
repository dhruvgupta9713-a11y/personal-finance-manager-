package com.finance.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// login request body
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
