package com.archflow.solicitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NegocioException;
import org.junit.jupiter.api.Test;

// Confere as recusas do CT04. O repositorio entra como null de proposito:
// nos tres casos o metodo recusa antes de chegar ao banco, e um teste que
// nao toca no banco roda na CI em qualquer maquina.
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
}
