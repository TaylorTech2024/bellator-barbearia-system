package com.taylortech.bellator.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class AgendamentoCreateRequest {
    @NotNull
    public Long servicoId;

    @NotNull
    public Long barbeiroId;

    @NotNull
    public LocalDate data;

    @NotNull
    public LocalTime horario;
}
