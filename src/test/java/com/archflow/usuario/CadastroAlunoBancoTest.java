package com.archflow.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.archflow.comum.PerfilUsuario;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

// Prova do CT14 com servidor HTTP e PostgreSQL reais. O nome do banco e
// restrito antes da subida do Spring; nunca apontar para o banco da equipe.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "ARCHFLOW_CT14_TESTE_URL",
        matches = "jdbc:postgresql://(localhost|127[.]0[.]0[.]1):[0-9]+/archflow_ct14_teste_[a-z0-9_]+")
@TestPropertySource(properties = {
        "server.address=127.0.0.1",
        "spring.datasource.url=${ARCHFLOW_CT14_TESTE_URL}",
        "spring.datasource.username=${ARCHFLOW_CT14_TESTE_USUARIO}",
        "spring.datasource.password=${ARCHFLOW_CT14_TESTE_SENHA}",
        "archflow.token.segredo=${ARCHFLOW_CT14_TESTE_SEGREDO}"
})
public class CadastroAlunoBancoTest {

    private static final String SENHA_DA_FICHA = "senha-exclusiva-da-ficha-ct14";

    private static final String EMAIL = "ct14-aluno@example.test";

    private static final String RA = "ct14-0001";

    @Autowired
    private TestRestTemplate api;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder conferenteDeSenha;

    @Autowired
    private JdbcTemplate sql;

    @BeforeEach
    public void prepararClienteHttp() {
        // A prova precisa ler as recusas 400 com corpo JSON.
        this.api.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        this.limparContasDaFicha();
    }

    @AfterEach
    public void limparContasDaFicha() {
        this.sql.update("DELETE FROM usuarios WHERE email LIKE 'ct14-%@example.test'");
        this.sql.update("DELETE FROM usuarios WHERE ra LIKE 'ct14-%'");
    }

    @Test
    public void cadastroGravaOAlunoNoBancoEDevolveTokenQueJaAbreOCatalogo() {
        ResponseEntity<JsonNode> criado = this.cadastrar(EMAIL, RA, SENHA_DA_FICHA);
        assertEquals(HttpStatus.CREATED, criado.getStatusCode());
        assertEquals("ALUNO", criado.getBody().get("perfil").asText());
        assertTrue(criado.getBody().get("usuarioId").asLong() > 0);
        // Quem acabou de se cadastrar ja cai logado na tela, entao a resposta
        // do CT14 precisa trazer o curso igual a do login.
        assertEquals("Sistemas de Informacao", criado.getBody().get("curso").asText());

        this.conferirContaGravada();

        String token = criado.getBody().get("token").asText();
        assertEquals(HttpStatus.OK, this.consultarCatalogo(token).getStatusCode());
    }

    private void conferirContaGravada() {
        Usuario gravado = this.usuarios.findByEmail(EMAIL);
        assertNotNull(gravado);
        assertEquals(RA, gravado.getRa());
        assertEquals(PerfilUsuario.ALUNO, gravado.getPerfil());
        assertTrue(gravado.isAtivo());
    }

    @Test
    public void senhaGravadaEHashBCryptEOLoginSeguinteFunciona() {
        this.cadastrar(EMAIL, RA, SENHA_DA_FICHA);
        Usuario gravado = this.usuarios.findByEmail(EMAIL);

        // O banco nao pode guardar a senha digitada, e o hash tem que ser um
        // BCrypt de verdade: e isso que falta nos usuarios de exemplo.
        assertFalse(SENHA_DA_FICHA.equals(gravado.getSenhaHash()));
        assertTrue(gravado.getSenhaHash().startsWith("$2"));
        assertTrue(this.conferenteDeSenha.matches(SENHA_DA_FICHA, gravado.getSenhaHash()));

        ResponseEntity<JsonNode> login = this.entrar(EMAIL, SENHA_DA_FICHA);
        assertEquals(HttpStatus.OK, login.getStatusCode());
        assertNotNull(login.getBody().get("token").asText());
    }

    @Test
    public void emailRepetidoERecusadoMesmoComOutraCaixa() {
        this.cadastrar(EMAIL, RA, SENHA_DA_FICHA);
        ResponseEntity<JsonNode> repetido =
                this.cadastrar(EMAIL.toUpperCase(), "ct14-0002", SENHA_DA_FICHA);
        this.conferirErro(repetido, HttpStatus.BAD_REQUEST, "EMAIL_JA_CADASTRADO");
    }

    @Test
    public void raRepetidoERecusado() {
        this.cadastrar(EMAIL, RA, SENHA_DA_FICHA);
        ResponseEntity<JsonNode> repetido =
                this.cadastrar("ct14-outro@example.test", RA, SENHA_DA_FICHA);
        this.conferirErro(repetido, HttpStatus.BAD_REQUEST, "RA_JA_CADASTRADO");
    }

    @Test
    public void cadastroRecusaCampoObrigatorioAusenteOuVazio() {
        String[][] corpos = {
            {null, RA, SENHA_DA_FICHA}, {EMAIL, null, SENHA_DA_FICHA},
            {EMAIL, RA, null}, {"   ", RA, SENHA_DA_FICHA}, {EMAIL, "   ", SENHA_DA_FICHA}
        };
        for (String[] corpo : corpos) {
            this.conferirErro(this.cadastrar(corpo[0], corpo[1], corpo[2]),
                    HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS");
        }
        assertNull(this.usuarios.findByEmail(EMAIL));
    }

    @Test
    public void senhaCurtaERecusadaESemGravarAConta() {
        this.conferirErro(this.cadastrar(EMAIL, RA, "1234567"),
                HttpStatus.BAD_REQUEST, "SENHA_FRACA");
        assertNull(this.usuarios.findByEmail(EMAIL));
    }

    @Test
    public void ninguemViraResponsavelPelaTelaDeCadastro() {
        // Mesmo mandando perfil no corpo, a rota publica so cria ALUNO.
        String corpo = "{\"nome\":\"Aluno da ficha\",\"ra\":\"" + RA + "\","
                + "\"curso\":\"Sistemas de Informacao\",\"email\":\"" + EMAIL + "\","
                + "\"senha\":\"" + SENHA_DA_FICHA + "\",\"perfil\":\"RESPONSAVEL\"}";
        ResponseEntity<JsonNode> criado = this.api.exchange("/api/auth/cadastro",
                HttpMethod.POST, this.corpoJson(corpo), JsonNode.class);

        assertEquals(HttpStatus.CREATED, criado.getStatusCode());
        assertEquals("ALUNO", criado.getBody().get("perfil").asText());
        assertEquals(PerfilUsuario.ALUNO, this.usuarios.findByEmail(EMAIL).getPerfil());
    }

    private ResponseEntity<JsonNode> cadastrar(String email, String ra, String senha) {
        String corpo = "{\"nome\":\"Aluno da ficha\"," + this.campo("ra", ra)
                + ",\"curso\":\"Sistemas de Informacao\"," + this.campo("email", email)
                + "," + this.campo("senha", senha) + "}";
        return this.api.exchange("/api/auth/cadastro", HttpMethod.POST,
                this.corpoJson(corpo), JsonNode.class);
    }

    private ResponseEntity<JsonNode> entrar(String email, String senha) {
        String corpo = "{" + this.campo("email", email) + "," + this.campo("senha", senha) + "}";
        return this.api.exchange("/api/auth/login", HttpMethod.POST,
                this.corpoJson(corpo), JsonNode.class);
    }

    private ResponseEntity<JsonNode> consultarCatalogo(String token) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.set("Authorization", "Bearer " + token);
        return this.api.exchange("/api/materiais", HttpMethod.GET,
                new HttpEntity<String>(cabecalhos), JsonNode.class);
    }

    private HttpEntity<String> corpoJson(String corpo) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<String>(corpo, cabecalhos);
    }

    private String campo(String nome, String valor) {
        if (valor == null) {
            return "\"" + nome + "\":null";
        }
        return "\"" + nome + "\":\"" + valor + "\"";
    }

    private void conferirErro(ResponseEntity<JsonNode> resposta, HttpStatus esperado,
            String codigo) {
        assertEquals(esperado, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(codigo, resposta.getBody().get("codigo").asText());
    }
}
