package com.archflow.notificacao;

import java.time.LocalDateTime;

// Projeção do CT07: o destinatário é usado na consulta, não aparece na saída.
public class NotificacaoSaida {

    private final Long notificacaoId;
    private final String tipo;
    private final String mensagem;
    private final LocalDateTime criadaEm;
    private final boolean lida;
    private final Long solicitacaoId;

    public NotificacaoSaida(Long notificacaoId, String tipo, String mensagem,
            LocalDateTime criadaEm, boolean lida, Long solicitacaoId) {
        this.notificacaoId = notificacaoId;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.criadaEm = criadaEm;
        this.lida = lida;
        this.solicitacaoId = solicitacaoId;
    }

    public Long getNotificacaoId() {
        return this.notificacaoId;
    }

    public String getTipo() {
        return this.tipo;
    }

    public String getMensagem() {
        return this.mensagem;
    }

    public LocalDateTime getCriadaEm() {
        return this.criadaEm;
    }

    public boolean isLida() {
        return this.lida;
    }

    public Long getSolicitacaoId() {
        return this.solicitacaoId;
    }
}
