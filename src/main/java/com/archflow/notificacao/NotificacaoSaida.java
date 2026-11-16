package com.archflow.notificacao;

import java.time.LocalDateTime;

public class NotificacaoSaida {

    private Long notificacaoId;
    private Long solicitacaoId;
    private String tipo;
    private String mensagem;
    private LocalDateTime criadaEm;
    private boolean lida;

    public NotificacaoSaida(Notificacao notificacao) {
        this.notificacaoId = notificacao.getNotificacaoId();
        this.solicitacaoId = notificacao.getSolicitacaoId();
        this.tipo = notificacao.getTipo();
        this.mensagem = notificacao.getMensagem();
        this.criadaEm = notificacao.getCriadoEm();
        this.lida = notificacao.isLida();
    }

    public Long getNotificacaoId() {
        return notificacaoId;
    }

    public Long getSolicitacaoId() {
        return solicitacaoId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public boolean isLida() {
        return lida;
    }
}