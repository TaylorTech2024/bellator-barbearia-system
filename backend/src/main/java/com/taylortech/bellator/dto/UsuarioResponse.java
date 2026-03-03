package com.taylortech.bellator.dto;

import com.taylortech.bellator.model.Role;

public class UsuarioResponse {
    public Long id;
    public String nome;
    public String email;
    public Role role;

    public UsuarioResponse(Long id, String nome, String email, Role role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
    }
}
