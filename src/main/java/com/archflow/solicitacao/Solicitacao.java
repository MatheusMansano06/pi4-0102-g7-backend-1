package com.archflow.solicitacao;

import com.archflow.material.Material;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long solicitacaoId;

    private Long usuarioId;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    private int quantidade;

    @Enumerated(EnumType.STRING)
    private StatusSolicitacao status;

    private LocalDateTime criadaEm;

    private LocalDateTime atualizadaEm;

    private LocalDateTime previsaoRetirada;

    private String observacao;

    public Solicitacao() {
    }

    public Solicitacao(Long usuarioId, Material material, int quantidade, String observacao) {
        this.setUsuarioId(usuarioId);
        this.setMaterial(material);
        this.setQuantidade(quantidade);
        this.setObservacao(observacao);
        this.setStatus(StatusSolicitacao.PENDENTE);
        this.criadaEm = LocalDateTime.now();
    }

    public Long getSolicitacaoId() {
        return this.solicitacaoId;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("O pedido precisa ser de algum aluno.");
        }
        this.usuarioId = usuarioId;
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("O pedido precisa de um material.");
        }
        this.material = material;
    }

    public int getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public StatusSolicitacao getStatus() {
        return this.status;
    }

    // Toda troca de status marca a hora, que e o que o CT12 devolve em atualizadaEm.
    public void setStatus(StatusSolicitacao status) {
        if (status == null) {
            throw new IllegalArgumentException("O pedido precisa de um status.");
        }
        this.status = status;
        this.atualizadaEm = LocalDateTime.now();
    }

    public LocalDateTime getCriadaEm() {
        return this.criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return this.atualizadaEm;
    }

    public LocalDateTime getPrevisaoRetirada() {
        return this.previsaoRetirada;
    }

    public void setPrevisaoRetirada(LocalDateTime previsaoRetirada) {
        this.previsaoRetirada = previsaoRetirada;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public String toString() {
        return "Pedido de " + this.quantidade + " " + this.material.getNome() + " (" + this.status + ")";
    }
}
