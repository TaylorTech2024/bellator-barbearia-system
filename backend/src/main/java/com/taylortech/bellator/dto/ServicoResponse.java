package com.taylortech.bellator.dto;

public class ServicoResponse {
    public Long id;
    public String nome;
    public Double preco;
    public Integer duracaoMinutos;

    public ServicoResponse(Long id, String nome, Double preco, Integer duracaoMinutos) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.duracaoMinutos = duracaoMinutos;
    }
}
