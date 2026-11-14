package com.archflow.notificacao;

import com.archflow.comum.FiltroDeIdentidade;
import com.archflow.comum.UsuarioAutenticado;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// A rota fica desativada até haver uma consulta CT13 ligada ao banco.
@RestController
@ConditionalOnProperty(name = "archflow.notificacoes.em-desenvolvimento", havingValue = "true")
public class NotificacaoController {

    private final NotificacaoService servico;

    public NotificacaoController(NotificacaoService servico) {
        this.servico = servico;
    }

    // CT07: GET /api/notificacoes
    @GetMapping("/api/notificacoes")
    public NotificacoesSaida listarNotificacoes(
            @RequestAttribute(name = FiltroDeIdentidade.ATRIBUTO_IDENTIDADE, required = false)
            UsuarioAutenticado identidade,
            @RequestParam(name = "somenteNaoLidas", required = false) String somenteNaoLidas) {
        return this.servico.listarNotificacoes(identidade, somenteNaoLidas);
    }
}
