package com.archflow.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.test.web.servlet.MockMvc;

// Exercita a rota e as regras reais, substituindo apenas o acesso ao banco.
// X-Perfil é provisório: estes testes não comprovam autenticação por token.
@WebMvcTest(MaterialController.class)
@Import(MaterialService.class)
public class MaterialEstoqueTest {

    @MockBean
    private MaterialRepository repositorio;

    @Autowired
    private MockMvc api;

    private Material madeira;

    @BeforeEach
    public void preparar() {
        this.madeira = new Material("Madeira", "Chapa", "12 mm", 10, 2);
        ReflectionTestUtils.setField(this.madeira, "materialId", 7L);
        when(this.repositorio.findByMaterialId(7L)).thenReturn(this.madeira);
        when(this.repositorio.save(this.madeira)).thenReturn(this.madeira);
    }

    @Test
    public void semPerfilNaoAlteraEstoque() throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        this.exigirSemGravacao();
    }

    @Test
    public void perfilVazioNaoAlteraEstoque() throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque").header("X-Perfil", "   ")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        this.exigirSemGravacao();
    }

    @Test
    public void alunoNaoAlteraEstoque() throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque").header("X-Perfil", "ALUNO")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        this.exigirSemGravacao();
    }

    @Test
    public void perfilDesconhecidoNaoAlteraEstoque() throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque").header("X-Perfil", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        this.exigirSemGravacao();
    }

    @Test
    public void materialInexistenteNaoGrava() throws Exception {
        this.api.perform(patch("/api/materiais/999/estoque").header("X-Perfil", "RESPONSAVEL")
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
        this.api.perform(patch("/api/materiais/7/estoque").header("X-Perfil", " responsavel ")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":24}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId").value(7))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(24))
                .andExpect(jsonPath("$.atualizadaEm").isString())
                .andExpect(jsonPath("$.nome").doesNotExist());
        verify(this.repositorio).save(this.madeira);
        assertEquals(24, this.madeira.getQuantidadeDisponivel());
    }

    @Test
    public void zeroEValidoECatalogoPassaAMostrarIndisponivel() throws Exception {
        List<Material> materiais = new ArrayList<Material>();
        materiais.add(this.madeira);
        when(this.repositorio.findByAtivoTrueOrderByNomeAsc()).thenReturn(materiais);
        this.api.perform(patch("/api/materiais/7/estoque").header("X-Perfil", "RESPONSAVEL")
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidadeDisponivel\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeDisponivel").value(0));
        this.api.perform(get("/api/materiais"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantidadeDisponivel").value(0))
                .andExpect(jsonPath("$[0].statusEstoque").value("INDISPONIVEL"));
        verify(this.repositorio).save(this.madeira);
    }

    @Test
    public void falhaDeGravacaoNaoRetornaSucessoNemDetalheInterno() throws Exception {
        when(this.repositorio.save(this.madeira)).thenThrow(new RuntimeException("senha do banco"));
        this.api.perform(patch("/api/materiais/7/estoque").header("X-Perfil", "RESPONSAVEL")
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
            servico.atualizarEstoque("RESPONSAVEL", 7L, null);
            fail("Entrada nula não podia alterar o estoque.");
        } catch (NegocioException erro) {
            assertEquals("QUANTIDADE_INVALIDA", erro.getCodigo());
        }
        this.exigirSemGravacao();
    }

    private void exigirQuantidadeInvalida(String corpo) throws Exception {
        this.api.perform(patch("/api/materiais/7/estoque").header("X-Perfil", "RESPONSAVEL")
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("QUANTIDADE_INVALIDA"));
    }

    private void exigirSemGravacao() {
        verify(this.repositorio, never()).save(any(Material.class));
        assertEquals(10, this.madeira.getQuantidadeDisponivel());
    }
}
