package com.archflow.solicitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archflow.comum.EmissorDeToken;
import com.archflow.comum.PerfilUsuario;
import com.archflow.material.Material;
import com.archflow.material.MaterialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

// PostgreSQL, filtro e assinatura JWT reais, com usuários exclusivos de teste.
// As tabelas vêm do SQL oficial; cada teste desfaz seus registros por rollback.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "ARCHFLOW_CT03_TESTE_URL",
        matches = "jdbc:postgresql://(localhost|127[.]0[.]0[.]1):[0-9]+/archflow_ct03_teste_[a-z0-9_]+")
@TestPropertySource(properties = {
        "spring.datasource.url=${ARCHFLOW_CT03_TESTE_URL}",
        "spring.datasource.username=${ARCHFLOW_CT03_TESTE_USUARIO}",
        "spring.datasource.password=${ARCHFLOW_CT03_TESTE_SENHA}",
        "archflow.token.segredo=${ARCHFLOW_CT03_TESTE_SEGREDO}"
})
public class SolicitacaoCriacaoBancoTest {

    @Autowired
    private MockMvc api;

    @Autowired
    private MaterialRepository materiais;

    @Autowired
    private SolicitacaoRepository solicitacoes;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private PasswordEncoder conferenteDeSenha;

    @Autowired
    private EntityManager entidades;

    @Autowired
    private JdbcTemplate sql;

    @Autowired
    private ObjectMapper json;

    @Value("${spring.datasource.url}")
    private String bancoUrl;

    private Material material;

    private Long alunoId;

    private String tokenDoAluno;

    private String tokenDoOutroAluno;

    private String tokenDoResponsavel;

    @BeforeEach
    public void preparar() {
        assertTrue(this.bancoUrl.contains("/archflow_ct03_teste_"), "Use somente o banco dedicado do CT03.");
        String hash = this.conferenteDeSenha.encode("senha-exclusiva-da-fixture-ct03");
        this.alunoId = this.criarAluno("ct03-aluno-a@example.test", "CT03-A", hash);
        Long outroAlunoId = this.criarAluno("ct03-aluno-b@example.test", "CT03-B", hash);
        Long responsavelId = this.sql.queryForObject(
                "INSERT INTO usuarios (nome, email, senha_hash, perfil, matricula_funcional, cargo) "
                        + "VALUES (?, ?, ?, 'RESPONSAVEL', ?, ?) RETURNING usuario_id", Long.class,
                "Fixture CT03 responsável", "ct03-responsavel@example.test", hash, "CT03-R", "Teste");
        this.tokenDoAluno = this.emissor.emitirPara(this.alunoId, PerfilUsuario.ALUNO,
                "ct03-aluno-a@example.test");
        this.tokenDoOutroAluno = this.emissor.emitirPara(outroAlunoId, PerfilUsuario.ALUNO,
                "ct03-aluno-b@example.test");
        this.tokenDoResponsavel = this.emissor.emitirPara(responsavelId, PerfilUsuario.RESPONSAVEL,
                "ct03-responsavel@example.test");
        this.material = new Material("Fixture CT03", "Sem dados reais", "Material de teste", 10, 2);
        this.material.setAceitaDimensao(true);
        this.material.setExigeDimensao(false);
        this.material.setAceitaDiametro(true);
        this.material.setExigeDiametro(false);
        this.material = this.materiais.saveAndFlush(this.material);
    }

    @Test
    public void postGravaAlunoPendenteDatasEParametrosConsultadosDeNovoNoPostgres() throws Exception {
        Long pedidoId = this.criarPedido();
        this.entidades.flush();
        this.entidades.clear();
        Solicitacao gravada = this.solicitacoes.findBySolicitacaoId(pedidoId);
        assertEquals(this.alunoId, gravada.getUsuarioId());
        assertEquals(2, gravada.getQuantidade());
        assertEquals(StatusSolicitacao.PENDENTE, gravada.getStatus());
        assertEquals("300 x 200 mm", gravada.getDimensao());
        assertEquals(4.5, gravada.getDiametro());
        assertEquals("Preservar observação", gravada.getObservacao());
        assertNotNull(gravada.getCriadaEm());
        assertEquals(gravada.getCriadaEm(), gravada.getAtualizadaEm());
        assertEquals("300 x 200 mm", this.sql.queryForObject(
                "SELECT dimensao FROM solicitacoes WHERE solicitacao_id = ?", String.class, pedidoId));
        assertEquals(10, gravada.getMaterial().getQuantidadeDisponivel());
    }

    @Test
    public void alteracaoDeStatusPreservaParametrosDoPedidoGravado() throws Exception {
        Long pedidoId = this.criarPedido();
        this.entidades.clear();
        SolicitacaoStatusEntrada entrada = new SolicitacaoStatusEntrada();
        entrada.setStatus("EM_SEPARACAO");
        entrada.setPrevisaoRetirada(LocalDateTime.now().plusDays(2));
        this.api.perform(patch("/api/solicitacoes/{solicitacaoId}/status", pedidoId)
                .header("Authorization", "Bearer " + this.tokenDoResponsavel)
                .contentType(MediaType.APPLICATION_JSON).content(this.json.writeValueAsBytes(entrada)))
                .andExpect(status().isOk());
        this.entidades.flush();
        this.entidades.clear();
        Solicitacao gravada = this.solicitacoes.findBySolicitacaoId(pedidoId);
        assertEquals(StatusSolicitacao.EM_SEPARACAO, gravada.getStatus());
        assertEquals("300 x 200 mm", gravada.getDimensao());
        assertEquals(4.5, gravada.getDiametro());
        assertEquals("Preservar observação", gravada.getObservacao());
        assertEquals(this.alunoId, gravada.getUsuarioId());
    }

    @Test
    public void politicaPadraoDoBancoPermitePedidoSemMedidasNoMvp() throws Exception {
        Long materialId = this.sql.queryForObject(
                "INSERT INTO materiais (nome, quantidade_disponivel, quantidade_minima, ativo) "
                        + "VALUES ('Fixture defaults MVP', 10, 2, TRUE) RETURNING material_id", Long.class);
        String resposta = this.api.perform(post("/api/solicitacoes")
                .header("Authorization", "Bearer " + this.tokenDoAluno)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"materialId\":" + materialId + ",\"quantidade\":2}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        Long pedidoId = this.json.readTree(resposta).get("solicitacaoId").longValue();
        this.entidades.flush();
        this.entidades.clear();
        Solicitacao gravada = this.solicitacoes.findBySolicitacaoId(pedidoId);
        assertEquals(this.alunoId, gravada.getUsuarioId());
        assertNull(gravada.getDimensao());
        assertNull(gravada.getDiametro());
        assertEquals(10, gravada.getMaterial().getQuantidadeDisponivel());
    }

    @Test
    public void politicaPendenteRecusaSemInserirPedido() throws Exception {
        this.material.setAceitaDimensao(null);
        this.materiais.saveAndFlush(this.material);
        long antes = this.solicitacoes.count();
        this.api.perform(post("/api/solicitacoes")
                .header("Authorization", "Bearer " + this.tokenDoAluno)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"materialId\":" + this.material.getMaterialId() + ",\"quantidade\":2}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("CONFIGURACAO_PENDENTE"));
        assertEquals(antes, this.solicitacoes.count());
    }

    @Test
    public void pedidoCriadoApareceNoHistoricoDoAlunoCorreto() throws Exception {
        Long pedidoId = this.criarPedido();
        this.entidades.flush();
        this.entidades.clear();
        JsonNode historico = this.consultarHistorico(this.tokenDoAluno);
        boolean encontrado = false;
        for (JsonNode pedido : historico) {
            if (pedido.path("solicitacaoId").longValue() == pedidoId.longValue()) {
                encontrado = true;
                assertEquals("PENDENTE", pedido.path("status").asText());
            }
        }
        assertTrue(encontrado, "O pedido criado deve aparecer no histórico do seu aluno.");
        for (JsonNode pedido : this.consultarHistorico(this.tokenDoOutroAluno)) {
            assertFalse(pedido.path("solicitacaoId").longValue() == pedidoId.longValue(),
                    "O pedido não pode aparecer no histórico de outro aluno.");
        }
    }

    // H05.3: a fila e o historico do responsavel dizem de quem e cada pedido.
    // O nome vem da tabela usuarios pela mesma coluna usuario_id que ja gravava
    // o dono, entao a prova so vale lendo de novo do PostgreSQL.
    @Test
    public void filaEHistoricoDoResponsavelTrazemONomeDoAlunoDono() throws Exception {
        Long pedidoId = this.criarPedido();
        this.entidades.flush();
        this.entidades.clear();

        assertEquals("Fixture CT03 aluno", this.alunoDoPedido(this.consultarFila(), pedidoId));
        assertEquals("Fixture CT03 aluno",
                this.alunoDoPedido(this.consultarHistorico(this.tokenDoResponsavel), pedidoId));
    }

    // H05.3: a tela da fila tem coluna de prazo. Pedido novo nasce sem previsao,
    // e a previsao gravada pelo responsavel no CT12 precisa aparecer na fila.
    @Test
    public void filaTrazAPrevisaoDeRetiradaGravadaPeloResponsavel() throws Exception {
        Long pedidoId = this.criarPedido();
        this.entidades.flush();
        this.entidades.clear();
        assertTrue(this.pedidoDaLista(this.consultarFila(), pedidoId)
                .path("previsaoRetirada").isNull(), "Pedido novo nao tem previsao marcada.");

        LocalDateTime previsao = LocalDateTime.now().plusDays(2).withNano(0);
        this.marcarEmSeparacao(pedidoId, previsao);
        this.entidades.flush();
        this.entidades.clear();

        JsonNode pedido = this.pedidoDaLista(this.consultarFila(), pedidoId);
        assertEquals(previsao, LocalDateTime.parse(pedido.path("previsaoRetirada").asText()));
    }

    // H10.3: a tela do historico do responsavel tem coluna de dimensao e deixa
    // ordenar por ela, entao o CT08 precisa devolver a medida do pedido.
    @Test
    public void historicoTrazADimensaoDoPedido() throws Exception {
        Long pedidoId = this.criarPedido();
        this.entidades.flush();
        this.entidades.clear();

        JsonNode pedido = this.pedidoDaLista(this.consultarHistorico(this.tokenDoAluno), pedidoId);
        assertEquals("300 x 200 mm", pedido.path("dimensao").asText());
    }

    @Test
    public void semTokenOuComTokenInvalidoNaoGravaMesmoComCabecalhosLivres() throws Exception {
        long antes = this.solicitacoes.count();
        this.api.perform(this.pedidoSemMedidas()
                .header("X-Perfil", "ALUNO").header("X-Usuario-Id", this.alunoId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        this.api.perform(this.pedidoSemMedidas().header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        assertEquals(antes, this.solicitacoes.count());
    }

    @Test
    public void responsavelAutenticadoNaoCriaPedidoNemAlteraSaldo() throws Exception {
        long antes = this.solicitacoes.count();
        this.api.perform(this.pedidoSemMedidas()
                .header("Authorization", "Bearer " + this.tokenDoResponsavel))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        assertEquals(antes, this.solicitacoes.count());
        assertEquals(10, this.materiais.findByMaterialId(this.material.getMaterialId()).getQuantidadeDisponivel());
    }

    private Long criarAluno(String email, String ra, String hash) {
        return this.sql.queryForObject(
                "INSERT INTO usuarios (nome, email, senha_hash, perfil, ra, curso) "
                        + "VALUES (?, ?, ?, 'ALUNO', ?, ?) RETURNING usuario_id", Long.class,
                "Fixture CT03 aluno", email, hash, ra, "Curso de teste");
    }

    // Leva o pedido para EM_SEPARACAO ja com a previsao, como o CT12 faz.
    private void marcarEmSeparacao(Long pedidoId, LocalDateTime previsao) throws Exception {
        SolicitacaoStatusEntrada entrada = new SolicitacaoStatusEntrada();
        entrada.setStatus("EM_SEPARACAO");
        entrada.setPrevisaoRetirada(previsao);
        this.api.perform(patch("/api/solicitacoes/{solicitacaoId}/status", pedidoId)
                .header("Authorization", "Bearer " + this.tokenDoResponsavel)
                .contentType(MediaType.APPLICATION_JSON).content(this.json.writeValueAsBytes(entrada)))
                .andExpect(status().isOk());
    }

    private JsonNode consultarFila() throws Exception {
        byte[] resposta = this.api.perform(get("/api/solicitacoes/fila")
                .header("Authorization", "Bearer " + this.tokenDoResponsavel))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        return this.json.readTree(resposta);
    }

    // Acha o pedido na lista e devolve o nome do aluno que veio nele.
    private String alunoDoPedido(JsonNode lista, Long pedidoId) {
        return this.pedidoDaLista(lista, pedidoId).path("aluno").asText(null);
    }

    private JsonNode pedidoDaLista(JsonNode lista, Long pedidoId) {
        for (JsonNode pedido : lista) {
            if (pedido.path("solicitacaoId").longValue() == pedidoId.longValue()) {
                return pedido;
            }
        }
        fail("O pedido " + pedidoId + " devia estar na lista.");
        return null;
    }

    private JsonNode consultarHistorico(String token) throws Exception {
        byte[] resposta = this.api.perform(get("/api/solicitacoes/historico")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        return this.json.readTree(resposta);
    }

    private MockHttpServletRequestBuilder pedidoSemMedidas() {
        return post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"materialId\":" + this.material.getMaterialId() + ",\"quantidade\":2}");
    }

    private Long criarPedido() throws Exception {
        String resposta = this.api.perform(post("/api/solicitacoes")
                .header("Authorization", "Bearer " + this.tokenDoAluno)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"materialId\":" + this.material.getMaterialId()
                        + ",\"quantidade\":2,\"dimensao\":\"300 x 200 mm\",\"diametro\":4.5,"
                        + "\"observacao\":\"Preservar observação\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return this.json.readTree(resposta).get("solicitacaoId").longValue();
    }
}
