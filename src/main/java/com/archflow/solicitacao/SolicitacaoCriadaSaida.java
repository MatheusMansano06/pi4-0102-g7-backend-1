package com.archflow.solicitacao;

import java.time.LocalDateTime;

public class SolicitacaoCriadaSaida {

    private final Long solicitacaoId;

    private final Long materialId;

    private final int quantidade;

    private final StatusSolicitacao status;

    private final LocalDateTime criadaEm;

    private final String dimensao;

    private final Double diametro;

    private final String observacao;

    public SolicitacaoCriadaSaida(Solicitacao solicitacao) {
        this.solicitacaoId = solicitacao.getSolicitacaoId();
        this.materialId = solicitacao.getMaterial().getMaterialId();
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

    public Long getMaterialId() {
        return this.materialId;
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
