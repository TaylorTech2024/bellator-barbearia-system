package com.taylortech.bellator.service;

import com.taylortech.bellator.exception.ApiException;
import com.taylortech.bellator.model.Role;
import com.taylortech.bellator.model.Usuario;
import com.taylortech.bellator.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    public Usuario byEmail(String email) {
        return repo.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public Usuario byId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public List<Usuario> listarBarbeiros() {
        return repo.findByRole(Role.BARBEIRO);
    }
}
