package com.archflow.material;

import java.util.ArrayList;
import java.util.List;

// O que o CT02 devolve por material.
public class MaterialSaida {

    private Long materialId;

    private String nome;

    private String descricao;

    private String especificacao;

    private int quantidadeDisponivel;

    private StatusEstoque statusEstoque;

    private List<String> parametrosAceitos;

    private List<String> parametrosObrigatorios;

    public MaterialSaida(Material material) {
        this.materialId = material.getMaterialId();
        this.nome = material.getNome();
        this.descricao = material.getDescricao();
        this.especificacao = material.getEspecificacao();
        this.quantidadeDisponivel = material.getQuantidadeDisponivel();
        this.statusEstoque = material.statusEstoque();
        if (this.configuracaoValida(material)) {
            this.parametrosAceitos = this.parametros(material.getAceitaDimensao(), material.getAceitaDiametro());
            this.parametrosObrigatorios = this.parametros(material.getExigeDimensao(), material.getExigeDiametro());
        }
    }

    public Long getMaterialId() {
        return this.materialId;
    }

    public String getNome() {
        return this.nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public String getEspecificacao() {
        return this.especificacao;
    }

    public int getQuantidadeDisponivel() {
        return this.quantidadeDisponivel;
    }

    public StatusEstoque getStatusEstoque() {
        return this.statusEstoque;
    }

    public List<String> getParametrosAceitos() {
        return this.parametrosAceitos;
    }

    public List<String> getParametrosObrigatorios() {
        return this.parametrosObrigatorios;
    }

    private boolean configuracaoValida(Material material) {
        if (material.getAceitaDimensao() == null || material.getExigeDimensao() == null
                || material.getAceitaDiametro() == null || material.getExigeDiametro() == null) {
            return false;
        }
        return (!material.getExigeDimensao() || material.getAceitaDimensao())
                && (!material.getExigeDiametro() || material.getAceitaDiametro());
    }

    private List<String> parametros(boolean dimensao, boolean diametro) {
        List<String> lista = new ArrayList<String>();
        if (dimensao) {
            lista.add("DIMENSAO");
        }
        if (diametro) {
            lista.add("DIAMETRO");
        }
        return lista;
    }
}
