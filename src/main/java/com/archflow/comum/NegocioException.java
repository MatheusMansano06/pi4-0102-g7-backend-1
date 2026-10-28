package com.archflow.comum;

// Erro de regra de negocio. Alem da mensagem, carrega o codigo que os
// contratos definem, como QUANTIDADE_INVALIDA.
//
// Herda de RuntimeException de proposito: assim os metodos nao precisam
// declarar "throws" e o Spring desfaz a transacao sozinho quando ela estoura.
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
