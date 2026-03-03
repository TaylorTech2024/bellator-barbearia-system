package com.taylortech.bellator.repository;

import com.taylortech.bellator.model.Agendamento;
import com.taylortech.bellator.model.StatusAgendamento;
import com.taylortech.bellator.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    boolean existsByBarbeiroAndDataAndHorario(Usuario barbeiro, LocalDate data, LocalTime horario);

    List<Agendamento> findByClienteOrderByDataDescHorarioDesc(Usuario cliente);

    List<Agendamento> findByBarbeiroAndDataOrderByHorarioAsc(Usuario barbeiro, LocalDate data);

    List<Agendamento> findByBarbeiroAndDataAndStatusNotOrderByHorarioAsc(Usuario barbeiro, LocalDate data, StatusAgendamento status);
}
