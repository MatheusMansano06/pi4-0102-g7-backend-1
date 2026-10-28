package com.archflow.comum;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Captura as excecoes lancadas nas outras camadas e transforma cada uma na
// resposta HTTP certa, conforme a tabela do ADR 0004.
//
// Sem esta classe, qualquer regra de negocio violada sairia como HTTP 500
// ("o servidor quebrou") e a mensagem nem chegaria ao frontend.
//
// O Spring escolhe sempre o metodo mais especifico. Por isso o de
// NegocioException, que e o pai de todos, fica por ultimo.
@RestControllerAdvice
public class TratadorDeErros {

    // 404: o recurso pedido nao existe.
    @ExceptionHandler(NaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroSaida tratarNaoEncontrado(NaoEncontradoException erro) {
        return new ErroSaida(erro.getCodigo(), erro.getMessage());
    }

    // 401: nao esta autenticado.
    @ExceptionHandler(NaoAutenticadoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErroSaida tratarNaoAutenticado(NaoAutenticadoException erro) {
        return new ErroSaida(erro.getCodigo(), erro.getMessage());
    }

    // 403: esta autenticado, mas o perfil nao permite.
    @ExceptionHandler(AcessoNegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErroSaida tratarAcessoNegado(AcessoNegadoException erro) {
        return new ErroSaida(erro.getCodigo(), erro.getMessage());
    }

    // 400: a entidade recusou o valor recebido.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroSaida tratarValorInvalido(IllegalArgumentException erro) {
        return new ErroSaida(CodigosDeErro.PARAMETROS_INVALIDOS, erro.getMessage());
    }

    // 400: qualquer outro erro de regra de negocio.
    @ExceptionHandler(NegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroSaida tratarRegraDeNegocio(NegocioException erro) {
        return new ErroSaida(erro.getCodigo(), erro.getMessage());
    }

    // 500: falha que nao esperavamos. A mensagem original nao vai para o
    // cliente de proposito, para nao vazar detalhe interno do sistema.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroSaida tratarFalhaInesperada(Exception erro) {
        return new ErroSaida(CodigosDeErro.ERRO_INTERNO, "Erro interno no servidor.");
    }
}
