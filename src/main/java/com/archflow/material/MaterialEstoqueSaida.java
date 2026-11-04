package com.archflow.material;

import java.time.LocalDateTime;

// O horário informa a conclusão desta operação; não cria uma coluna no banco.
public class MaterialEstoqueSaida {

    private Long materialId;

    private int quantidadeDisponivel;

    private LocalDateTime atualizadaEm;

    public MaterialEstoqueSaida(Material material) {
        this.materialId = material.getMaterialId();
        this.quantidadeDisponivel = material.getQuantidadeDisponivel();
        this.atualizadaEm = LocalDateTime.now();
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
