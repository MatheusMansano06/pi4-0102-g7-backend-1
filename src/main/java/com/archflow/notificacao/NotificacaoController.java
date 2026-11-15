package com.archflow.notificacao;

import com.archflow.comum.UsuarioAutenticado;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NegocioException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(
            NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    // GET /api/notificacoes
    @GetMapping
    public NotificacoesSaida listar(

            @RequestAttribute(
                    name = "usuarioAutenticado",
                    required = false)
            UsuarioAutenticado identidade,

            @RequestParam(name = "somenteNaoLidas", required = false)
            String somenteNaoLidas) {

        return notificacaoService.listar(
                identidade,
                this.lerSomenteNaoLidas(somenteNaoLidas)
        );
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
