package com.archflow.comum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

// Confere que a recusa acontece antes da operacao: sem token valido, a
// chamada nao chega ao controlador, e a tela recebe o mesmo formato de erro
// do resto da API.
public class FiltroDeIdentidadeTest {

    private final ObjectMapper json = new ObjectMapper();

    private FiltroDeIdentidade filtro() {
        JwtDecoder decodificador = mock(JwtDecoder.class);
        when(decodificador.decode("bom")).thenReturn(Jwt.withTokenValue("bom")
                .header("alg", "HS256").claim("perfil", "ALUNO")
                .claim("usuarioId", "12").build());
        when(decodificador.decode("ruim")).thenThrow(new BadJwtException("assinatura"));
        return new FiltroDeIdentidade(new LeitorDeToken(decodificador), this.json);
    }

    private MockHttpServletRequest pedido(String metodo, String token) {
        MockHttpServletRequest pedido = new MockHttpServletRequest(metodo, "/api/materiais");
        pedido.addHeader("Origin", "http://localhost:5173");
        if (token != null) {
            pedido.addHeader("Authorization", "Bearer " + token);
        }
        return pedido;
    }

    @Test
    public void semTokenAChamadaNaoChegaAoControlador() throws Exception {
        MockHttpServletRequest pedido = this.pedido("GET", null);
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain corrente = new MockFilterChain();
        this.filtro().doFilter(pedido, resposta, corrente);
        assertEquals(401, resposta.getStatus());
        assertEquals("NAO_AUTENTICADO",
                this.json.readTree(resposta.getContentAsString()).get("codigo").asText());
        assertNull(corrente.getRequest(), "A chamada não podia ter seguido para o controlador.");
        assertEquals("http://localhost:5173", resposta.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void tokenForjadoNaoChegaAoControlador() throws Exception {
        MockHttpServletRequest pedido = this.pedido("GET", "ruim");
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain corrente = new MockFilterChain();
        this.filtro().doFilter(pedido, resposta, corrente);
        assertEquals(401, resposta.getStatus());
        assertNull(corrente.getRequest(), "A chamada não podia ter seguido para o controlador.");
    }

    @Test
    public void tokenBomGuardaAIdentidadeNoPedido() throws Exception {
        MockHttpServletRequest pedido = this.pedido("GET", "bom");
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain corrente = new MockFilterChain();
        this.filtro().doFilter(pedido, resposta, corrente);
        assertEquals(200, resposta.getStatus());
        assertNotNull(corrente.getRequest(), "A chamada devia ter seguido para o controlador.");
        UsuarioAutenticado identidade = (UsuarioAutenticado) pedido
                .getAttribute(FiltroDeIdentidade.ATRIBUTO_IDENTIDADE);
        assertEquals(PerfilUsuario.ALUNO, identidade.getPerfil());
        assertEquals(12L, identidade.getUsuarioId());
    }

    // O navegador pergunta antes, com OPTIONS e sem token, se a chamada e
    // permitida. Recusar esse OPTIONS cancelaria a chamada de verdade.
    @Test
    public void perguntaDoNavegadorPassaSemToken() throws Exception {
        MockHttpServletRequest pedido = this.pedido("OPTIONS", null);
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain corrente = new MockFilterChain();
        this.filtro().doFilter(pedido, resposta, corrente);
        assertEquals(200, resposta.getStatus());
        assertNotNull(corrente.getRequest(), "O OPTIONS devia ter seguido adiante.");
    }

    @Test
    public void oLoginPassaSemToken() throws Exception {
        MockHttpServletRequest pedido =
                new MockHttpServletRequest("POST", "/api/auth/login");
        pedido.addHeader("Origin", "http://localhost:5173");
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        MockFilterChain corrente = new MockFilterChain();

        this.filtro().doFilter(pedido, resposta, corrente);

        assertEquals(200, resposta.getStatus());
        assertNotNull(corrente.getRequest(), "O login tinha que ter chegado ao controlador.");
    }
}
