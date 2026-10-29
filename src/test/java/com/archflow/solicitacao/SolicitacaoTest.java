package com.archflow.solicitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.archflow.material.Material;
import org.junit.jupiter.api.Test;

// Confere o que o proprio pedido garante: o que ele recusa e como ele nasce.
// Nao precisa de banco.
public class SolicitacaoTest {

    private Material madeira() {
        return new Material("Madeira", "Chapa para maquete", "12 mm", 10, 2);
    }

    @Test
    public void pedidoNasceComPendenteEComHora() {
        Solicitacao pedido = new Solicitacao(12L, this.madeira(), 2, "Uso em maquete");

        assertEquals(StatusSolicitacao.PENDENTE, pedido.getStatus());
        assertNotNull(pedido.getCriadaEm());
    }

    @Test
    public void pedidoSemMaterialNaoPodeExistir() {
        try {
            new Solicitacao(12L, null, 2, null);
            fail("Um pedido sem material nao podia ter sido criado.");
        } catch (IllegalArgumentException erro) {
            assertEquals("O pedido precisa de um material.", erro.getMessage());
        }
    }

    @Test
    public void pedidoSemAlunoNaoPodeExistir() {
        try {
            new Solicitacao(null, this.madeira(), 2, null);
            fail("Um pedido sem aluno nao podia ter sido criado.");
        } catch (IllegalArgumentException erro) {
            assertEquals("O pedido precisa ser de algum aluno.", erro.getMessage());
        }
    }

    @Test
    public void quantidadeZeradaNaoPodeExistir() {
        try {
            new Solicitacao(12L, this.madeira(), 0, null);
            fail("A quantidade zerada nao podia ter sido aceita.");
        } catch (IllegalArgumentException erro) {
            assertEquals("A quantidade deve ser maior que zero.", erro.getMessage());
        }
    }

    @Test
    public void trocaDeStatusMarcaAHoraDaMudanca() {
        Solicitacao pedido = new Solicitacao(12L, this.madeira(), 2, null);

        pedido.setStatus(StatusSolicitacao.EM_SEPARACAO);

        assertEquals(StatusSolicitacao.EM_SEPARACAO, pedido.getStatus());
        assertNotNull(pedido.getAtualizadaEm());
    }
}
