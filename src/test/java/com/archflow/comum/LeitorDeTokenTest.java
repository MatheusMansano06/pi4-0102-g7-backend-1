package com.archflow.comum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

// Confere o H02.2: o que o backend aceita como identidade e o que ele recusa.
//
// O decodificador entra substituido de proposito. Conferir assinatura de
// verdade exigiria o Keycloak no ar, e o que se testa aqui e a decisao tomada
// depois da conferencia: perfil, numero do aluno e cada recusa da ficha.
public class LeitorDeTokenTest {

    private JwtDecoder decodificador;

    private LeitorDeToken leitor(Jwt token) {
        this.decodificador = mock(JwtDecoder.class);
        when(this.decodificador.decode("token-bom")).thenReturn(token);
        when(this.decodificador.decode("token-ruim")).thenThrow(new BadJwtException("assinatura"));
        return new LeitorDeToken(this.decodificador);
    }

    private Jwt token(String usuarioId, String... papeis) {
        List<String> lista = new ArrayList<String>();
        for (String papel : papeis) {
            lista.add(papel);
        }
        Map<String, Object> acessoAoRealm = new HashMap<String, Object>();
        acessoAoRealm.put("roles", lista);
        Jwt.Builder construtor = Jwt.withTokenValue("token-bom").header("alg", "RS256")
                .claim("realm_access", acessoAoRealm);
        if (usuarioId != null) {
            construtor = construtor.claim("usuarioId", usuarioId);
        }
        return construtor.build();
    }

    @Test
    public void semCabecalhoNinguemSeIdentificou() {
        try {
            this.leitor(this.token("12", "aluno")).ler(null);
            fail("Sem o cabeçalho Authorization a chamada não podia ser aceita.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void cabecalhoSemBearerNaoVale() {
        String[] cabecalhos = {"token-bom", "Basic token-bom", "Bearer ", "Bearer    "};
        for (String cabecalho : cabecalhos) {
            try {
                this.leitor(this.token("12", "aluno")).ler(cabecalho);
                fail("O cabeçalho \"" + cabecalho + "\" não podia ter sido aceito.");
            } catch (NaoAutenticadoException erro) {
                assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
            }
        }
    }

    @Test
    public void tokenForjadoOuVencidoNaoVale() {
        try {
            this.leitor(this.token("12", "aluno")).ler("Bearer token-ruim");
            fail("Um token que o Keycloak não assinou não podia ter sido aceito.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void contaSemPapelDoArchflowNaoEntra() {
        try {
            this.leitor(this.token("12", "offline_access")).ler("Bearer token-bom");
            fail("Uma conta sem papel de aluno nem de responsável não podia ter entrado.");
        } catch (AcessoNegadoException erro) {
            assertEquals(CodigosDeErro.ACESSO_NEGADO, erro.getCodigo());
        }
    }

    @Test
    public void tokenSemUsuarioIdAvisaFaltaDeConfiguracao() {
        try {
            this.leitor(this.token(null, "aluno")).ler("Bearer token-bom");
            fail("Sem o usuarioId no token o pedido não tem dono e não podia passar.");
        } catch (ConfiguracaoPendenteException erro) {
            assertEquals(CodigosDeErro.CONFIGURACAO_PENDENTE, erro.getCodigo());
        }
    }

    @Test
    public void usuarioIdQueNaoENumeroAvisaFaltaDeConfiguracao() {
        try {
            this.leitor(this.token("abc", "aluno")).ler("Bearer token-bom");
            fail("Um usuarioId que não é número não podia ter sido aceito.");
        } catch (ConfiguracaoPendenteException erro) {
            assertEquals(CodigosDeErro.CONFIGURACAO_PENDENTE, erro.getCodigo());
        }
    }

    @Test
    public void alunoEntraComONumeroDoToken() {
        UsuarioAutenticado identidade = this.leitor(this.token("12", "aluno")).ler("Bearer token-bom");
        assertEquals(PerfilUsuario.ALUNO, identidade.getPerfil());
        assertEquals(12L, identidade.getUsuarioId());
    }

    @Test
    public void quemTemOsDoisPapeisAtendeComoResponsavel() {
        UsuarioAutenticado identidade = this.leitor(this.token("9", "aluno", "responsavel"))
                .ler("Bearer token-bom");
        assertEquals(PerfilUsuario.RESPONSAVEL, identidade.getPerfil());
        assertEquals(9L, identidade.getUsuarioId());
    }
}
