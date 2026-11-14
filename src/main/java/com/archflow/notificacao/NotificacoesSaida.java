package com.archflow.notificacao;

import java.util.List;

public class NotificacoesSaida {

    private final List<NotificacaoSaida> notificacoes;

    public NotificacoesSaida(List<NotificacaoSaida> notificacoes) {
        this.notificacoes = notificacoes;
    }

    public List<NotificacaoSaida> getNotificacoes() {
        return this.notificacoes;
    }
}
