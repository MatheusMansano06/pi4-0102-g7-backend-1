package com.archflow.material;

import java.time.LocalDateTime;

// A resposta usa o horário armazenado junto com o novo saldo.
public class MaterialEstoqueSaida {

    private Long materialId;

    private int quantidadeDisponivel;

    private LocalDateTime atualizadaEm;

    public MaterialEstoqueSaida(Material material) {
        this.materialId = material.getMaterialId();
        this.quantidadeDisponivel = material.getQuantidadeDisponivel();
        this.atualizadaEm = material.getAtualizadaEm();
    }

    public Long getMaterialId() {
        return this.materialId;
    }

    public int getQuantidadeDisponivel() {
        return this.quantidadeDisponivel;
    }

    public LocalDateTime getAtualizadaEm() {
        return this.atualizadaEm;
    }
}
