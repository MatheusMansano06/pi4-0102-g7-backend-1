package com.archflow.notificacao;

import java.util.List;

public interface ConsultaDeNotificacoes {

    // CT13: restringir a consulta ao destinatário e aplicar o filtro na leitura.
    // Sem resultados, devolver uma lista vazia.
    List<NotificacaoSaida> listarDoUsuario(Long usuarioId, boolean somenteNaoLidas);
}
