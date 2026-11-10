package com.archflow.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.archflow.comum.PerfilUsuario;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

// Prova da API com servidor HTTP, PostgreSQL e JWT reais. O nome do banco e
// restrito antes da subida do Spring; nunca apontar para o banco da equipe.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "ARCHFLOW_CT01_TESTE_URL",
        matches = "jdbc:postgresql://(localhost|127[.]0[.]0[.]1):[0-9]+/archflow_ct01_teste_[a-z0-9_]+")
@TestPropertySource(properties = {
        "server.address=127.0.0.1",
        "spring.datasource.url=${ARCHFLOW_CT01_TESTE_URL}",
        "spring.datasource.username=${ARCHFLOW_CT01_TESTE_USUARIO}",
        "spring.datasource.password=${ARCHFLOW_CT01_TESTE_SENHA}",
        "archflow.token.segredo=${ARCHFLOW_CT01_TESTE_SEGREDO}"
})
public class AutenticacaoBancoTest {

    private static final String SENHA_DA_FIXTURE = "senha-exclusiva-da-fixture-1141";

    @Autowired
    private TestRestTemplate api;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder conferenteDeSenha;

    private Usuario contaDeTeste;

    @AfterEach
    public void removerContaDeTeste() {
        if (this.contaDeTeste != null) {
            this.usuarios.deleteById(this.contaDeTeste.getUsuarioId());
        }
    }

    @Test
    public void alunoEntraConsultaCatalogoMasNaoAcessaFila() {
        String token = this.entrar(PerfilUsuario.ALUNO);
        this.conferirLista("/api/materiais", token);
        this.conferirErro(this.consultar("/api/solicitacoes/fila", token),
                HttpStatus.FORBIDDEN, "ACESSO_NEGADO");
    }

    @Test
    public void responsavelEntraEConsultaFila() {
        String token = this.entrar(PerfilUsuario.RESPONSAVEL);
        this.conferirLista("/api/materiais", token);
        this.conferirLista("/api/solicitacoes/fila", token);
    }

    @Test
    public void catalogoRecusaTokenAusenteOuInvalido() {
        this.conferirErro(this.consultar("/api/materiais", null),
                HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO");
        this.conferirErro(this.consultar("/api/materiais", "credencial-de-teste-invalida"),
                HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO");
    }

    private String entrar(PerfilUsuario perfil) {
        this.contaDeTeste = this.usuarios.saveAndFlush(new Usuario(
                "Conta de teste " + perfil.name(), "ct01-" + perfil.name() + "@example.test",
                this.conferenteDeSenha.encode(SENHA_DA_FIXTURE), perfil));
        AutenticacaoEntrada entrada = new AutenticacaoEntrada();
        entrada.setEmail(this.contaDeTeste.getEmail());
        entrada.setSenha(SENHA_DA_FIXTURE);
        ResponseEntity<JsonNode> resposta = this.api.postForEntity(
                "/api/auth/login", entrada, JsonNode.class);
        return this.conferirSessao(resposta, perfil);
    }

    private String conferirSessao(ResponseEntity<JsonNode> resposta, PerfilUsuario perfil) {
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        JsonNode sessao = resposta.getBody();
        assertNotNull(sessao);
        assertEquals(this.contaDeTeste.getUsuarioId().longValue(), sessao.path("usuarioId").asLong());
        assertEquals(this.contaDeTeste.getNome(), sessao.path("nome").asText());
        assertEquals(perfil.name(), sessao.path("perfil").asText());
        assertFalse(sessao.has("senha"));
        assertFalse(sessao.has("senhaHash"));
        assertTrue(sessao.path("token").isTextual());
        assertFalse(sessao.path("token").asText().isEmpty());
        return sessao.path("token").asText();
    }

    private ResponseEntity<JsonNode> consultar(String rota, String token) {
        HttpHeaders cabecalhos = new HttpHeaders();
        if (token != null) {
            cabecalhos.setBearerAuth(token);
        }
        return this.api.exchange(rota, HttpMethod.GET,
                new HttpEntity<Void>(cabecalhos), JsonNode.class);
    }

    private void conferirLista(String rota, String token) {
        ResponseEntity<JsonNode> resposta = this.consultar(rota, token);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertTrue(resposta.getBody().isArray());
    }

    private void conferirErro(ResponseEntity<JsonNode> resposta, HttpStatus estado, String codigo) {
        assertEquals(estado, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(codigo, resposta.getBody().path("codigo").asText());
    }
}
