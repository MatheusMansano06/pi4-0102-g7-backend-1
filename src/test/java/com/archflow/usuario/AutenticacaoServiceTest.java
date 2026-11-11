package com.archflow.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NegocioException;
import org.junit.jupiter.api.Test;

// Confere a recusa do CT01 (H02.1) que acontece antes de o login tocar o banco.
// As outras recusas e o acerto so dao para provar com a tabela usuarios no ar,
// e estao no teste de integracao do H02.4.
public class AutenticacaoServiceTest {

    private AutenticacaoEntrada pedidoDeLogin(String email, String senha) {
        AutenticacaoEntrada entrada = new AutenticacaoEntrada();
        entrada.setEmail(email);
        entrada.setSenha(senha);
        return entrada;
    }

    @Test
    public void loginSemEmailOuSemSenhaERecusado() {
        AutenticacaoService servico = new AutenticacaoService(null, null, null);
        AutenticacaoEntrada[] pedidos = {
            this.pedidoDeLogin(null, "senha"),
            this.pedidoDeLogin("aluno@exemplo.com", null),
            this.pedidoDeLogin("   ", "senha"),
            this.pedidoDeLogin("aluno@exemplo.com", "   ")
        };
        for (AutenticacaoEntrada pedido : pedidos) {
            try {
                servico.autenticar(pedido);
                fail("Um login sem e-mail ou sem senha não podia ter sido aceito.");
            } catch (NegocioException erro) {
                assertEquals(CodigosDeErro.DADOS_INVALIDOS, erro.getCodigo());
            }
        }
    }
}
