package com.archflow.material;

// Situacao do estoque de um material, mostrada no catalogo.
// O valor nao e guardado no banco: sai da comparacao entre a quantidade
// disponivel e a quantidade minima do proprio material.
public enum StatusEstoque {
    DISPONIVEL,
    BAIXO,
    INDISPONIVEL
}
