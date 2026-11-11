package com.archflow.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archflow.comum.NegocioException;
import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// Exercita a rota e as regras reais, substituindo apenas o acesso ao banco.
// A identidade entra como atributo do pedido, no lugar em que o
// FiltroDeIdentidade a guarda depois de conferir o JWT. Quem
// confere o token é o LeitorDeTokenTest.
@WebMvcTest({MaterialController.class, MaterialEstoqueController.class})
@Import(MaterialService.class)
@TestPropertySource(properties = "archflow.estoque.em-desenvolvimento=true")
public class MaterialEstoqueTest {

    private static final LocalDateTime HORARIO_ANTERIOR = LocalDateTime.of(2026, 10, 13, 12, 5, 7);

    @MockBean
    private MaterialRepository repositorio;

    @Autowired
    private MockMvc api;

    private Material madeira;

    private UsuarioAutenticado responsavel;

    private UsuarioAutenticado aluno;

    @BeforeEach
    public void preparar() {
        this.responsavel = new UsuarioAutenticado(9L, PerfilUsuario.RESPONSAVEL);
        this.aluno = new UsuarioAutenticado(1L, PerfilUsuario.ALUNO);
        this.madeira = new Material("Madeira", "Chapa", "12 mm", 10, 2);
        this.madeira.atualizarEstoque(10, HORARIO_ANTERIOR);
        ReflectionTestUtils.setField(this.madeira, "materialId", 7L);
        when(this.repositorio.findByMaterialId(7L)).thenReturn(this.madeira);
        when(this.repositorio.saveAndFlush(this.madeira)).thenReturn(this.madeira);
    }

    @Test
    public void semIdentidadeNaoAlteraEstoque() throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        this.exigirSemGravacao();
    }

    @Test
    public void alunoNaoAlteraEstoque() throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque").requestAttr("usuarioAutenticado", this.aluno)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        this.exigirSemGravacao();
    }

    @Test
    public void materialInexistenteNaoGrava() throws Exception {
        this.api.perform(patch("/api/materiais/999/estoque").requestAttr("usuarioAutenticado", this.responsavel)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("MATERIAL_NAO_ENCONTRADO"));
        this.exigirSemGravacao();
    }

    @Test
    public void quantidadeAusenteNulaOuNegativaNaoGrava() throws Exception {
        String[] corpos = {"{}", "{\"quantidadeDisponivel\":null}", "{\"quantidadeDisponivel\":-1}"};
        for (String corpo : corpos) {
            this.exigirQuantidadeInvalida(corpo);
        }
        this.exigirSemGravacao();
    }

    @Test
    public void tipoInvalidoNaoSofreConversaoSilenciosa() throws Exception {
        String[] valores = {"2.5", "2.0", "\"2\"", "\"abc\"", "true", "[]", "{}", "2147483648"};
        for (String valor : valores) {
            this.exigirQuantidadeInvalida("{\"quantidadeDisponivel\":" + valor + "}");
        }
        this.exigirSemGravacao();
    }

    @Test
    public void corpoAusenteNuloOuMalformadoNaoGrava() throws Exception {
        String[] corpos = {"", "null", "{", "[]"};
        for (String corpo : corpos) {
            this.exigirQuantidadeInvalida(corpo);
        }
        this.exigirSemGravacao();
    }

    @Test
    public void saldoSubstituiQuantidadeAnteriorEDevolveHorario() throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque").requestAttr("usuarioAutenticado", this.responsavel)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId").value(7))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(24))
                .andExpect(jsonPath("$.atualizadaEm").isString())
                .andExpect(jsonPath("$.nome").doesNotExist());
        verify(this.repositorio).saveAndFlush(this.madeira);
        assertEquals(24, this.madeira.getQuantidadeDisponivel());
        assertNotNull(this.madeira.getAtualizadaEm());
        assertNotEquals(HORARIO_ANTERIOR, this.madeira.getAtualizadaEm());
    }

    @Test
    public void respostaUsaHorarioDoMaterialRetornadoPelaGravacao() throws Exception {
        Material gravado = new Material("Madeira", "Chapa", "12 mm", 24, 2);
        gravado.atualizarEstoque(24, HORARIO_ANTERIOR);
        when(this.repositorio.saveAndFlush(this.madeira)).thenReturn(gravado);
        this.api.perform(patch("/api/materiais/7/estoque").requestAttr("usuarioAutenticado", this.responsavel)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atualizadaEm").value("2026-10-13T12:05:07"));
    }

    @Test
    public void zeroEValidoECatalogoPassaAMostrarIndisponivel() throws Exception {
        List<Material> materiais = new ArrayList<Material>();
        materiais.add(this.madeira);
        when(this.repositorio.findByAtivoTrueOrderByNomeAsc()).thenReturn(materiais);
        this.api.perform(patch("/api/materiais/7/estoque").requestAttr("usuarioAutenticado", this.responsavel)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeDisponivel").value(0));
        this.api.perform(get("/api/materiais"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantidadeDisponivel").value(0))
                .andExpect(jsonPath("$[0].statusEstoque").value("INDISPONIVEL"))
                .andExpect(jsonPath("$[0].atualizadaEm").doesNotExist());
        verify(this.repositorio).saveAndFlush(this.madeira);
    }

    @Test
    public void falhaDeGravacaoNaoRetornaSucessoNemDetalheInterno() throws Exception {
        when(this.repositorio.saveAndFlush(this.madeira)).thenThrow(new RuntimeException("falha simulada"));
        this.api.perform(patch("/api/materiais/7/estoque").requestAttr("usuarioAutenticado", this.responsavel)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.mensagem").value("Erro interno no servidor."))
                .andExpect(jsonPath("$.atualizadaEm").doesNotExist());
    }

    @Test
    public void chamadaDiretaComEntradaNulaTambemRecusa() {
        MaterialService servico = new MaterialService(this.repositorio);
        try {
            servico.atualizarEstoque(this.responsavel, 7L, null);
            fail("Entrada nula não podia alterar o estoque.");
        } catch (NegocioException erro) {
            assertEquals("QUANTIDADE_INVALIDA", erro.getCodigo());
        }
        this.exigirSemGravacao();
    }

    private void exigirQuantidadeInvalida(String corpo) throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque").requestAttr("usuarioAutenticado", this.responsavel)
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("QUANTIDADE_INVALIDA"));
    }

    private void exigirSemGravacao() {
        verify(this.repositorio, never()).saveAndFlush(any(Material.class));
        assertEquals(10, this.madeira.getQuantidadeDisponivel());
        assertEquals(HORARIO_ANTERIOR, this.madeira.getAtualizadaEm());
    }
}
