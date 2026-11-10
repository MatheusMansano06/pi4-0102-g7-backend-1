package com.archflow.solicitacao;

import java.time.LocalDateTime;

// O que o CT04 devolve por pedido da fila. O material sai como o nome dele,
// porque a tela da fila mostra texto, e nao a ficha inteira do material.
// Dimensao, diametro e observacao vem junto porque a triagem decide por eles:
// sem a medida pedida, o responsavel teria de abrir pedido por pedido.
public class SolicitacaoFilaSaida {

    private Long solicitacaoId;

    private String material;

    private int quantidade;

    private StatusSolicitacao status;

    private LocalDateTime criadaEm;

    private String dimensao;

    private Double diametro;

    private String observacao;

    public SolicitacaoFilaSaida(Solicitacao solicitacao) {
        this.solicitacaoId = solicitacao.getSolicitacaoId();
        this.material = solicitacao.getMaterial().getNome();
        this.quantidade = solicitacao.getQuantidade();
        this.status = solicitacao.getStatus();
        this.criadaEm = solicitacao.getCriadaEm();
        this.dimensao = solicitacao.getDimensao();
        this.diametro = solicitacao.getDiametro();
        this.observacao = solicitacao.getObservacao();
    }

    public Long getSolicitacaoId() {
        return this.solicitacaoId;
    }

    public String getMaterial() {
        return this.material;
    }

    public int getQuantidade() {
        return this.quantidade;
    }

    public StatusSolicitacao getStatus() {
        return this.status;
    }

    public LocalDateTime getCriadaEm() {
        return this.criadaEm;
    }

    public String getDimensao() {
        return this.dimensao;
    }

    public Double getDiametro() {
        return this.diametro;
    }

    public String getObservacao() {
        return this.observacao;
    }
}
