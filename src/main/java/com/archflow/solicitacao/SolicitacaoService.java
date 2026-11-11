package com.archflow.solicitacao;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NaoEncontradoException;
import com.archflow.comum.NegocioException;
import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
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
    public List<SolicitacaoFilaSaida> listarFila(UsuarioAutenticado identidade, String status) {
        this.exigirResponsavel(identidade);
        List<Solicitacao> encontradas = this.procurar(status);
        List<SolicitacaoFilaSaida> fila = new ArrayList<SolicitacaoFilaSaida>();
        for (Solicitacao solicitacao : encontradas) {
            fila.add(new SolicitacaoFilaSaida(solicitacao));
        }
        return fila;
    }

    // CT06: so o responsavel muda o status, pelos caminhos permitidos.
    public SolicitacaoStatusSaida atualizarStatus(UsuarioAutenticado identidade, Long solicitacaoId,
            SolicitacaoStatusEntrada entrada) {
        this.exigirResponsavel(identidade);
        StatusSolicitacao novoStatus = this.converterStatus(entrada.getStatus());
        this.exigirPrevisaoNoFuturo(entrada.getPrevisaoRetirada());
        Solicitacao solicitacao = this.buscar(solicitacaoId);
        this.exigirTransicaoPermitida(solicitacao, novoStatus);
        solicitacao.setStatus(novoStatus);
        this.guardarPrevisao(solicitacao, entrada.getPrevisaoRetirada());
        return new SolicitacaoStatusSaida(this.solicitacaoRepository.save(solicitacao));
    }

    // CT08: o aluno ve so os pedidos dele; o responsavel ve os de todos.
    public List<SolicitacaoHistoricoSaida> listarHistorico(UsuarioAutenticado identidade) {
        this.exigirIdentificado(identidade);
        List<Solicitacao> encontradas = this.procurarHistorico(identidade.getPerfil(),
                identidade.getUsuarioId());
        List<SolicitacaoHistoricoSaida> historico = new ArrayList<SolicitacaoHistoricoSaida>();
        for (Solicitacao solicitacao : encontradas) {
            historico.add(new SolicitacaoHistoricoSaida(solicitacao));
        }
        return historico;
    }

    // O aluno so leva os pedidos dele, pelo numero que veio no token.
    private List<Solicitacao> procurarHistorico(PerfilUsuario perfil, Long usuarioId) {
        if (PerfilUsuario.RESPONSAVEL.equals(perfil)) {
            return this.solicitacaoRepository.findAllByOrderByCriadaEmDesc();
        }
        if (usuarioId == null) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso estar autenticado para ver o histórico.");
        }
        return this.solicitacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId);
    }

    private void exigirIdentificado(UsuarioAutenticado identidade) {
        if (identidade == null) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso estar autenticado para ver o histórico.");
        }
    }

    private Solicitacao buscar(Long solicitacaoId) {
        Solicitacao solicitacao = this.solicitacaoRepository.findBySolicitacaoId(solicitacaoId);
        if (solicitacao == null) {
            throw new NaoEncontradoException(CodigosDeErro.SOLICITACAO_NAO_ENCONTRADA,
                    "O pedido " + solicitacaoId + " não existe.");
        }
        return solicitacao;
    }

    // A mensagem diz de onde para onde o pedido ia.
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

    // Pedido sem previsao mantem a que ja tinha.
    private void guardarPrevisao(Solicitacao solicitacao, LocalDateTime previsaoRetirada) {
        if (previsaoRetirada == null) {
            return;
        }
        solicitacao.setPrevisaoRetirada(previsaoRetirada);
    }

    // A fila e so do responsavel.
    private void exigirResponsavel(UsuarioAutenticado identidade) {
        if (identidade == null) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso estar autenticado para ver a fila.");
        }
        if (!PerfilUsuario.RESPONSAVEL.equals(identidade.getPerfil())) {
            throw new AcessoNegadoException(CodigosDeErro.ACESSO_NEGADO,
                    "Só o responsável vê a fila de atendimento.");
        }
    }

    // Status em branco e a fila do que falta atender, sem os concluidos.
    private List<Solicitacao> procurar(String status) {
        if (status == null || status.trim().isEmpty()) {
            return this.solicitacaoRepository.findByStatusNotOrderByCriadaEmAsc(StatusSolicitacao.CONCLUIDO);
        }
        return this.solicitacaoRepository.findByStatusOrderByCriadaEmAsc(this.converterStatus(status));
    }

    // Texto fora da lista do contrato e recusado, nao vira fila vazia.
    private StatusSolicitacao converterStatus(String status) {
        try {
            return StatusSolicitacao.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException erro) {
            throw new NegocioException(CodigosDeErro.STATUS_INVALIDO,
                    "O status \"" + status + "\" não existe.");
        }
    }
}
