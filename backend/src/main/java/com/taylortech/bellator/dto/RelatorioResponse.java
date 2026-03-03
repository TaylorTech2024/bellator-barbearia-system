package com.taylortech.bellator.dto;

public class RelatorioResponse {
    public long totalAgendamentos;
    public long concluidos;
    public double faturamentoTotal;

    public RelatorioResponse(long totalAgendamentos, long concluidos, double faturamentoTotal) {
        this.totalAgendamentos = totalAgendamentos;
        this.concluidos = concluidos;
        this.faturamentoTotal = faturamentoTotal;
    }
}
