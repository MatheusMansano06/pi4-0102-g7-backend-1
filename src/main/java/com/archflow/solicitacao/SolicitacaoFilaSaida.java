package com.archflow.solicitacao;

import java.time.LocalDateTime;

// O que o CT04 devolve por pedido da fila. O responsavel decide a triagem
// pela dimensao, pelo diametro e pela observacao, entao os tres vao junto.
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
