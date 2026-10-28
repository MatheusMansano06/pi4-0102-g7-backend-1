package com.archflow.comum;

// Quem chamou esta autenticado, mas o perfil dele nao permite a operacao.
// Vira HTTP 403.
public class AcessoNegadoException extends NegocioException {

    public AcessoNegadoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
