package com.taylortech.bellator.dto;

import com.taylortech.bellator.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuthRegisterRequest {
    @NotBlank
    public String nome;

    @Email @NotBlank
    public String email;

    @NotBlank
    public String senha;

    @NotNull
    public Role role;
}
