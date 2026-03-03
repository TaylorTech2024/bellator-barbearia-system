package com.taylortech.bellator.dto;

import com.taylortech.bellator.model.Role;

public class AuthResponse {
    public String token;
    public Role role;
    public UsuarioResponse user;

    public AuthResponse(String token, Role role, UsuarioResponse user) {
        this.token = token;
        this.role = role;
        this.user = user;
    }
}
