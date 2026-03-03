package com.taylortech.bellator.controller;

import com.taylortech.bellator.dto.UsuarioResponse;
import com.taylortech.bellator.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbeiros")
public class BarbeiroController {

    private final UsuarioService usuarioService;

    public BarbeiroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listarBarbeiros().stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getRole()))
                .toList();
    }
}
