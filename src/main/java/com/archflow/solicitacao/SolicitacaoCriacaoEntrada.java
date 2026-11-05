package com.archflow.solicitacao;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NegocioException;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class SolicitacaoCriacaoEntrada {

    private Long materialId;

    private Integer quantidade;

    private String dimensao;

    private Double diametro;

    private String observacao;

    public SolicitacaoCriacaoEntrada() {
    }

    public Long getMaterialId() {
        return this.materialId;
    }

    public void setMaterialId(Object materialId) {
        if (materialId == null) {
            this.materialId = null;
            return;
        }
        if (!(materialId instanceof Integer) && !(materialId instanceof Long)) {
            throw new NegocioException(CodigosDeErro.DADOS_INVALIDOS,
                    "O materialId deve ser um número inteiro.");
        }
        this.materialId = ((Number) materialId).longValue();
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Object quantidade) {
        if (quantidade == null) {
            this.quantidade = null;
            return;
        }
        if (!(quantidade instanceof Integer)) {
            throw new NegocioException(CodigosDeErro.QUANTIDADE_INVALIDA,
                    "A quantidade deve ser um número inteiro.");
        }
        this.quantidade = (Integer) quantidade;
    }

    public String getDimensao() {
        return this.dimensao;
    }

    public void setDimensao(Object dimensao) {
        this.dimensao = this.textoOpcional(dimensao, "dimensao");
    }

    public Double getDiametro() {
        return this.diametro;
    }

    public void setDiametro(Object diametro) {
        if (diametro == null) {
            this.diametro = null;
            return;
        }
        if (!(diametro instanceof Number)) {
            throw new NegocioException(CodigosDeErro.PARAMETROS_INVALIDOS,
                    "O diâmetro deve ser um número, sem unidade no valor JSON.");
        }
        double valor = ((Number) diametro).doubleValue();
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new NegocioException(CodigosDeErro.PARAMETROS_INVALIDOS,
                    "O diâmetro deve ser positivo e finito.");
        }
        this.diametro = valor;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(Object observacao) {
        this.observacao = this.textoOpcional(observacao, "observacao");
    }

    private String textoOpcional(Object valor, String campo) {
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof String)) {
            throw new NegocioException(CodigosDeErro.DADOS_INVALIDOS,
                    "O campo " + campo + " deve ser texto.");
        }
        return (String) valor;
    }

    @JsonAnySetter
    public void recusarCampoDesconhecido(String campo, Object valor) {
        throw new NegocioException(CodigosDeErro.DADOS_INVALIDOS,
                "Campo não definido na criação da solicitação: " + campo + ".");
    }
}
