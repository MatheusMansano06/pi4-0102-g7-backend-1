package com.archflow.comum;

// O corpo que toda resposta de erro da API devolve, conforme o ADR 0004:
//   { "codigo": "QUANTIDADE_INVALIDA", "mensagem": "A quantidade deve ser maior que zero." }
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
