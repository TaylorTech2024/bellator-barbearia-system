package com.taylortech.bellator.controller;

import com.taylortech.bellator.dto.AgendamentoCreateRequest;
import com.taylortech.bellator.dto.AgendamentoResponse;
import com.taylortech.bellator.model.Agendamento;
import com.taylortech.bellator.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @PostMapping
    public AgendamentoResponse criar(Authentication auth, @Valid @RequestBody AgendamentoCreateRequest req) {
        Agendamento a = service.criar(auth.getName(), req);
        return toResp(a);
    }

    @GetMapping("/me")
    public List<AgendamentoResponse> meus(Authentication auth) {
        return service.meus(auth.getName()).stream().map(this::toResp).toList();
    }

    @GetMapping("/barbeiro")
    public List<AgendamentoResponse> agendaBarbeiro(Authentication auth, @RequestParam LocalDate data) {
        return service.agendaBarbeiro(auth.getName(), data).stream().map(this::toResp).toList();
    }

    @PutMapping("/<built-in function id>/concluir")
    public AgendamentoResponse concluir(Authentication auth, @PathVariable Long id) {
        return toResp(service.concluir(id, auth.getName()));
    }

    @PutMapping("/<built-in function id>/cancelar")
    public AgendamentoResponse cancelar(Authentication auth, @PathVariable Long id) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return toResp(service.cancelar(id, auth.getName(), isAdmin));
    }

    private AgendamentoResponse toResp(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getCliente().getNome(),
                a.getBarbeiro().getNome(),
                a.getServico().getNome(),
                a.getServico().getPreco(),
                a.getData(),
                a.getHorario(),
                a.getStatus()
        );
    }
}
