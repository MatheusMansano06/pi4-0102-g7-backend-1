package com.archflow.comum;

// Quem chamou nao mandou token, ou o token nao vale mais. Vira HTTP 401.
public class NaoAutenticadoException extends NegocioException {

    public NaoAutenticadoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
