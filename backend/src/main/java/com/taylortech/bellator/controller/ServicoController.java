package com.taylortech.bellator.controller;

import com.taylortech.bellator.dto.ServicoRequest;
import com.taylortech.bellator.dto.ServicoResponse;
import com.taylortech.bellator.model.Servico;
import com.taylortech.bellator.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServicoResponse> listar() {
        return service.listar().stream()
                .map(s -> new ServicoResponse(s.getId(), s.getNome(), s.getPreco(), s.getDuracaoMinutos()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ServicoResponse criar(@Valid @RequestBody ServicoRequest req) {
        Servico s = service.criar(req);
        return new ServicoResponse(s.getId(), s.getNome(), s.getPreco(), s.getDuracaoMinutos());
    }

    @PutMapping("/<built-in function id>")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ServicoRequest req) {
        Servico s = service.atualizar(id, req);
        return new ServicoResponse(s.getId(), s.getNome(), s.getPreco(), s.getDuracaoMinutos());
    }

    @DeleteMapping("/<built-in function id>")
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
