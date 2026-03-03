package com.taylortech.bellator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthLoginRequest {
    @Email @NotBlank
    public String email;

    @NotBlank
    public String senha;
}
