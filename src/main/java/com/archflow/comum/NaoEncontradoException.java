package com.archflow.comum;

// O material, a solicitacao ou o usuario pedido nao existe. Vira HTTP 404.
public class NaoEncontradoException extends NegocioException {

    public NaoEncontradoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
