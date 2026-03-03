package com.taylortech.bellator.service;

import com.taylortech.bellator.dto.AgendamentoCreateRequest;
import com.taylortech.bellator.exception.ApiException;
import com.taylortech.bellator.model.Agendamento;
import com.taylortech.bellator.model.StatusAgendamento;
import com.taylortech.bellator.model.Usuario;
import com.taylortech.bellator.repository.AgendamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repo;
    private final UsuarioService usuarioService;
    private final ServicoService servicoService;

    public AgendamentoService(AgendamentoRepository repo, UsuarioService usuarioService, ServicoService servicoService) {
        this.repo = repo;
        this.usuarioService = usuarioService;
        this.servicoService = servicoService;
    }

    public Agendamento criar(String clienteEmail, AgendamentoCreateRequest req) {
        Usuario cliente = usuarioService.byEmail(clienteEmail);
        Usuario barbeiro = usuarioService.byId(req.barbeiroId);

        if (!"BARBEIRO".equals(barbeiro.getRole().name())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Usuário selecionado não é barbeiro");
        }

        if (repo.existsByBarbeiroAndDataAndHorario(barbeiro, req.data, req.horario)) {
            throw new ApiException(HttpStatus.CONFLICT, "Horário já ocupado");
        }

        Agendamento a = new Agendamento();
        a.setCliente(cliente);
        a.setBarbeiro(barbeiro);
        a.setServico(servicoService.byId(req.servicoId));
        a.setData(req.data);
        a.setHorario(req.horario);
        a.setStatus(StatusAgendamento.AGENDADO);
        return repo.save(a);
    }

    public List<Agendamento> meus(String clienteEmail) {
        Usuario cliente = usuarioService.byEmail(clienteEmail);
        return repo.findByClienteOrderByDataDescHorarioDesc(cliente);
    }

    public List<Agendamento> agendaBarbeiro(String barbeiroEmail, LocalDate data) {
        Usuario barbeiro = usuarioService.byEmail(barbeiroEmail);
        return repo.findByBarbeiroAndDataOrderByHorarioAsc(barbeiro, data);
    }

    public Agendamento concluir(Long id, String barbeiroEmail) {
        Agendamento a = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
        if (!a.getBarbeiro().getEmail().equalsIgnoreCase(barbeiroEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Você não pode concluir esse agendamento");
        }
        a.setStatus(StatusAgendamento.CONCLUIDO);
        return repo.save(a);
    }

    public Agendamento cancelar(Long id, String requesterEmail, boolean isAdmin) {
        Agendamento a = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
        boolean isClienteDono = a.getCliente().getEmail().equalsIgnoreCase(requesterEmail);
        if (!isAdmin && !isClienteDono) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Você não pode cancelar esse agendamento");
        }
        a.setStatus(StatusAgendamento.CANCELADO);
        return repo.save(a);
    }

    public List<Agendamento> listarTodos() {
        return repo.findAll();
    }
}
