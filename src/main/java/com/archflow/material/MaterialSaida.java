package com.archflow.material;

// O que o CT02 devolve por material. Nao devolvemos a entidade para que o
// JSON nao carregue campo nenhum que o contrato nao previu.
public class MaterialSaida {

    private Long materialId;

    private String nome;

    private String descricao;

    private String especificacao;

    private int quantidadeDisponivel;

    private StatusEstoque statusEstoque;

    public MaterialSaida(Material material) {
        this.materialId = material.getMaterialId();
        this.nome = material.getNome();
        this.descricao = material.getDescricao();
        this.especificacao = material.getEspecificacao();
        this.quantidadeDisponivel = material.getQuantidadeDisponivel();
        this.statusEstoque = material.statusEstoque();
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
}
