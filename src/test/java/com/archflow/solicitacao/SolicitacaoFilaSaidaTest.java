package com.archflow.solicitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.archflow.material.Material;
import org.junit.jupiter.api.Test;

// Confere o CT04 do lado da saida: o pedido que o aluno fez chega na fila do
// responsavel com os mesmos valores, inclusive a medida e a observacao.
// Nao precisa de banco, porque aqui so se monta a resposta a partir do pedido.
public class SolicitacaoFilaSaidaTest {

    private Material madeira() {
        return new Material("Madeira", "Chapa para maquete", "12 mm", 10, 2);
    }

    private Solicitacao pedidoComMedida() {
        Solicitacao pedido = new Solicitacao(12L, this.madeira(), 2, "Uso em maquete");
        pedido.setDimensao("30x40 cm");
        pedido.setDiametro(8.5);
        return pedido;
    }

    @Test
    public void filaMostraADimensaoODiametroEAObservacaoDoPedido() {
        SolicitacaoFilaSaida saida = new SolicitacaoFilaSaida(this.pedidoComMedida());

        assertEquals("30x40 cm", saida.getDimensao());
        assertEquals(8.5, saida.getDiametro());
        assertEquals("Uso em maquete", saida.getObservacao());
    }

    @Test
    public void filaMantemOsDadosQueJaSaiamAntes() {
        SolicitacaoFilaSaida saida = new SolicitacaoFilaSaida(this.pedidoComMedida());

        assertEquals("Madeira", saida.getMaterial());
        assertEquals(2, saida.getQuantidade());
        assertEquals(StatusSolicitacao.PENDENTE, saida.getStatus());
    }

    @Test
    public void pedidoSemMedidaNemObservacaoSaiComOsCamposVazios() {
        Solicitacao pedido = new Solicitacao(12L, this.madeira(), 1, null);

        SolicitacaoFilaSaida saida = new SolicitacaoFilaSaida(pedido);

        assertNull(saida.getDimensao());
        assertNull(saida.getDiametro());
        assertNull(saida.getObservacao());
    }
}
