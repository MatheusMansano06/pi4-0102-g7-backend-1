package com.archflow.solicitacao;

import com.archflow.comum.UsuarioAutenticado;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
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
    @GetMapping("/fila")
    public List<SolicitacaoFilaSaida> listarFila(
            @RequestAttribute(name = "usuarioAutenticado", required = false) UsuarioAutenticado identidade,
            @RequestParam(required = false) String status) {
        return this.solicitacaoService.listarFila(identidade, status);
    }

    // CT08: GET /api/solicitacoes/historico
    @GetMapping("/historico")
    public List<SolicitacaoHistoricoSaida> listarHistorico(
            @RequestAttribute(name = "usuarioAutenticado", required = false) UsuarioAutenticado identidade) {
        return this.solicitacaoService.listarHistorico(identidade);
    }

    // CT06: PATCH /api/solicitacoes/{solicitacaoId}/status
    @PatchMapping("/{solicitacaoId}/status")
    public SolicitacaoStatusSaida atualizarStatus(
            @RequestAttribute(name = "usuarioAutenticado", required = false) UsuarioAutenticado identidade,
            @PathVariable Long solicitacaoId,
            @RequestBody SolicitacaoStatusEntrada entrada) {
        return this.solicitacaoService.atualizarStatus(identidade, solicitacaoId, entrada);
    }
}
