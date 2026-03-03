package com.taylortech.bellator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agendamentos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_barbeiro_data_hora", columnNames = {"barbeiro_id","data","horario"})
        }
)
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "barbeiro_id")
    private Usuario barbeiro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @NotNull
    private LocalDate data;

    @NotNull
    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    public Long getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public Usuario getBarbeiro() { return barbeiro; }
    public Servico getServico() { return servico; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public StatusAgendamento getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }
    public void setBarbeiro(Usuario barbeiro) { this.barbeiro = barbeiro; }
    public void setServico(Servico servico) { this.servico = servico; }
    public void setData(LocalDate data) { this.data = data; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public void setStatus(StatusAgendamento status) { this.status = status; }
}
