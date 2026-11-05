package com.archflow.solicitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
import com.archflow.material.Material;
import com.archflow.material.MaterialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// PostgreSQL real, mas identidade e políticas são fixtures de teste, não Keycloak.
// Não cria tabelas nem apaga registros. Cada teste desfaz sua transação no final.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "ARCHFLOW_CT03_TESTE_URL", matches = ".+")
@TestPropertySource(properties = {
        "spring.datasource.url=${ARCHFLOW_CT03_TESTE_URL}",
        "spring.datasource.username=${ARCHFLOW_CT03_TESTE_USUARIO}",
        "spring.datasource.password=${ARCHFLOW_CT03_TESTE_SENHA}"
})
public class SolicitacaoCriacaoBancoTest {

    @Autowired
    private MockMvc api;

    @Autowired
    private MaterialRepository materiais;

    @Autowired
    private SolicitacaoRepository solicitacoes;

    @Autowired
    private SolicitacaoService status;

    @Autowired
    private EntityManager entidades;

    @Autowired
    private JdbcTemplate sql;

    @Autowired
    private ObjectMapper json;

    @Value("${spring.datasource.url}")
    private String bancoUrl;

    private Material material;

    @BeforeEach
    public void preparar() {
        assertTrue(this.bancoUrl.contains("/archflow_ct03_teste_"), "Use somente o banco dedicado do CT03.");
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
        assertEquals(12L, gravada.getUsuarioId());
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
        this.status.atualizarStatus("RESPONSAVEL", pedidoId, entrada);
        this.entidades.flush();
        this.entidades.clear();
        Solicitacao gravada = this.solicitacoes.findBySolicitacaoId(pedidoId);
        assertEquals(StatusSolicitacao.EM_SEPARACAO, gravada.getStatus());
        assertEquals("300 x 200 mm", gravada.getDimensao());
        assertEquals(4.5, gravada.getDiametro());
        assertEquals("Preservar observação", gravada.getObservacao());
        assertEquals(12L, gravada.getUsuarioId());
    }

    @Test
    public void politicaPendenteRecusaSemInserirPedido() throws Exception {
        this.material.setAceitaDimensao(null);
        this.materiais.saveAndFlush(this.material);
        long antes = this.solicitacoes.count();
        this.api.perform(post("/api/solicitacoes")
                .requestAttr("usuarioAutenticado", new UsuarioAutenticado(12L, PerfilUsuario.ALUNO))
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
        List<SolicitacaoHistoricoSaida> historico = this.status.listarHistorico("ALUNO", 12L);
        boolean encontrado = false;
        for (SolicitacaoHistoricoSaida pedido : historico) {
            if (pedido.getSolicitacaoId().equals(pedidoId)) {
                encontrado = true;
                assertEquals(StatusSolicitacao.PENDENTE, pedido.getStatus());
            }
        }
        assertTrue(encontrado, "O pedido criado deve aparecer no histórico do seu aluno.");
        for (SolicitacaoHistoricoSaida pedido : this.status.listarHistorico("ALUNO", 13L)) {
            assertFalse(pedido.getSolicitacaoId().equals(pedidoId),
                    "O pedido não pode aparecer no histórico de outro aluno.");
        }
    }

    private Long criarPedido() throws Exception {
        String resposta = this.api.perform(post("/api/solicitacoes")
                .requestAttr("usuarioAutenticado", new UsuarioAutenticado(12L, PerfilUsuario.ALUNO))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"materialId\":" + this.material.getMaterialId()
                        + ",\"quantidade\":2,\"dimensao\":\"300 x 200 mm\",\"diametro\":4.5,"
                        + "\"observacao\":\"Preservar observação\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return this.json.readTree(resposta).get("solicitacaoId").longValue();
    }
}
