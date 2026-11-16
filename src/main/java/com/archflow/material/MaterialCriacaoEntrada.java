package com.archflow.material;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NegocioException;

public class MaterialCriacaoEntrada {

    private String nome;
    private String descricao;
    private String especificacao;
    private Integer quantidadeDisponivel;
    private Integer quantidadeMinima;

    public MaterialCriacaoEntrada() {
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEspecificacao() {
        return this.especificacao;
    }

    public void setEspecificacao(String especificacao) {
        this.especificacao = especificacao;
    }

    public Integer getQuantidadeDisponivel() {
        return this.quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(Object quantidadeDisponivel) {
        this.quantidadeDisponivel = this.lerInteiro(quantidadeDisponivel);
    }

    public Integer getQuantidadeMinima() {
        return this.quantidadeMinima;
    }

    public void setQuantidadeMinima(Object quantidadeMinima) {
        this.quantidadeMinima = this.lerInteiro(quantidadeMinima);
    }

    private Integer lerInteiro(Object valor) {
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof Integer)) {
            throw new NegocioException(CodigosDeErro.QUANTIDADE_INVALIDA,
                    "As quantidades devem ser números inteiros.");
        }
        return (Integer) valor;
    }
}
