package com.archflow.material;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NegocioException;

public class MaterialEstoqueEntrada {

    private Integer quantidadeDisponivel;

    public MaterialEstoqueEntrada() {
    }

    public Integer getQuantidadeDisponivel() {
        return this.quantidadeDisponivel;
    }

    // Object preserva o tipo recebido no JSON: 2.5 e "2" não podem virar 2.
    // A validação de presença e de valor não negativo permanece no Service.
    public void setQuantidadeDisponivel(Object quantidadeDisponivel) {
        if (quantidadeDisponivel == null) {
            this.quantidadeDisponivel = null;
            return;
        }
        if (!(quantidadeDisponivel instanceof Integer)) {
            throw new NegocioException(CodigosDeErro.QUANTIDADE_INVALIDA,
                    "A quantidade disponível deve ser um número inteiro.");
        }
        this.quantidadeDisponivel = (Integer) quantidadeDisponivel;
    }
}
