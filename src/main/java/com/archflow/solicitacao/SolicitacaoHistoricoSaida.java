package com.archflow.solicitacao;

import java.time.LocalDateTime;

// O que o CT08 devolve por pedido do historico. Sai tambem a data da ultima
// mudanca, porque no historico o que interessa e quando o pedido terminou,
// e nao so quando ele comecou.
public class SolicitacaoHistoricoSaida {

    private Long solicitacaoId;

    private String material;

    private int quantidade;

    private StatusSolicitacao status;

    private LocalDateTime criadaEm;

    private LocalDateTime atualizadaEm;

    public SolicitacaoHistoricoSaida(Solicitacao solicitacao) {
        this.solicitacaoId = solicitacao.getSolicitacaoId();
        this.material = solicitacao.getMaterial().getNome();
        this.quantidade = solicitacao.getQuantidade();
        this.status = solicitacao.getStatus();
        this.criadaEm = solicitacao.getCriadaEm();
        this.atualizadaEm = solicitacao.getAtualizadaEm();
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

    public LocalDateTime getAtualizadaEm() {
        return this.atualizadaEm;
    }
}
