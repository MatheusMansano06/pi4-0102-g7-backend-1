package com.archflow.notificacao;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archflow.comum.FiltroDeIdentidade;
import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.util.ReflectionTestUtils;

// A identidade interna e a consulta são fixtures. Este teste não liga PostgreSQL.
@WebMvcTest(NotificacaoController.class)
@Import(NotificacaoService.class)
public class NotificacaoConsultaTest {

    @Autowired
    private MockMvc api;

    @MockBean
    private NotificacaoRepository consulta;

    @Test
    public void semFiltroDevolveLidasENaoLidasComCamposDoContrato() throws Exception {
        List<Notificacao> avisos = this.listaCom(this.aviso(55L, false, 101L));
        avisos.add(this.aviso(56L, true, null));
        when(this.consulta.findByUsuarioIdOrderByCriadoEmDesc(12L)).thenReturn(avisos);
        this.api.perform(this.pedido(12L, PerfilUsuario.ALUNO)).andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacoes.length()").value(2))
                .andExpect(jsonPath("$.notificacoes[0].notificacaoId").value(55))
                .andExpect(jsonPath("$.notificacoes[0].tipo").value("PEDIDO_PRONTO"))
                .andExpect(jsonPath("$.notificacoes[0].mensagem").value("Seu pedido está pronto."))
                .andExpect(jsonPath("$.notificacoes[0].criadaEm").value("2026-10-13T12:15:10"))
                .andExpect(jsonPath("$.notificacoes[0].lida").value(false))
                .andExpect(jsonPath("$.notificacoes[0].solicitacaoId").value(101))
                .andExpect(jsonPath("$.notificacoes[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$.notificacoes[1].lida").value(true))
                .andExpect(jsonPath("$.notificacoes[1].solicitacaoId").value(nullValue()));
        verify(this.consulta).findByUsuarioIdOrderByCriadoEmDesc(12L);
    }

    @Test
    public void solicitaSomenteNaoLidasParaOUsuarioDaIdentidade() throws Exception {
        when(this.consulta.findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(12L)).thenReturn(this.listaCom(this.aviso(55L, false, 101L)));
        this.api.perform(this.pedido(12L, PerfilUsuario.ALUNO).param("somenteNaoLidas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacoes.length()").value(1))
                .andExpect(jsonPath("$.notificacoes[0].lida").value(false));
        verify(this.consulta).findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(12L);
    }

    @Test
    public void outroUsuarioTemSuaPropriaConsultaMesmoQuandoEResponsavel() throws Exception {
        when(this.consulta.findByUsuarioIdOrderByCriadoEmDesc(23L)).thenReturn(this.listaCom(this.aviso(92L, true, null)));
        this.api.perform(this.pedido(23L, PerfilUsuario.RESPONSAVEL)
                .param("usuarioId", "12").param("somenteNaoLidas", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.notificacoes.length()").value(1))
                .andExpect(jsonPath("$.notificacoes[0].notificacaoId").value(92));
        verify(this.consulta).findByUsuarioIdOrderByCriadoEmDesc(23L);
    }

    @Test
    public void consultaSemResultadoDevolveListaVazia() throws Exception {
        when(this.consulta.findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(12L)).thenReturn(new ArrayList<Notificacao>());
        this.api.perform(this.pedido(12L, PerfilUsuario.ALUNO).param("somenteNaoLidas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacoes").isArray())
                .andExpect(jsonPath("$.notificacoes").isEmpty());
    }

    @Test
    public void semIdentidadeNaoConsulta() throws Exception {
        this.api.perform(get("/api/notificacoes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        verifyNoInteractions(this.consulta);
    }

    @Test
    public void filtroInvalidoNaoConsulta() throws Exception {
        this.api.perform(this.pedido(12L, PerfilUsuario.ALUNO).param("somenteNaoLidas", "talvez"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));
        verifyNoInteractions(this.consulta);
    }

    @Test
    public void falhaDaConsultaNaoExpoeDetalhesInternos() throws Exception {
        when(this.consulta.findByUsuarioIdOrderByCriadoEmDesc(12L)).thenThrow(new IllegalStateException("detalhe interno"));
        this.api.perform(this.pedido(12L, PerfilUsuario.ALUNO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.mensagem").value("Erro interno no servidor."));
    }

    private MockHttpServletRequestBuilder pedido(Long usuarioId, PerfilUsuario perfil) {
        return get("/api/notificacoes").requestAttr(FiltroDeIdentidade.ATRIBUTO_IDENTIDADE,
                new UsuarioAutenticado(usuarioId, perfil));
    }

    private Notificacao aviso(Long id, boolean lida, Long solicitacaoId) {
        Notificacao aviso = new Notificacao(12L, solicitacaoId, "PEDIDO_PRONTO", "Seu pedido está pronto.");
        ReflectionTestUtils.setField(aviso, "notificacaoId", id);
        ReflectionTestUtils.setField(aviso, "criadoEm", LocalDateTime.of(2026, 10, 13, 12, 15, 10));
        ReflectionTestUtils.setField(aviso, "lida", lida);
        return aviso;
    }

    private List<Notificacao> listaCom(Notificacao aviso) {
        List<Notificacao> avisos = new ArrayList<Notificacao>();
        avisos.add(aviso);
        return avisos;
    }
}
