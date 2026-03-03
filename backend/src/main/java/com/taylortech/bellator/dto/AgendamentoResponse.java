package com.taylortech.bellator.dto;

import com.taylortech.bellator.model.StatusAgendamento;
import java.time.LocalDate;
import java.time.LocalTime;

public class AgendamentoResponse {
    public Long id;
    public String clienteNome;
    public String barbeiroNome;
    public String servicoNome;
    public Double servicoPreco;
    public LocalDate data;
    public LocalTime horario;
    public StatusAgendamento status;

    public AgendamentoResponse(Long id, String clienteNome, String barbeiroNome, String servicoNome, Double servicoPreco,
                               LocalDate data, LocalTime horario, StatusAgendamento status) {
        this.id = id;
        this.clienteNome = clienteNome;
        this.barbeiroNome = barbeiroNome;
        this.servicoNome = servicoNome;
        this.servicoPreco = servicoPreco;
        this.data = data;
        this.horario = horario;
        this.status = status;
    }
}
