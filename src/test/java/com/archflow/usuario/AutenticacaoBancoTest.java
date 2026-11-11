package com.archflow.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.archflow.comum.PerfilUsuario;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
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

    @Autowired
    private JdbcTemplate sql;

    @Autowired
    private JwtEncoder assinador;

    private Usuario contaDeTeste;

    private Long contaDeTesteId;

    @BeforeEach
    public void prepararClienteHttp() {
        // A prova precisa enviar Origin e ler recusas 401 com corpo JSON.
        this.api.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    @AfterEach
    public void removerContaDeTeste() {
        if (this.contaDeTesteId != null) {
            this.sql.update("DELETE FROM usuarios WHERE usuario_id = ?", this.contaDeTesteId);
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

    @Test
    public void senhaErradaEEmailInexistenteRecebemAMesmaRecusa() {
        this.criarConta(PerfilUsuario.ALUNO, true);
        ResponseEntity<JsonNode> senhaErrada = this.autenticar(this.contaDeTeste.getEmail(), "senha-errada");
        ResponseEntity<JsonNode> inexistente = this.autenticar("ct01-ausente@example.test", SENHA_DA_FIXTURE);
        this.conferirErro(senhaErrada, HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS");
        this.conferirErro(inexistente, HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS");
        assertEquals(senhaErrada.getBody(), inexistente.getBody());
    }

    @Test
    public void contaInativaComSenhaCorretaRecebe403() {
        this.criarConta(PerfilUsuario.ALUNO, false);
        this.conferirErro(this.autenticar(this.contaDeTeste.getEmail(), SENHA_DA_FIXTURE),
                HttpStatus.FORBIDDEN, "USUARIO_INATIVO");
    }

    @Test
    public void contaInativaComSenhaErradaRecebe401SemRevelarSuaSituacao() {
        this.criarConta(PerfilUsuario.ALUNO, false);
        this.conferirErro(this.autenticar(this.contaDeTeste.getEmail(), "senha-errada"),
                HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS");
    }

    @Test
    public void loginRecusaCamposObrigatoriosAusentesOuVazios() {
        String[][] entradas = {
            {null, SENHA_DA_FIXTURE}, {"ct01@example.test", null},
            {"   ", SENHA_DA_FIXTURE}, {"ct01@example.test", "   "}
        };
        for (String[] entrada : entradas) {
            this.conferirErro(this.autenticar(entrada[0], entrada[1]),
                    HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS");
        }
    }

    @Test
    public void catalogoRecusaTokenAssinadoMasExpirado() {
        this.criarConta(PerfilUsuario.ALUNO, true);
        Instant agora = Instant.now();
        JwtClaimsSet dados = JwtClaimsSet.builder().issuer("archflow")
                .subject(this.contaDeTeste.getEmail()).issuedAt(agora.minusSeconds(7200))
                .expiresAt(agora.minusSeconds(3600)).claim("usuarioId", this.contaDeTesteId)
                .claim("perfil", "ALUNO").build();
        String token = this.assinador.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), dados)).getTokenValue();
        this.conferirErro(this.consultar("/api/materiais", token),
                HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO");
    }

    @Test
    public void preVooSemTokenAutorizaSomenteOrigemDaTela() {
        ResponseEntity<String> permitida = this.consultarPreVoo("http://localhost:5173");
        assertEquals(HttpStatus.OK, permitida.getStatusCode());
        assertEquals("http://localhost:5173", permitida.getHeaders().getAccessControlAllowOrigin());
        ResponseEntity<String> recusada = this.consultarPreVoo("http://origem-nao-autorizada.test");
        assertEquals(HttpStatus.FORBIDDEN, recusada.getStatusCode());
        assertFalse(recusada.getHeaders().containsKey("Access-Control-Allow-Origin"));
    }

    private void criarConta(PerfilUsuario perfil, boolean ativa) {
        String email = "ct01-" + perfil.name() + "@example.test";
        String hash = this.conferenteDeSenha.encode(SENHA_DA_FIXTURE);
        if (perfil == PerfilUsuario.ALUNO) {
            this.contaDeTesteId = this.criarAluno(email, hash, ativa);
        }
        if (perfil == PerfilUsuario.RESPONSAVEL) {
            this.contaDeTesteId = this.criarResponsavel(email, hash, ativa);
        }
        this.contaDeTeste = this.usuarios.findByEmail(email);
        assertNotNull(this.contaDeTeste);
    }

    private Long criarAluno(String email, String hash, boolean ativa) {
        return this.sql.queryForObject(
                "INSERT INTO usuarios (nome, email, senha_hash, perfil, ativo, ra, curso) "
                        + "VALUES (?, ?, ?, 'ALUNO', ?, ?, ?) RETURNING usuario_id", Long.class,
                "Conta de teste ALUNO", email, hash, ativa, "CT01-A", "Curso de teste");
    }

    private Long criarResponsavel(String email, String hash, boolean ativa) {
        return this.sql.queryForObject(
                "INSERT INTO usuarios (nome, email, senha_hash, perfil, ativo, matricula_funcional, cargo) "
                        + "VALUES (?, ?, ?, 'RESPONSAVEL', ?, ?, ?) RETURNING usuario_id", Long.class,
                "Conta de teste RESPONSAVEL", email, hash, ativa, "CT01-R", "Teste");
    }

    private String entrar(PerfilUsuario perfil) {
        this.criarConta(perfil, true);
        return this.conferirSessao(this.autenticar(this.contaDeTeste.getEmail(), SENHA_DA_FIXTURE), perfil);
    }

    private ResponseEntity<JsonNode> autenticar(String email, String senha) {
        AutenticacaoEntrada entrada = new AutenticacaoEntrada();
        entrada.setEmail(email);
        entrada.setSenha(senha);
        return this.api.postForEntity(
                "/api/auth/login", entrada, JsonNode.class);
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

    private ResponseEntity<String> consultarPreVoo(String origem) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setOrigin(origem);
        cabecalhos.set("Access-Control-Request-Method", "GET");
        cabecalhos.set("Access-Control-Request-Headers", "Authorization");
        return this.api.exchange("/api/materiais", HttpMethod.OPTIONS,
                new HttpEntity<Void>(cabecalhos), String.class);
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
        assertFalse(resposta.getBody().has("token"));
        assertFalse(resposta.getBody().has("senha"));
        assertFalse(resposta.getBody().has("senhaHash"));
    }
}
