package com.github.valdpq.mentoringplatform.auth.dto;

import com.github.valdpq.mentoringplatform.auth.RegistrableRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password should be at least 6 characters")
        String password,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotNull(message = "Role is required")
        RegistrableRole role
) {
}
