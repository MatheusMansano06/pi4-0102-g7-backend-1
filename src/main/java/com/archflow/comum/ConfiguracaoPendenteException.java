package com.archflow.comum;

public class ConfiguracaoPendenteException extends NegocioException {

    public ConfiguracaoPendenteException(String mensagem) {
        super(CodigosDeErro.CONFIGURACAO_PENDENTE, mensagem);
    }
}
