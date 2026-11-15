package com.archflow.notificacao;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archflow.comum.ConfiguracaoDeIdentidade;
import com.archflow.comum.EmissorDeToken;
import com.archflow.comum.PerfilUsuario;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

// Filtro, assinatura e leitura JWT reais; a consulta permanece simulada.
// Não comprova adaptador de banco nem avisos produzidos pelo H09.1.
@WebMvcTest(value = NotificacaoController.class, properties = {
        "archflow.token.segredo=segredo-exclusivo-da-prova-ct07-com-jwt-2026"
})
@Import({NotificacaoService.class, ConfiguracaoDeIdentidade.class})
public class NotificacaoTokenTest {

    @Autowired
    private MockMvc api;

    @Autowired
    private EmissorDeToken emissor;

    @Autowired
    private JwtEncoder assinador;

    @MockBean
    private NotificacaoRepository consulta;

    @Test
    public void alunoConsultaSomenteDestinatarioDoToken() throws Exception {
        when(this.consulta.findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(12L)).thenReturn(this.avisos(55L));
        this.api.perform(get("/api/notificacoes")
                .header("Authorization", this.bearer(12L, PerfilUsuario.ALUNO))
                .header("X-Perfil", "RESPONSAVEL")
                .param("usuarioId", "23").param("somenteNaoLidas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacoes[0].notificacaoId").value(55))
                .andExpect(jsonPath("$.notificacoes[0].usuarioId").doesNotExist());
        verify(this.consulta).findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(12L);
    }

    @Test
    public void responsavelTambemConsultaSomenteDestinatarioDoToken() throws Exception {
        when(this.consulta.findByUsuarioIdOrderByCriadoEmDesc(23L)).thenReturn(this.avisos(92L));
        this.api.perform(get("/api/notificacoes")
                .header("Authorization", this.bearer(23L, PerfilUsuario.RESPONSAVEL))
                .param("usuarioId", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacoes[0].notificacaoId").value(92));
        verify(this.consulta).findByUsuarioIdOrderByCriadoEmDesc(23L);
    }

    @Test
    public void cabecalhoDePerfilSemTokenNaoAutentica() throws Exception {
        this.api.perform(get("/api/notificacoes").header("X-Perfil", "RESPONSAVEL"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        verifyNoInteractions(this.consulta);
    }

    @Test
    public void tokenMalformadoNaoConsulta() throws Exception {
        this.api.perform(get("/api/notificacoes").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        verifyNoInteractions(this.consulta);
    }

    @Test
    public void tokenExpiradoNaoConsulta() throws Exception {
        Instant agora = Instant.now();
        String token = this.assinar(this.assinador, agora.minusSeconds(7200), agora.minusSeconds(3600));
        this.api.perform(get("/api/notificacoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        verifyNoInteractions(this.consulta);
    }

    @Test
    public void assinaturaDeOutraChaveNaoConsulta() throws Exception {
        ConfiguracaoDeIdentidade configuracao = new ConfiguracaoDeIdentidade();
        JwtEncoder outroAssinador = configuracao.assinadorDeToken(new SecretKeySpec(
                "outra-chave-exclusiva-da-prova-ct07-2026".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                "HmacSHA256"));
        Instant agora = Instant.now();
        String token = this.assinar(outroAssinador, agora, agora.plusSeconds(3600));
        this.api.perform(get("/api/notificacoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
        verifyNoInteractions(this.consulta);
    }

    @Test
    public void filtroInvalidoComTokenValidoNaoConsulta() throws Exception {
        this.api.perform(get("/api/notificacoes")
                .header("Authorization", this.bearer(12L, PerfilUsuario.ALUNO))
                .param("somenteNaoLidas", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));
        verifyNoInteractions(this.consulta);
    }

    private String bearer(Long usuarioId, PerfilUsuario perfil) {
        return "Bearer " + this.emissor.emitirPara(usuarioId, perfil, "fixture@exemplo.test");
    }

    private String assinar(JwtEncoder encoder, Instant inicio, Instant fim) {
        JwtClaimsSet dados = JwtClaimsSet.builder().issuer("archflow").subject("fixture@exemplo.test")
                .issuedAt(inicio).expiresAt(fim).claim("usuarioId", 12L).claim("perfil", "ALUNO").build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), dados)).getTokenValue();
    }

    private List<Notificacao> avisos(Long id) {
        List<Notificacao> lista = new ArrayList<Notificacao>();
        Notificacao aviso = new Notificacao(12L, null, "PEDIDO_PRONTO", "Seu pedido está pronto.");
        ReflectionTestUtils.setField(aviso, "notificacaoId", id);
        lista.add(aviso);
        return lista;
    }
}
