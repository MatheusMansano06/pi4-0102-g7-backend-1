package com.archflow.solicitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NegocioException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

// Confere as recusas do CT04 e do CT06. O repositorio entra como null de
// proposito: em todos estes casos o metodo recusa antes de chegar ao banco, e
// um teste que nao toca no banco roda na CI em qualquer maquina.
public class SolicitacaoServiceTest {

    private SolicitacaoService servico() {
        return new SolicitacaoService(null);
    }

    @Test
    public void semPerfilAFilaRecusaPorFaltaDeAutenticacao() {
        try {
            this.servico().listarFila(null, null);
            fail("A fila nao podia ter respondido a quem nao se identificou.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void alunoNaoVeAFila() {
        try {
            this.servico().listarFila("ALUNO", null);
            fail("A fila nao podia ter respondido a um aluno.");
        } catch (AcessoNegadoException erro) {
            assertEquals(CodigosDeErro.ACESSO_NEGADO, erro.getCodigo());
        }
    }

    @Test
    public void statusForaDaListaDoContratoERecusado() {
        try {
            this.servico().listarFila("RESPONSAVEL", "ENTREGUE");
            fail("O status ENTREGUE nao existe e nao podia ter sido aceito.");
        } catch (NegocioException erro) {
            assertEquals(CodigosDeErro.STATUS_INVALIDO, erro.getCodigo());
        }
    }

    private SolicitacaoStatusEntrada pedidoDeMudanca(String status, LocalDateTime previsao) {
        SolicitacaoStatusEntrada entrada = new SolicitacaoStatusEntrada();
        entrada.setStatus(status);
        entrada.setPrevisaoRetirada(previsao);
        return entrada;
    }

    @Test
    public void semPerfilAMudancaDeStatusRecusaPorFaltaDeAutenticacao() {
        try {
            this.servico().atualizarStatus(null, 1L, this.pedidoDeMudanca("EM_SEPARACAO", null));
            fail("O status nao podia ter mudado para quem nao se identificou.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void alunoNaoMudaOStatusDoPedido() {
        try {
            this.servico().atualizarStatus("ALUNO", 1L, this.pedidoDeMudanca("EM_SEPARACAO", null));
            fail("O aluno nao podia ter mudado o status de um pedido.");
        } catch (AcessoNegadoException erro) {
            assertEquals(CodigosDeErro.ACESSO_NEGADO, erro.getCodigo());
        }
    }

    @Test
    public void statusForaDaListaDoContratoNaoMudaNada() {
        try {
            this.servico().atualizarStatus("RESPONSAVEL", 1L, this.pedidoDeMudanca("ENTREGUE", null));
            fail("O status ENTREGUE nao existe e nao podia ter sido aceito.");
        } catch (NegocioException erro) {
            assertEquals(CodigosDeErro.STATUS_INVALIDO, erro.getCodigo());
        }
    }

    @Test
    public void semPerfilOHistoricoRecusaPorFaltaDeAutenticacao() {
        try {
            this.servico().listarHistorico(null, 1L);
            fail("O historico nao podia ter respondido a quem nao se identificou.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void perfilForaDaListaDoContratoNaoVeOHistorico() {
        try {
            this.servico().listarHistorico("COORDENADOR", 1L);
            fail("Um perfil que o sistema nao conhece nao podia ter visto o historico.");
        } catch (AcessoNegadoException erro) {
            assertEquals(CodigosDeErro.ACESSO_NEGADO, erro.getCodigo());
        }
    }

    @Test
    public void alunoSemNumeroNaoVeOHistoricoDaTurma() {
        try {
            this.servico().listarHistorico("ALUNO", null);
            fail("Sem saber qual aluno pediu, o historico nao podia ter sido montado.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void previsaoDeRetiradaNoPassadoERecusada() {
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);
        try {
            this.servico().atualizarStatus("RESPONSAVEL", 1L,
                    this.pedidoDeMudanca("PRONTO_PARA_RETIRADA", ontem));
            fail("Uma previsao de retirada no passado nao podia ter sido aceita.");
        } catch (NegocioException erro) {
            assertEquals(CodigosDeErro.PREVISAO_INVALIDA, erro.getCodigo());
        }
    }
}
