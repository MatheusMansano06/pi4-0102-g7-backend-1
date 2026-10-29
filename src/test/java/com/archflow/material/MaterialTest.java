package com.archflow.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

// Confere as regras que o proprio material garante: o que ele recusa no
// construtor e como ele classifica o proprio estoque. Nao precisa de banco.
public class MaterialTest {

    @Test
    public void materialSemNomeNaoPodeExistir() {
        try {
            new Material("   ", "Chapa para maquete", "12 mm", 10, 2);
            fail("Um material sem nome nao podia ter sido criado.");
        } catch (IllegalArgumentException erro) {
            assertEquals("O nome do material é obrigatório.", erro.getMessage());
        }
    }

    @Test
    public void quantidadeNegativaNaoPodeExistir() {
        Material madeira = new Material("Madeira", null, null, 10, 2);
        try {
            madeira.setQuantidadeDisponivel(-1);
            fail("A quantidade negativa nao podia ter sido aceita.");
        } catch (IllegalArgumentException erro) {
            assertEquals("A quantidade disponível não pode ser negativa.", erro.getMessage());
        }
    }

    @Test
    public void semEstoqueOMaterialFicaIndisponivel() {
        Material madeira = new Material("Madeira", null, null, 0, 2);

        assertEquals(StatusEstoque.INDISPONIVEL, madeira.statusEstoque());
    }

    @Test
    public void naQuantidadeMinimaOEstoqueFicaBaixo() {
        Material madeira = new Material("Madeira", null, null, 2, 2);

        assertEquals(StatusEstoque.BAIXO, madeira.statusEstoque());
    }

    @Test
    public void acimaDaQuantidadeMinimaOEstoqueFicaDisponivel() {
        Material madeira = new Material("Madeira", null, null, 24, 2);

        assertEquals(StatusEstoque.DISPONIVEL, madeira.statusEstoque());
    }

    @Test
    public void aSaidaLevaSoOsCamposDoContrato() {
        Material madeira = new Material("Madeira", "Chapa para maquete", "12 mm, 600 x 400 mm", 24, 2);

        MaterialSaida saida = new MaterialSaida(madeira);

        assertEquals("Madeira", saida.getNome());
        assertEquals("Chapa para maquete", saida.getDescricao());
        assertEquals("12 mm, 600 x 400 mm", saida.getEspecificacao());
        assertEquals(24, saida.getQuantidadeDisponivel());
        assertEquals(StatusEstoque.DISPONIVEL, saida.getStatusEstoque());
    }
}
