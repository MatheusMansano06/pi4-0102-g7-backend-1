package com.archflow.material;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "materiais")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long materialId;

    private String nome;

    private String descricao;

    private String especificacao;

    private int quantidadeDisponivel;

    private int quantidadeMinima;

    private boolean ativo;

    // Null significa que a equipe ainda não definiu a política deste material.
    private Boolean aceitaDimensao;

    private Boolean exigeDimensao;

    private Boolean aceitaDiametro;

    private Boolean exigeDiametro;

    public Material() {
    }

    public Material(String nome, String descricao, String especificacao,
            int quantidadeDisponivel, int quantidadeMinima) {
        this.setNome(nome);
        this.setDescricao(descricao);
        this.setEspecificacao(especificacao);
        this.setQuantidadeDisponivel(quantidadeDisponivel);
        this.setQuantidadeMinima(quantidadeMinima);
        this.setAtivo(true);
    }

    public Long getMaterialId() {
        return this.materialId;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do material é obrigatório.");
        }
        this.nome = nome.trim();
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

    public int getQuantidadeDisponivel() {
        return this.quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(int quantidadeDisponivel) {
        if (quantidadeDisponivel < 0) {
            throw new IllegalArgumentException("A quantidade disponível não pode ser negativa.");
        }
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public int getQuantidadeMinima() {
        return this.quantidadeMinima;
    }

    public void setQuantidadeMinima(int quantidadeMinima) {
        if (quantidadeMinima < 0) {
            throw new IllegalArgumentException("A quantidade mínima não pode ser negativa.");
        }
        this.quantidadeMinima = quantidadeMinima;
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Boolean getAceitaDimensao() {
        return this.aceitaDimensao;
    }

    public void setAceitaDimensao(Boolean aceitaDimensao) {
        this.aceitaDimensao = aceitaDimensao;
    }

    public Boolean getExigeDimensao() {
        return this.exigeDimensao;
    }

    public void setExigeDimensao(Boolean exigeDimensao) {
        this.exigeDimensao = exigeDimensao;
    }

    public Boolean getAceitaDiametro() {
        return this.aceitaDiametro;
    }

    public void setAceitaDiametro(Boolean aceitaDiametro) {
        this.aceitaDiametro = aceitaDiametro;
    }

    public Boolean getExigeDiametro() {
        return this.exigeDiametro;
    }

    public void setExigeDiametro(Boolean exigeDiametro) {
        this.exigeDiametro = exigeDiametro;
    }

    // Sem nenhuma unidade em estoque o material nao pode ser pedido.
    // Chegando na quantidade minima, ele ainda pode, mas o responsavel
    // precisa enxergar que esta acabando.
    public StatusEstoque statusEstoque() {
        if (this.quantidadeDisponivel == 0) {
            return StatusEstoque.INDISPONIVEL;
        }
        if (this.quantidadeDisponivel <= this.quantidadeMinima) {
            return StatusEstoque.BAIXO;
        }
        return StatusEstoque.DISPONIVEL;
    }

    @Override
    public String toString() {
        return this.nome + " (" + this.quantidadeDisponivel + " em estoque)";
    }
}
