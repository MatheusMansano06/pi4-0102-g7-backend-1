package com.archflow.solicitacao;

import java.time.LocalDateTime;

// O que o CT06 devolve depois de gravar: o pedido com o status novo, a
// previsao que vale agora e a hora em que a mudanca aconteceu.
public class SolicitacaoStatusSaida {

    private Long solicitacaoId;

    private StatusSolicitacao status;

    private LocalDateTime previsaoRetirada;

    private LocalDateTime atualizadaEm;

    public SolicitacaoStatusSaida(Solicitacao solicitacao) {
        this.solicitacaoId = solicitacao.getSolicitacaoId();
        this.status = solicitacao.getStatus();
        this.previsaoRetirada = solicitacao.getPrevisaoRetirada();
        this.atualizadaEm = solicitacao.getAtualizadaEm();
    }

    public Long getSolicitacaoId() {
        return this.solicitacaoId;
    }

    public StatusSolicitacao getStatus() {
        return this.status;
    }

    public LocalDateTime getPrevisaoRetirada() {
        return this.previsaoRetirada;
    }

    public LocalDateTime getAtualizadaEm() {
        return this.atualizadaEm;
    }
}
