package com.archflow.solicitacao;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    // CT04: GET /api/solicitacoes/fila?status={status}
    //
    // Provisorio: o perfil chega no cabecalho X-Perfil enquanto o H02.2 nao
    // confere o token na tabela sessoes (ADR 0003). Quando conferir, o perfil
    // sai da sessao e este cabecalho deixa de existir.
    @GetMapping("/fila")
    public List<SolicitacaoFilaSaida> listarFila(
            @RequestHeader(name = "X-Perfil", required = false) String perfil,
            @RequestParam(required = false) String status) {
        return this.solicitacaoService.listarFila(perfil, status);
    }
}
