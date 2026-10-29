package com.archflow.solicitacao;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NegocioException;
import com.archflow.comum.PerfilUsuario;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    // CT04: a fila e so do responsavel, em ordem de chegada.
    public List<SolicitacaoFilaSaida> listarFila(String perfil, String status) {
        // 1) Quem nao e o responsavel nao passa daqui.
        this.exigirResponsavel(perfil);
        // 2) Sem status no pedido da tela, a fila volta sem os pedidos concluidos.
        List<Solicitacao> encontradas = this.procurar(status);
        // 3) A tela recebe so os campos do contrato, nunca a entidade.
        List<SolicitacaoFilaSaida> fila = new ArrayList<SolicitacaoFilaSaida>();
        for (Solicitacao solicitacao : encontradas) {
            fila.add(new SolicitacaoFilaSaida(solicitacao));
        }
        return fila;
    }

    // Perfil em branco significa que ninguem se identificou. O aluno se
    // identificou, mas a fila dos outros nao e da conta dele.
    private void exigirResponsavel(String perfil) {
        if (perfil == null || perfil.trim().isEmpty()) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso estar autenticado para ver a fila.");
        }
        if (!PerfilUsuario.RESPONSAVEL.name().equals(perfil.trim().toUpperCase())) {
            throw new AcessoNegadoException(CodigosDeErro.ACESSO_NEGADO,
                    "Só o responsável vê a fila de atendimento.");
        }
    }

    // Status em branco nao e erro: significa a fila do que ainda falta atender,
    // por isso o pedido concluido fica de fora enquanto ninguem pedir por ele.
    private List<Solicitacao> procurar(String status) {
        if (status == null || status.trim().isEmpty()) {
            return this.solicitacaoRepository.findByStatusNotOrderByCriadaEmAsc(StatusSolicitacao.CONCLUIDO);
        }
        return this.solicitacaoRepository.findByStatusOrderByCriadaEmAsc(this.converterStatus(status));
    }

    // O contrato aceita so os status da lista. Qualquer outro texto e recusado,
    // em vez de devolver fila vazia e fazer o responsavel achar que nao ha pedido.
    private StatusSolicitacao converterStatus(String status) {
        try {
            return StatusSolicitacao.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException erro) {
            throw new NegocioException(CodigosDeErro.STATUS_INVALIDO,
                    "O status \"" + status + "\" não existe.");
        }
    }
}
