package com.archflow.notificacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.test.context.TestPropertySource;

// Servidor HTTP, login BCrypt/JWT e repositório JPA reais no DDL oficial.
// Só executa no banco loopback descartável dedicado, nunca no banco da equipe.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "ARCHFLOW_CT07_TESTE_URL",
        matches = "jdbc:postgresql://(localhost|127[.]0[.]0[.]1):[0-9]+/archflow_ct07_teste_[a-z0-9_]+")
@TestPropertySource(properties = {
        "server.address=127.0.0.1",
        "spring.datasource.url=${ARCHFLOW_CT07_TESTE_URL}",
        "spring.datasource.username=${ARCHFLOW_CT07_TESTE_USUARIO}",
        "spring.datasource.password=${ARCHFLOW_CT07_TESTE_SENHA}",
        "archflow.token.segredo=${ARCHFLOW_CT07_TESTE_SEGREDO}"
})
public class NotificacaoBancoTest {

    private static final String SENHA = "senha-exclusiva-fixture-ct07";

    @Autowired
    private TestRestTemplate api;

    @Autowired
    private JdbcTemplate sql;

    @Autowired
    private PasswordEncoder senhas;

    private final List<Long> contas = new ArrayList<Long>();
    private Long aluno;
    private Long outroAluno;
    private Long responsavel;
    private Long material;
    private Long pedido;
    private String tokenAluno;
    private String tokenOutro;
    private String tokenResponsavel;

    @BeforeEach
    public void prepararContasEPedido() {
        this.api.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
        String hash = this.senhas.encode(SENHA);
        this.aluno = this.criarAluno("ct07-aluno", hash);
        this.outroAluno = this.criarAluno("ct07-outro", hash);
        this.responsavel = this.sql.queryForObject("INSERT INTO usuarios "
                + "(nome,email,senha_hash,perfil,matricula_funcional,cargo) "
                + "VALUES ('Responsavel CT07','ct07-responsavel@example.test',?,'RESPONSAVEL','CT07-R','Teste') "
                + "RETURNING usuario_id", Long.class, hash);
        this.contas.add(this.responsavel);
        this.material = this.sql.queryForObject("INSERT INTO materiais "
                + "(nome,quantidade_disponivel,quantidade_minima) VALUES ('Fixture CT07',10,2) "
                + "RETURNING material_id", Long.class);
        this.pedido = this.sql.queryForObject("INSERT INTO solicitacoes "
                + "(usuario_id,material_id,quantidade,status,criada_em,atualizada_em) "
                + "VALUES (?,?,1,'PENDENTE',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) RETURNING solicitacao_id",
                Long.class, this.aluno, this.material);
        this.tokenAluno = this.entrar("ct07-aluno@example.test");
        this.tokenOutro = this.entrar("ct07-outro@example.test");
        this.tokenResponsavel = this.entrar("ct07-responsavel@example.test");
    }

    @AfterEach
    public void removerSomenteFixtures() {
        this.sql.execute("ALTER TABLE notificacoes DROP CONSTRAINT IF EXISTS ck_ct07_prova_falha");
        for (Long conta : this.contas) {
            this.sql.update("DELETE FROM notificacoes WHERE usuario_id = ?", conta);
            this.sql.update("DELETE FROM solicitacoes WHERE usuario_id = ?", conta);
            this.sql.update("DELETE FROM usuarios WHERE usuario_id = ?", conta);
        }
        if (this.material != null) {
            this.sql.update("DELETE FROM materiais WHERE material_id = ?", this.material);
        }
    }

    @Test
    public void mudancasReaisGeramAvisosConsultadosSomentePeloDono() {
        this.conferirStatus(this.mudarStatus("AGUARDANDO_COMPRA", this.tokenResponsavel), HttpStatus.OK);
        this.conferirStatus(this.mudarStatus("EM_SEPARACAO", this.tokenResponsavel), HttpStatus.OK);
        this.conferirStatus(this.mudarStatus("PRONTO_PARA_RETIRADA", this.tokenResponsavel), HttpStatus.OK);
        JsonNode avisos = this.listar(this.tokenAluno, "");
        assertEquals(3, avisos.size());
        assertEquals("PEDIDO_PRONTO", avisos.get(0).path("tipo").asText());
        assertEquals("PEDIDO_AGUARDANDO_COMPRA", avisos.get(2).path("tipo").asText());
        for (JsonNode aviso : avisos) {
            assertEquals(this.pedido.longValue(), aviso.path("solicitacaoId").asLong());
            assertFalse(aviso.has("usuarioId"));
            assertFalse(aviso.path("mensagem").asText().isEmpty());
            LocalDateTime data = LocalDateTime.parse(aviso.path("criadaEm").asText());
            assertNotNull(data);
            assertEquals(data, this.sql.queryForObject(
                    "SELECT criado_em FROM notificacoes WHERE notificacao_id = ?",
                    LocalDateTime.class, aviso.path("notificacaoId").asLong()));
        }
        assertTrue(this.listar(this.tokenOutro, "?usuarioId=" + this.aluno).isEmpty());
        assertTrue(this.listar(this.tokenResponsavel, "?usuarioId=" + this.aluno).isEmpty());
    }

    @Test
    public void responsavelRecebeSomenteSeusPropriosAvisos() {
        this.inserirAviso(this.aluno, false);
        Long id = this.inserirAviso(this.responsavel, false);
        JsonNode avisos = this.listar(this.tokenResponsavel, "?usuarioId=" + this.aluno);
        assertEquals(1, avisos.size());
        assertEquals(id.longValue(), avisos.get(0).path("notificacaoId").asLong());
        assertTrue(avisos.get(0).path("solicitacaoId").isNull());
    }

    @Test
    public void filtroMantemLidasENaoLidasSemAlterarLeitura() {
        this.inserirAviso(this.aluno, true);
        Long naoLida = this.inserirAviso(this.aluno, false);
        this.inserirAviso(this.outroAluno, false);
        assertEquals(2, this.listar(this.tokenAluno, "").size());
        assertEquals(2, this.listar(this.tokenAluno, "?somenteNaoLidas=false").size());
        JsonNode filtradas = this.listar(this.tokenAluno, "?somenteNaoLidas=true");
        assertEquals(1, filtradas.size());
        assertEquals(naoLida.longValue(), filtradas.get(0).path("notificacaoId").asLong());
        assertFalse(this.sql.queryForObject("SELECT lida FROM notificacoes WHERE notificacao_id = ?",
                Boolean.class, naoLida));
        assertEquals(2, this.listar(this.tokenAluno, "").size());
    }

    @Test
    public void usuarioSemAvisosRecebeObjetoComListaVazia() {
        this.inserirAviso(this.aluno, false);
        assertTrue(this.listar(this.tokenOutro, "").isEmpty());
        assertTrue(this.listar(this.tokenOutro, "?somenteNaoLidas=true").isEmpty());
    }

    @Test
    public void filtroInvalidoRecebe400SemAlterarAvisos() {
        Long id = this.inserirAviso(this.aluno, false);
        String[] invalidos = {"talvez", "1", "0", "TRUE", "on", ""};
        for (String valor : invalidos) {
            this.conferirErro(this.consultar(this.tokenAluno, "?somenteNaoLidas=" + valor),
                    HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS");
        }
        assertFalse(this.sql.queryForObject("SELECT lida FROM notificacoes WHERE notificacao_id = ?",
                Boolean.class, id));
    }

    @Test
    public void semTokenOuTokenInvalidoRecebe401EAlunoNaoMudaStatus() {
        this.conferirErro(this.consultar(null, ""), HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO");
        this.conferirErro(this.consultar("invalido", ""), HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO");
        this.conferirErro(this.mudarStatus("AGUARDANDO_COMPRA", this.tokenAluno),
                HttpStatus.FORBIDDEN, "ACESSO_NEGADO");
        assertEquals(0, this.sql.queryForObject("SELECT count(*) FROM notificacoes WHERE usuario_id = ?",
                Integer.class, this.aluno));
        assertEquals("PENDENTE", this.statusPersistido());
    }

    @Test
    public void falhaDoAvisoDesfazStatusPrevisaoEDataNoBanco() {
        this.sql.update("UPDATE solicitacoes SET status='EM_SEPARACAO', "
                + "previsao_retirada='2099-01-01 10:00:00', atualizada_em='2026-10-13 12:00:00' "
                + "WHERE solicitacao_id = ?", this.pedido);
        this.sql.execute("ALTER TABLE notificacoes ADD CONSTRAINT ck_ct07_prova_falha "
                + "CHECK (tipo <> 'PEDIDO_PRONTO') NOT VALID");
        this.conferirErro(this.requisitar("/api/solicitacoes/" + this.pedido + "/status", HttpMethod.PATCH,
                this.tokenResponsavel, "{\"status\":\"PRONTO_PARA_RETIRADA\","
                        + "\"previsaoRetirada\":\"2099-02-01T10:00:00\"}"),
                HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO");
        assertEquals("EM_SEPARACAO", this.statusPersistido());
        assertEquals(LocalDateTime.of(2099, 1, 1, 10, 0), this.sql.queryForObject(
                "SELECT previsao_retirada FROM solicitacoes WHERE solicitacao_id = ?",
                LocalDateTime.class, this.pedido));
        assertEquals(LocalDateTime.of(2026, 10, 13, 12, 0), this.sql.queryForObject(
                "SELECT atualizada_em FROM solicitacoes WHERE solicitacao_id = ?",
                LocalDateTime.class, this.pedido));
        assertEquals(0, this.sql.queryForObject("SELECT count(*) FROM notificacoes WHERE usuario_id = ?",
                Integer.class, this.aluno));
    }

    private Long criarAluno(String nome, String hash) {
        Long id = this.sql.queryForObject("INSERT INTO usuarios (nome,email,senha_hash,perfil,ra,curso) "
                + "VALUES (?,?,?,'ALUNO',?,'Curso de teste') RETURNING usuario_id",
                Long.class, nome, nome + "@example.test", hash, nome);
        this.contas.add(id);
        return id;
    }

    private String entrar(String email) {
        ResponseEntity<JsonNode> resposta = this.requisitar("/api/auth/login", HttpMethod.POST,
                null, "{\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}");
        this.conferirStatus(resposta, HttpStatus.OK);
        String token = resposta.getBody().path("token").asText();
        assertFalse(token.isEmpty());
        return token;
    }

    private Long inserirAviso(Long destinatario, boolean lida) {
        return this.sql.queryForObject("INSERT INTO notificacoes (usuario_id,tipo,mensagem,lida) "
                + "VALUES (?,'PEDIDO_PRONTO','Aviso de fixture',?) RETURNING notificacao_id",
                Long.class, destinatario, lida);
    }

    private JsonNode listar(String token, String parametros) {
        ResponseEntity<JsonNode> resposta = this.consultar(token, parametros);
        this.conferirStatus(resposta, HttpStatus.OK);
        JsonNode lista = resposta.getBody().path("notificacoes");
        assertTrue(lista.isArray());
        return lista;
    }

    private ResponseEntity<JsonNode> consultar(String token, String parametros) {
        return this.requisitar("/api/notificacoes" + parametros, HttpMethod.GET, token, null);
    }

    private ResponseEntity<JsonNode> mudarStatus(String status, String token) {
        return this.requisitar("/api/solicitacoes/" + this.pedido + "/status", HttpMethod.PATCH,
                token, "{\"status\":\"" + status + "\"}");
    }

    private ResponseEntity<JsonNode> requisitar(String caminho, HttpMethod metodo, String token, String corpo) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.set("Content-Type", "application/json");
        if (token != null) {
            cabecalhos.setBearerAuth(token);
        }
        return this.api.exchange(caminho, metodo, new HttpEntity<String>(corpo, cabecalhos), JsonNode.class);
    }

    private String statusPersistido() {
        return this.sql.queryForObject("SELECT status FROM solicitacoes WHERE solicitacao_id = ?",
                String.class, this.pedido);
    }

    private void conferirStatus(ResponseEntity<JsonNode> resposta, HttpStatus esperado) {
        assertEquals(esperado, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
    }

    private void conferirErro(ResponseEntity<JsonNode> resposta, HttpStatus esperado, String codigo) {
        this.conferirStatus(resposta, esperado);
        assertEquals(codigo, resposta.getBody().path("codigo").asText());
    }
}
