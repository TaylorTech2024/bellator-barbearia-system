package com.taylortech.bellator.service;

import com.taylortech.bellator.dto.ServicoRequest;
import com.taylortech.bellator.exception.ApiException;
import com.taylortech.bellator.model.Servico;
import com.taylortech.bellator.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository repo;

    public ServicoService(ServicoRepository repo) {
        this.repo = repo;
    }

    public List<Servico> listar() {
        return repo.findAll();
    }

    public Servico criar(ServicoRequest req) {
        Servico s = new Servico();
        s.setNome(req.nome);
        s.setPreco(req.preco);
        s.setDuracaoMinutos(req.duracaoMinutos);
        return repo.save(s);
    }

    public Servico atualizar(Long id, ServicoRequest req) {
        Servico s = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
        s.setNome(req.nome);
        s.setPreco(req.preco);
        s.setDuracaoMinutos(req.duracaoMinutos);
        return repo.save(s);
    }

    public void deletar(Long id) {
        if (!repo.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Serviço não encontrado");
        }
        repo.deleteById(id);
    }

    public Servico byId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
    }
}
