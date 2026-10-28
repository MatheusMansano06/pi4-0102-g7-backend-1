package com.archflow.comum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

// Confere o que o ADR 0004 promete: toda resposta de erro sai no formato
// { codigo, mensagem }, com o codigo que o contrato define.
public class TratadorDeErrosTest {

    @Test
    public void naoEncontradoDevolveOCodigoDoContrato() {
        TratadorDeErros tratador = new TratadorDeErros();
        NaoEncontradoException erro = new NaoEncontradoException(
                CodigosDeErro.MATERIAL_NAO_ENCONTRADO, "Material nao encontrado.");

        ErroSaida saida = tratador.tratarNaoEncontrado(erro);

        assertEquals("MATERIAL_NAO_ENCONTRADO", saida.getCodigo());
        assertEquals("Material nao encontrado.", saida.getMensagem());
    }

    @Test
    public void naoAutenticadoDevolveOCodigoDoContrato() {
        TratadorDeErros tratador = new TratadorDeErros();
        NaoAutenticadoException erro = new NaoAutenticadoException(
                CodigosDeErro.NAO_AUTENTICADO, "Token ausente ou invalido.");

        ErroSaida saida = tratador.tratarNaoAutenticado(erro);

        assertEquals("NAO_AUTENTICADO", saida.getCodigo());
    }

    @Test
    public void acessoNegadoDevolveOCodigoDoContrato() {
        TratadorDeErros tratador = new TratadorDeErros();
        AcessoNegadoException erro = new AcessoNegadoException(
                CodigosDeErro.ACESSO_NEGADO, "Somente o responsavel pode fazer isso.");

        ErroSaida saida = tratador.tratarAcessoNegado(erro);

        assertEquals("ACESSO_NEGADO", saida.getCodigo());
    }

    @Test
    public void valorRecusadoPelaEntidadeViraParametrosInvalidos() {
        // As entidades lancam IllegalArgumentException nos setters. O tratador
        // traduz isso para o codigo que o contrato conhece.
        TratadorDeErros tratador = new TratadorDeErros();
        IllegalArgumentException erro = new IllegalArgumentException("O nome nao pode ser null nem vazio.");

        ErroSaida saida = tratador.tratarValorInvalido(erro);

        assertEquals("PARAMETROS_INVALIDOS", saida.getCodigo());
        assertEquals("O nome nao pode ser null nem vazio.", saida.getMensagem());
    }

    @Test
    public void falhaInesperadaNaoVazaDetalheInterno() {
        // A mensagem original pode conter nome de tabela, caminho de arquivo
        // ou senha. O cliente recebe so ERRO_INTERNO.
        TratadorDeErros tratador = new TratadorDeErros();
        Exception erro = new Exception("conexao recusada em jdbc:postgresql://banco:5432/archflow");

        ErroSaida saida = tratador.tratarFalhaInesperada(erro);

        assertEquals("ERRO_INTERNO", saida.getCodigo());
        assertEquals("Erro interno no servidor.", saida.getMensagem());
        assertNotEquals(erro.getMessage(), saida.getMensagem());
    }
}
