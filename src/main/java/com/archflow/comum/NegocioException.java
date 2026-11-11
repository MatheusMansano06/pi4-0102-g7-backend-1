package com.archflow.comum;

// Erro de regra de negocio. Alem da mensagem, carrega o codigo que os
// contratos definem, como QUANTIDADE_INVALIDA.
public class NegocioException extends RuntimeException {

    private String codigo;

    public NegocioException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return this.codigo;
    }
}
