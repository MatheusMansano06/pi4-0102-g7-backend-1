package com.archflow.solicitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NegocioException;
import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

// Confere as recusas do CT04 e do CT06. O repositorio entra como null de
// proposito: em todos estes casos o metodo recusa antes de chegar ao banco, e
// um teste que nao toca no banco roda na CI em qualquer maquina.
public class SolicitacaoServiceTest {

    private SolicitacaoService servico() {
        return new SolicitacaoService(null);
    }

    // A identidade chega pronta do token conferido no FiltroDeIdentidade: aqui
    // ela e montada a mao so para exercitar a regra de cada operacao.
    private UsuarioAutenticado aluno() {
        return new UsuarioAutenticado(1L, PerfilUsuario.ALUNO);
    }

    private UsuarioAutenticado responsavel() {
        return new UsuarioAutenticado(9L, PerfilUsuario.RESPONSAVEL);
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
            this.servico().listarFila(this.aluno(), null);
            fail("A fila nao podia ter respondido a um aluno.");
        } catch (AcessoNegadoException erro) {
            assertEquals(CodigosDeErro.ACESSO_NEGADO, erro.getCodigo());
        }
    }

    @Test
    public void statusForaDaListaDoContratoERecusado() {
        try {
            this.servico().listarFila(this.responsavel(), "ENTREGUE");
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
            this.servico().atualizarStatus(this.aluno(), 1L, this.pedidoDeMudanca("EM_SEPARACAO", null));
            fail("O aluno nao podia ter mudado o status de um pedido.");
        } catch (AcessoNegadoException erro) {
            assertEquals(CodigosDeErro.ACESSO_NEGADO, erro.getCodigo());
        }
    }

    @Test
    public void statusForaDaListaDoContratoNaoMudaNada() {
        try {
            this.servico().atualizarStatus(this.responsavel(), 1L, this.pedidoDeMudanca("ENTREGUE", null));
            fail("O status ENTREGUE nao existe e nao podia ter sido aceito.");
        } catch (NegocioException erro) {
            assertEquals(CodigosDeErro.STATUS_INVALIDO, erro.getCodigo());
        }
    }

    @Test
    public void semPerfilOHistoricoRecusaPorFaltaDeAutenticacao() {
        try {
            this.servico().listarHistorico(null);
            fail("O historico nao podia ter respondido a quem nao se identificou.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void previsaoDeRetiradaNoPassadoERecusada() {
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);
        try {
            this.servico().atualizarStatus(this.responsavel(), 1L,
                    this.pedidoDeMudanca("PRONTO_PARA_RETIRADA", ontem));
            fail("Uma previsao de retirada no passado nao podia ter sido aceita.");
        } catch (NegocioException erro) {
            assertEquals(CodigosDeErro.PREVISAO_INVALIDA, erro.getCodigo());
        }
    }
}
