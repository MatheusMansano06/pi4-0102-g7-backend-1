package com.archflow.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.EmissorDeToken;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NegocioException;
import com.archflow.comum.PerfilUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

// Confere as recusas e o acerto do CT01 (H02.1).
//
// O banco e a assinatura do token entram substituidos: o que se testa aqui e a
// regra do login, que decide antes de qualquer um dos dois.
public class AutenticacaoServiceTest {

    private UsuarioRepository repositorio = mock(UsuarioRepository.class);

    private PasswordEncoder conferenteDeSenha = mock(PasswordEncoder.class);

    private EmissorDeToken emissor = mock(EmissorDeToken.class);

    private AutenticacaoService servico() {
        return new AutenticacaoService(this.repositorio, this.conferenteDeSenha, this.emissor);
    }

    private AutenticacaoEntrada pedidoDeLogin(String email, String senha) {
        AutenticacaoEntrada entrada = new AutenticacaoEntrada();
        entrada.setEmail(email);
        entrada.setSenha(senha);
        return entrada;
    }

    private Usuario aluna() {
        return new Usuario("Ana Silva", "aluno@exemplo.com", "hash-guardado", PerfilUsuario.ALUNO);
    }

    @Test
    public void loginSemEmailOuSemSenhaERecusado() {
        AutenticacaoEntrada[] pedidos = {
            this.pedidoDeLogin(null, "senha"),
            this.pedidoDeLogin("aluno@exemplo.com", null),
            this.pedidoDeLogin("   ", "senha"),
            this.pedidoDeLogin("aluno@exemplo.com", "   ")
        };
        for (AutenticacaoEntrada pedido : pedidos) {
            try {
                this.servico().autenticar(pedido);
                fail("Um login sem e-mail ou sem senha não podia ter sido aceito.");
            } catch (NegocioException erro) {
                assertEquals(CodigosDeErro.DADOS_INVALIDOS, erro.getCodigo());
            }
        }
    }

    @Test
    public void emailQueNaoExisteNaoDizQueNaoExiste() {
        when(this.repositorio.findByEmail("ninguem@exemplo.com")).thenReturn(null);
        when(this.conferenteDeSenha.matches(anyString(), anyString())).thenReturn(false);
        try {
            this.servico().autenticar(this.pedidoDeLogin("ninguem@exemplo.com", "senha"));
            fail("Um e-mail sem conta não podia ter entrado.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.CREDENCIAIS_INVALIDAS, erro.getCodigo());
            assertEquals("E-mail ou senha incorretos.", erro.getMessage());
        }
    }

    @Test
    public void senhaErradaERecusada() {
        when(this.repositorio.findByEmail("aluno@exemplo.com")).thenReturn(this.aluna());
        when(this.conferenteDeSenha.matches("errada", "hash-guardado")).thenReturn(false);
        try {
            this.servico().autenticar(this.pedidoDeLogin("aluno@exemplo.com", "errada"));
            fail("Uma senha errada não podia ter entrado.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.CREDENCIAIS_INVALIDAS, erro.getCodigo());
        }
    }

    @Test
    public void contaInativaNaoEntraMesmoComASenhaCerta() {
        Usuario desligada = this.aluna();
        desligada.setAtivo(false);
        when(this.repositorio.findByEmail("aluno@exemplo.com")).thenReturn(desligada);
        when(this.conferenteDeSenha.matches("certa", "hash-guardado")).thenReturn(true);
        try {
            this.servico().autenticar(this.pedidoDeLogin("aluno@exemplo.com", "certa"));
            fail("Uma conta inativa não podia ter entrado.");
        } catch (AcessoNegadoException erro) {
            assertEquals(CodigosDeErro.USUARIO_INATIVO, erro.getCodigo());
        }
    }

    @Test
    public void senhaCertaDevolveOPerfilEOToken() {
        when(this.repositorio.findByEmail("aluno@exemplo.com")).thenReturn(this.aluna());
        when(this.conferenteDeSenha.matches("certa", "hash-guardado")).thenReturn(true);
        when(this.emissor.emitirPara(null, PerfilUsuario.ALUNO, "aluno@exemplo.com"))
                .thenReturn("token-emitido");

        AutenticacaoSaida saida =
                this.servico().autenticar(this.pedidoDeLogin("aluno@exemplo.com", "certa"));

        assertEquals("Ana Silva", saida.getNome());
        assertEquals(PerfilUsuario.ALUNO, saida.getPerfil());
        assertEquals("token-emitido", saida.getToken());
    }

    @Test
    public void espacoSobrandoNoEmailNaoAtrapalha() {
        when(this.repositorio.findByEmail("aluno@exemplo.com")).thenReturn(this.aluna());
        when(this.conferenteDeSenha.matches("certa", "hash-guardado")).thenReturn(true);
        when(this.emissor.emitirPara(null, PerfilUsuario.ALUNO, "aluno@exemplo.com"))
                .thenReturn("token-emitido");

        AutenticacaoSaida saida =
                this.servico().autenticar(this.pedidoDeLogin("  aluno@exemplo.com  ", "certa"));

        assertEquals("token-emitido", saida.getToken());
    }
}
