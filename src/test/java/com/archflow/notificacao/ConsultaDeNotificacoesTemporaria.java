package com.archflow.notificacao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

// Consulta de referência para a prova. A tabela só existe na sessão de teste.
class ConsultaDeNotificacoesTemporaria implements ConsultaDeNotificacoes {

    private static final String CONSULTA_DO_DESTINATARIO =
            "SELECT notificacao_id, tipo, mensagem, criada_em, lida, solicitacao_id"
                    + " FROM pg_temp.avisos_ct07_teste WHERE usuario_id = ?";

    private final JdbcTemplate sql;

    ConsultaDeNotificacoesTemporaria(JdbcTemplate sql) {
        this.sql = sql;
    }

    @Override
    public List<NotificacaoSaida> listarDoUsuario(Long usuarioId, boolean somenteNaoLidas) {
        String consulta = CONSULTA_DO_DESTINATARIO;
        if (somenteNaoLidas) {
            consulta = consulta + " AND lida = FALSE";
        }
        return this.sql.query(consulta, new CamposDoAviso(), usuarioId);
    }

    private static class CamposDoAviso implements RowMapper<NotificacaoSaida> {

        @Override
        public NotificacaoSaida mapRow(ResultSet linha, int numero) throws SQLException {
            return new NotificacaoSaida(linha.getLong("notificacao_id"), linha.getString("tipo"),
                    linha.getString("mensagem"), linha.getObject("criada_em", LocalDateTime.class),
                    linha.getBoolean("lida"), linha.getObject("solicitacao_id", Long.class));
        }
    }
}
