package com.archflow.comum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

// Confere o H02.2: o que o backend aceita como identidade e o que ele recusa.
public class LeitorDeTokenTest {

    // Dublê do decodificador: recusa "token-ruim" e devolve o token pronto
    // nos outros casos, sem conferir assinatura de verdade.
    private static class DecodificadorDeMentira implements JwtDecoder {

        private final Jwt token;

        public DecodificadorDeMentira(Jwt token) {
            this.token = token;
        }

        @Override
        public Jwt decode(String valor) {
            if ("token-ruim".equals(valor)) {
                throw new BadJwtException("assinatura");
            }
            return this.token;
        }
    }

    private LeitorDeToken leitor(Jwt token) {
        return new LeitorDeToken(new DecodificadorDeMentira(token));
    }

    private Jwt token(String usuarioId, String perfil) {
        Jwt.Builder construtor = Jwt.withTokenValue("token-bom").header("alg", "HS256");
        if (usuarioId != null) {
            construtor = construtor.claim("usuarioId", usuarioId);
        }
        if (perfil != null) {
            construtor = construtor.claim("perfil", perfil);
        }
        return construtor.build();
    }

    @Test
    public void semCabecalhoNinguemSeIdentificou() {
        try {
            this.leitor(this.token("12", "ALUNO")).ler(null);
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
                this.leitor(this.token("12", "ALUNO")).ler(cabecalho);
                fail("O cabeçalho \"" + cabecalho + "\" não podia ter sido aceito.");
            } catch (NaoAutenticadoException erro) {
                assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
            }
        }
    }

    @Test
    public void tokenForjadoOuVencidoNaoVale() {
        try {
            this.leitor(this.token("12", "ALUNO")).ler("Bearer token-ruim");
            fail("Um token que este backend não assinou não podia ter sido aceito.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void tokenSemPerfilNaoVale() {
        try {
            this.leitor(this.token("12", null)).ler("Bearer token-bom");
            fail("Um token sem perfil não foi emitido por este backend.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void perfilQueOSistemaNaoConheceNaoVale() {
        try {
            this.leitor(this.token("12", "PROFESSOR")).ler("Bearer token-bom");
            fail("Um perfil fora do CT01 não podia ter sido aceito.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void tokenSemUsuarioIdNaoVale() {
        try {
            this.leitor(this.token(null, "ALUNO")).ler("Bearer token-bom");
            fail("Sem o usuarioId no token o pedido não tem dono e não podia passar.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void usuarioIdQueNaoENumeroNaoVale() {
        try {
            this.leitor(this.token("abc", "ALUNO")).ler("Bearer token-bom");
            fail("Um usuarioId que não é número não podia ter sido aceito.");
        } catch (NaoAutenticadoException erro) {
            assertEquals(CodigosDeErro.NAO_AUTENTICADO, erro.getCodigo());
        }
    }

    @Test
    public void alunoEntraComONumeroDoToken() {
        UsuarioAutenticado identidade =
                this.leitor(this.token("12", "ALUNO")).ler("Bearer token-bom");
        assertEquals(PerfilUsuario.ALUNO, identidade.getPerfil());
        assertEquals(12L, identidade.getUsuarioId());
    }

    @Test
    public void responsavelEntraComONumeroDoToken() {
        UsuarioAutenticado identidade =
                this.leitor(this.token("9", "RESPONSAVEL")).ler("Bearer token-bom");
        assertEquals(PerfilUsuario.RESPONSAVEL, identidade.getPerfil());
        assertEquals(9L, identidade.getUsuarioId());
    }
}
