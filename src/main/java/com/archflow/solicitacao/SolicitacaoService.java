package com.archflow.solicitacao;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NaoEncontradoException;
import com.archflow.comum.NegocioException;
import com.archflow.comum.PerfilUsuario;
import java.time.LocalDateTime;
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

    // CT06: so o responsavel muda o status, e so pelos caminhos que a tabela
    // de transicoes permite.
    public SolicitacaoStatusSaida atualizarStatus(String perfil, Long solicitacaoId,
            SolicitacaoStatusEntrada entrada) {
        // 1) Quem nao e o responsavel nao passa daqui.
        this.exigirResponsavel(perfil);
        // 2) O status pedido tem que ser um dos nomes do contrato.
        StatusSolicitacao novoStatus = this.converterStatus(entrada.getStatus());
        // 3) Previsao no passado nao serve para avisar o aluno.
        this.exigirPrevisaoNoFuturo(entrada.getPrevisaoRetirada());
        // 4) O pedido tem que existir e aceitar esse caminho.
        Solicitacao solicitacao = this.buscar(solicitacaoId);
        this.exigirTransicaoPermitida(solicitacao, novoStatus);
        // 5) A entidade marca a hora da mudanca sozinha quando o status troca.
        solicitacao.setStatus(novoStatus);
        this.guardarPrevisao(solicitacao, entrada.getPrevisaoRetirada());
        return new SolicitacaoStatusSaida(this.solicitacaoRepository.save(solicitacao));
    }

    private Solicitacao buscar(Long solicitacaoId) {
        Solicitacao solicitacao = this.solicitacaoRepository.findBySolicitacaoId(solicitacaoId);
        if (solicitacao == null) {
            throw new NaoEncontradoException(CodigosDeErro.SOLICITACAO_NAO_ENCONTRADA,
                    "O pedido " + solicitacaoId + " não existe.");
        }
        return solicitacao;
    }

    // A mensagem diz de onde para onde o pedido ia, porque o responsavel ve a
    // fila inteira e precisa saber qual pedido recusou a mudanca.
    private void exigirTransicaoPermitida(Solicitacao solicitacao, StatusSolicitacao novoStatus) {
        if (solicitacao.podeIrPara(novoStatus)) {
            return;
        }
        throw new NegocioException(CodigosDeErro.STATUS_INVALIDO,
                "Um pedido " + solicitacao.getStatus() + " não pode ir para " + novoStatus + ".");
    }

    // A previsao e opcional no contrato: sem ela a mudanca de status vale.
    private void exigirPrevisaoNoFuturo(LocalDateTime previsaoRetirada) {
        if (previsaoRetirada == null) {
            return;
        }
        if (previsaoRetirada.isBefore(LocalDateTime.now())) {
            throw new NegocioException(CodigosDeErro.PREVISAO_INVALIDA,
                    "A previsão de retirada não pode ser no passado.");
        }
    }

    // Pedido que chega sem previsao mantem a que ja tinha, para a mudanca de
    // status nao apagar a data que o aluno ja viu no painel de prazos.
    private void guardarPrevisao(Solicitacao solicitacao, LocalDateTime previsaoRetirada) {
        if (previsaoRetirada == null) {
            return;
        }
        solicitacao.setPrevisaoRetirada(previsaoRetirada);
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
