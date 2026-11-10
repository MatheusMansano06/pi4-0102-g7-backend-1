package com.archflow.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archflow.comum.EmissorDeToken;
import com.archflow.comum.PerfilUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

// O serviço conclui sua própria transação; a releitura é posterior à resposta HTTP.
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "ARCHFLOW_CT05_TESTE_URL",
        matches = "jdbc:postgresql://(localhost|127[.]0[.]0[.]1):[0-9]+/archflow_ct05_teste_[a-z0-9_]+")
@TestPropertySource(properties = {
        "archflow.estoque.em-desenvolvimento=true",
        "spring.datasource.url=${ARCHFLOW_CT05_TESTE_URL}",
        "spring.datasource.username=${ARCHFLOW_CT05_TESTE_USUARIO}",
        "spring.datasource.password=${ARCHFLOW_CT05_TESTE_SENHA}",
        "archflow.token.segredo=${ARCHFLOW_CT05_TESTE_SEGREDO}"
})
public class MaterialEstoqueBancoTest {

    @Autowired
    private MockMvc api;

    @Autowired
    private MaterialRepository materiais;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private ObjectMapper json;

    private Material material;

    @BeforeEach
    public void prepararMaterial() {
        this.material = this.materiais.saveAndFlush(new Material("Fixture CT05", "Chapa", "12 mm", 10, 2));
    }

    @AfterEach
    public void removerSomenteMaterialDeTeste() {
        if (this.material != null) {
            this.materiais.deleteById(this.material.getMaterialId());
        }
    }

    @Test
    public void responsavelGravaSaldoEDataReconsultadosDoBanco() throws Exception {
        assertNull(this.relerMaterial().getAtualizadaEm());
        MvcResult resposta = this.atualizarComPerfil(PerfilUsuario.RESPONSAVEL, 24)
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantidadeDisponivel").value(24)).andReturn();
        LocalDateTime dataRespondida = this.lerData(resposta);
        Material gravado = this.relerMaterial();
        assertEquals(24, gravado.getQuantidadeDisponivel());
        assertEquals(dataRespondida, gravado.getAtualizadaEm());
    }

    @Test
    public void recusasMantemSaldoEDataJaGravados() throws Exception {
        LocalDateTime anterior = LocalDateTime.of(2026, 10, 13, 12, 5, 7);
        this.material.atualizarEstoque(10, anterior);
        this.material = this.materiais.saveAndFlush(this.material);
        this.api.perform(this.pedido(24)).andExpect(status().isUnauthorized());
        this.atualizarComPerfil(PerfilUsuario.ALUNO, 24).andExpect(status().isForbidden());
        this.atualizarComPerfil(PerfilUsuario.RESPONSAVEL, -1).andExpect(status().isBadRequest());
        Material gravado = this.relerMaterial();
        assertEquals(10, gravado.getQuantidadeDisponivel());
        assertEquals(anterior, gravado.getAtualizadaEm());
    }

    private ResultActions atualizarComPerfil(PerfilUsuario perfil, int quantidade) throws Exception {
        // Identidades de teste, com o mesmo emissor e validador da aplicação.
        String token = this.emissor.emitirPara(9L, perfil, "ct05@example.test");
        return this.api.perform(this.pedido(quantidade).header("Authorization", "Bearer " + token));
    }

    private MockHttpServletRequestBuilder pedido(int quantidade) {
        return patch("/api/materiais/{materialId}/estoque", this.material.getMaterialId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantidadeDisponivel\":" + quantidade + "}");
    }

    private Material relerMaterial() {
        Material encontrado = this.materiais.findByMaterialId(this.material.getMaterialId());
        assertNotNull(encontrado);
        return encontrado;
    }

    private LocalDateTime lerData(MvcResult resposta) throws Exception {
        String data = this.json.readTree(resposta.getResponse().getContentAsByteArray())
                .path("atualizadaEm").asText();
        return LocalDateTime.parse(data);
    }
}
