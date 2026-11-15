package com.archflow.notificacao;

import com.archflow.comum.UsuarioAutenticado;

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

            @RequestParam(defaultValue = "false")
            boolean somenteNaoLidas) {

        return notificacaoService.listar(
                identidade,
                somenteNaoLidas
        );
    }
}