package com.archflow.material;

// Situacao do estoque, calculada comparando a quantidade disponivel com a
// quantidade minima do material. Nao fica guardada no banco.
public enum StatusEstoque {
    DISPONIVEL,
    BAIXO,
    INDISPONIVEL
}
