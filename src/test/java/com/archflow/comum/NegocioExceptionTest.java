package com.archflow.comum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// Confere que as excecoes guardam o codigo do contrato e que a familia
// de classes esta montada certo. Nao precisa de banco nem do Spring.
public class NegocioExceptionTest {

    @Test
    public void guardaOCodigoEAMensagem() {
        NegocioException erro = new NegocioException(
                CodigosDeErro.QUANTIDADE_INVALIDA, "A quantidade deve ser maior que zero.");

        assertEquals("QUANTIDADE_INVALIDA", erro.getCodigo());
        assertEquals("A quantidade deve ser maior que zero.", erro.getMessage());
    }

    @Test
    public void asExcecoesEspecificasSaoTiposDeNegocioException() {
        // O TratadorDeErros depende disso: ele trata o caso especifico primeiro
        // e cai no NegocioException quando nao ha um mais especifico.
        NaoEncontradoException naoEncontrado = new NaoEncontradoException(
                CodigosDeErro.MATERIAL_NAO_ENCONTRADO, "Material nao encontrado.");
        NaoAutenticadoException naoAutenticado = new NaoAutenticadoException(
                CodigosDeErro.NAO_AUTENTICADO, "Token ausente ou invalido.");
        AcessoNegadoException acessoNegado = new AcessoNegadoException(
                CodigosDeErro.ACESSO_NEGADO, "Somente o responsavel pode fazer isso.");

        assertTrue(naoEncontrado instanceof NegocioException);
        assertTrue(naoAutenticado instanceof NegocioException);
        assertTrue(acessoNegado instanceof NegocioException);
    }

    @Test
    public void naoObrigaOChamadorATratarComTryCatch() {
        // Herdar de RuntimeException e o que permite lancar a excecao sem
        // declarar "throws" em cada metodo do caminho.
        NegocioException erro = new NegocioException(CodigosDeErro.STATUS_INVALIDO, "Transicao invalida.");

        assertTrue(erro instanceof RuntimeException);
    }
}
