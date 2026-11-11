package com.archflow.comum;

// O corpo de toda resposta de erro da API, conforme o ADR 0004.
public class ErroSaida {

    private String codigo;
    private String mensagem;

    public ErroSaida(String codigo, String mensagem) {
        this.codigo = codigo;
        this.mensagem = mensagem;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public String getMensagem() {
        return this.mensagem;
    }
}
