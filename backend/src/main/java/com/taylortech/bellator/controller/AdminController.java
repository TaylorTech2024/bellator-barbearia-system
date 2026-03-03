package com.taylortech.bellator.controller;

import com.taylortech.bellator.dto.RelatorioResponse;
import com.taylortech.bellator.model.Agendamento;
import com.taylortech.bellator.model.StatusAgendamento;
import com.taylortech.bellator.service.AgendamentoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AgendamentoService agendamentoService;

    public AdminController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/agendamentos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Agendamento> listarTodos() {
        return agendamentoService.listarTodos();
    }

    @GetMapping("/relatorio")
    @PreAuthorize("hasRole('ADMIN')")
    public RelatorioResponse relatorio() {
        List<Agendamento> all = agendamentoService.listarTodos();
        long total = all.size();
        long concluidos = all.stream().filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO).count();
        double faturamento = all.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.CONCLUIDO)
                .mapToDouble(a -> a.getServico().getPreco() == null ? 0.0 : a.getServico().getPreco())
                .sum();
        return new RelatorioResponse(total, concluidos, faturamento);
    }
}
