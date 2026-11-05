package com.archflow.material;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

// Sem a propriedade de desenvolvimento, somente o catálogo deve ser publicado.
@WebMvcTest({MaterialController.class, MaterialEstoqueController.class})
public class MaterialEstoqueDesabilitadoTest {

    @MockBean
    private MaterialService servico;

    @Autowired
    private ApplicationContext contexto;

    @Autowired
    private RequestMappingHandlerMapping rotas;

    @Autowired
    private MockMvc api;

    @Test
    public void configuracaoPadraoNaoRegistraOperacaoDeEstoque() {
        assertFalse(this.contexto.containsBean("materialEstoqueController"));
        for (HandlerMethod operacao : this.rotas.getHandlerMethods().values()) {
            assertFalse(operacao.getMethod().getName().equals("atualizarEstoque"));
        }
        verifyNoInteractions(this.servico);
    }

    @Test
    public void catalogoContinuaDisponivelSemAtivarAlteracaoDeEstoque() throws Exception {
        when(this.servico.listarMateriais(null)).thenReturn(new ArrayList<MaterialSaida>());
        this.api.perform(get("/api/materiais")).andExpect(status().isOk());
    }
}
