package com.archflow.notificacao;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NegocioException;
import com.archflow.comum.UsuarioAutenticado;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "archflow.notificacoes.em-desenvolvimento", havingValue = "true")
public class NotificacaoService {

    private final ConsultaDeNotificacoes consulta;

    public NotificacaoService(ConsultaDeNotificacoes consulta) {
        this.consulta = consulta;
    }

    public NotificacoesSaida listarNotificacoes(UsuarioAutenticado identidade, String somenteNaoLidas) {
        // 1) O destinatário vem da identidade validada, qualquer que seja o perfil.
        if (identidade == null) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso estar autenticado para ver as notificações.");
        }
        // 2) A consulta CT13 recebe o destinatário e o filtro já validados.
        boolean filtrarNaoLidas = this.lerSomenteNaoLidas(somenteNaoLidas);
        return new NotificacoesSaida(this.consulta.listarDoUsuario(identidade.getUsuarioId(), filtrarNaoLidas));
    }

    private boolean lerSomenteNaoLidas(String valor) {
        if (valor == null || "false".equals(valor)) {
            return false;
        }
        if ("true".equals(valor)) {
            return true;
        }
        throw new NegocioException(CodigosDeErro.DADOS_INVALIDOS,
                "Informe true ou false em somenteNaoLidas.");
    }
}
