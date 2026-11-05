package com.archflow.solicitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
import com.archflow.material.Material;
import com.archflow.material.MaterialRepository;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// Identidade e política são fixtures controladas, não autenticação Keycloak real.
@WebMvcTest(SolicitacaoCriacaoController.class)
@Import(SolicitacaoCriacaoService.class)
public class SolicitacaoCriacaoTest {

    @Autowired
    private MockMvc api;

    @MockBean
    private MaterialRepository materiais;

    @MockBean
    private SolicitacaoRepository solicitacoes;

    private Material material;
    private UsuarioAutenticado aluno;

    @BeforeEach
    public void preparar() {
        this.material = new Material("Material de teste", "Fixture", "Teste", 10, 2);
        ReflectionTestUtils.setField(this.material, "materialId", 7L);
        this.politica(true, false, true, false);
        this.aluno = new UsuarioAutenticado(12L, PerfilUsuario.ALUNO);
        when(this.materiais.findByMaterialId(7L)).thenReturn(this.material);
        Solicitacao gravada = new Solicitacao(12L, this.material, 2, "Teste");
        ReflectionTestUtils.setField(gravada, "solicitacaoId", 101L);
        when(this.solicitacoes.saveAndFlush(any(Solicitacao.class))).thenReturn(gravada);
    }

    @Test
    public void semIdentidadeNaoCriaMesmoComCabecalhosInventados() throws Exception {
        this.api.perform(post("/api/solicitacoes").header("X-Perfil", "ALUNO")
                .header("X-Usuario-Id", "12").header("Authorization", "Bearer inventado")
                .contentType(MediaType.APPLICATION_JSON).content("{\"materialId\":7,\"quantidade\":2}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        this.semGravacao();
    }

    @Test
    public void principalSemVinculoValidadoNaoViraUsuarioPorNome() throws Exception {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("12");
        this.api.perform(post("/api/solicitacoes").principal(principal)
                .contentType(MediaType.APPLICATION_JSON).content("{\"materialId\":7,\"quantidade\":2}"))
                .andExpect(status().isUnauthorized());
        this.semGravacao();
    }

    @Test
    public void responsavelNaoCriaPedidoComoAluno() throws Exception {
        this.api.perform(this.pedido("{\"materialId\":7,\"quantidade\":2}")
                .requestAttr("usuarioAutenticado", new UsuarioAutenticado(13L, PerfilUsuario.RESPONSAVEL)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        this.semGravacao();
    }

    @Test
    public void materialIdDeveSerInteiroPositivo() throws Exception {
        String[] valores = {"null", "0", "-1", "7.0", "\"7\"", "true", "[]", "9223372036854775808"};
        for (String valor : valores) {
            this.erro400("{\"materialId\":" + valor + ",\"quantidade\":2}", "DADOS_INVALIDOS");
        }
        this.erro400("{\"quantidade\":2}", "DADOS_INVALIDOS");
        this.semGravacao();
    }

    @Test
    public void quantidadeDeveSerInteiraPositivaSemCoercao() throws Exception {
        String[] valores = {"null", "0", "-1", "2.0", "2.5", "\"2\"", "true", "{}", "2147483648"};
        for (String valor : valores) {
            this.erro400("{\"materialId\":7,\"quantidade\":" + valor + "}", "QUANTIDADE_INVALIDA");
        }
        this.erro400("{\"materialId\":7}", "QUANTIDADE_INVALIDA");
        this.semGravacao();
    }

    @Test
    public void corpoInvalidoOuAusenteNaoCria() throws Exception {
        for (String corpo : new String[] {"", "null", "{", "[]"}) {
            this.erro400(corpo, "DADOS_INVALIDOS");
        }
        this.semGravacao();
    }

    @Test
    public void camposDeAutoridadeOuAindaNaoDefinidosSaoRecusados() throws Exception {
        for (String campo : new String[] {"usuarioId", "perfil", "status", "criadaEm", "atualizadaEm", "dataLimite"}) {
            this.erro400("{\"materialId\":7,\"quantidade\":2,\"" + campo + "\":\"inventado\"}", "DADOS_INVALIDOS");
        }
        this.semGravacao();
    }

    @Test
    public void materialInexistenteNaoCria() throws Exception {
        when(this.materiais.findByMaterialId(7L)).thenReturn(null);
        this.api.perform(this.pedido("{\"materialId\":7,\"quantidade\":2}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("MATERIAL_NAO_ENCONTRADO"));
        this.semGravacao();
    }

    @Test
    public void materialInativoNaoCria() throws Exception {
        this.material.setAtivo(false);
        this.api.perform(this.pedido("{\"materialId\":7,\"quantidade\":2}"))
                .andExpect(status().isNotFound());
        this.semGravacao();
    }

    @Test
    public void politicaNaoDefinidaNaoViraPermissaoPorPadrao() throws Exception {
        this.material.setAceitaDimensao(null);
        this.api.perform(this.pedido("{\"materialId\":7,\"quantidade\":2}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("CONFIGURACAO_PENDENTE"));
        this.semGravacao();
    }

    @Test
    public void politicaInconsistenteNaoCria() throws Exception {
        this.politica(false, true, true, false);
        this.api.perform(this.pedido("{\"materialId\":7,\"quantidade\":2}"))
                .andExpect(status().isServiceUnavailable());
        this.semGravacao();
    }

    @Test
    public void parametrosObrigatoriosNaoPodemFaltar() throws Exception {
        this.politica(true, true, true, true);
        this.erro400("{\"materialId\":7,\"quantidade\":2}", "PARAMETROS_INVALIDOS");
        this.erro400("{\"materialId\":7,\"quantidade\":2,\"dimensao\":\"300 x 200 mm\"}", "PARAMETROS_INVALIDOS");
        this.semGravacao();
    }

    @Test
    public void parametrosNaoAceitosNaoSaoDescartadosSilenciosamente() throws Exception {
        this.politica(false, false, false, false);
        this.erro400("{\"materialId\":7,\"quantidade\":2,\"dimensao\":\"300 x 200 mm\"}", "PARAMETROS_INVALIDOS");
        this.erro400("{\"materialId\":7,\"quantidade\":2,\"diametro\":4.5}", "PARAMETROS_INVALIDOS");
        this.semGravacao();
    }

    @Test
    public void dimensaoInformadaDeveSerTextoNaoVazio() throws Exception {
        this.erro400("{\"materialId\":7,\"quantidade\":2,\"dimensao\":4}", "DADOS_INVALIDOS");
        this.erro400("{\"materialId\":7,\"quantidade\":2,\"dimensao\":\"   \"}", "PARAMETROS_INVALIDOS");
        this.semGravacao();
    }

    @Test
    public void diametroInformadoDeveSerNumeroPositivoFinito() throws Exception {
        for (String valor : new String[] {"0", "-1", "\"4.5\"", "true", "[]", "1e309"}) {
            this.erro400("{\"materialId\":7,\"quantidade\":2,\"diametro\":" + valor + "}", "PARAMETROS_INVALIDOS");
        }
        this.semGravacao();
    }

    @Test
    public void observacaoNaoPodeSerObjetoOuNumero() throws Exception {
        this.erro400("{\"materialId\":7,\"quantidade\":2,\"observacao\":{}}", "DADOS_INVALIDOS");
        this.erro400("{\"materialId\":7,\"quantidade\":2,\"observacao\":42}", "DADOS_INVALIDOS");
        this.semGravacao();
    }

    @Test
    public void criaPendenteComAlunoDatasEParametrosSemAlterarEstoque() throws Exception {
        this.api.perform(this.pedido("{\"materialId\":7,\"quantidade\":2,\"dimensao\":\"300 x 200 mm\","
                + "\"diametro\":4.5,\"observacao\":\"Preservar este texto\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.solicitacaoId").value(101))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
        ArgumentCaptor<Solicitacao> gravada = ArgumentCaptor.forClass(Solicitacao.class);
        verify(this.solicitacoes).saveAndFlush(gravada.capture());
        Solicitacao pedido = gravada.getValue();
        assertEquals(12L, pedido.getUsuarioId());
        assertEquals("300 x 200 mm", pedido.getDimensao());
        assertEquals(4.5, pedido.getDiametro());
        assertEquals("Preservar este texto", pedido.getObservacao());
        assertEquals(StatusSolicitacao.PENDENTE, pedido.getStatus());
        assertNotNull(pedido.getCriadaEm());
        assertEquals(pedido.getCriadaEm(), pedido.getAtualizadaEm());
        assertEquals(10, this.material.getQuantidadeDisponivel());
        verify(this.materiais, never()).save(any(Material.class));
    }

    @Test
    public void politicaExplicitaPodePermitirPedidoSemMedidas() throws Exception {
        this.politica(false, false, false, false);
        this.api.perform(this.pedido("{\"materialId\":7,\"quantidade\":2}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void falhaNaGravacaoNaoRetornaSucessoOuDetalheInterno() throws Exception {
        when(this.solicitacoes.saveAndFlush(any(Solicitacao.class)))
                .thenThrow(new RuntimeException("detalhe interno do banco"));
        this.api.perform(this.pedido("{\"materialId\":7,\"quantidade\":2}"))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.mensagem").value("Erro interno no servidor."));
    }

    private MockHttpServletRequestBuilder pedido(String corpo) {
        return post("/api/solicitacoes").requestAttr("usuarioAutenticado", this.aluno)
                .contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private void erro400(String corpo, String codigo) throws Exception {
        this.api.perform(this.pedido(corpo)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(codigo));
    }

    private void semGravacao() {
        verify(this.solicitacoes, never()).saveAndFlush(any(Solicitacao.class));
    }

    private void politica(boolean aceitaDimensao, boolean exigeDimensao, boolean aceitaDiametro, boolean exigeDiametro) {
        this.material.setAceitaDimensao(aceitaDimensao);
        this.material.setExigeDimensao(exigeDimensao);
        this.material.setAceitaDiametro(aceitaDiametro);
        this.material.setExigeDiametro(exigeDiametro);
    }
}
