package com.archflow.notificacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

// Usa a consulta de referência apenas em uma tabela temporária de teste.
@EnabledIfEnvironmentVariable(named = "ARCHFLOW_CT07_TESTE_URL",
        matches = "jdbc:postgresql://(localhost|127[.]0[.]0[.]1):[0-9]+/archflow_ct07_teste_[a-z0-9_]+")
public class ConsultaNotificacoesBancoTest {

    private Connection conexao;
    private NotificacaoService servico;

    @BeforeEach
    public void prepararTabelaTemporaria() throws SQLException {
        this.conexao = DriverManager.getConnection(System.getenv("ARCHFLOW_CT07_TESTE_URL"),
                System.getenv("ARCHFLOW_CT07_TESTE_USUARIO"), System.getenv("ARCHFLOW_CT07_TESTE_SENHA"));
        this.conexao.setAutoCommit(false);
        EncodedResource dados = new EncodedResource(
                new ClassPathResource("ct07/avisos-temporarios.sql"), StandardCharsets.UTF_8);
        ScriptUtils.executeSqlScript(this.conexao, dados);
        JdbcTemplate sql = new JdbcTemplate(new SingleConnectionDataSource(this.conexao, true));
        this.servico = new NotificacaoService(new ConsultaDeNotificacoesTemporaria(sql));
    }

    @AfterEach
    public void fecharSessaoDeTeste() throws SQLException {
        if (this.conexao != null) {
            try {
                this.conexao.rollback();
            } finally {
                this.conexao.close();
            }
        }
    }

    @Test
    public void cadaPerfilRecebeSomenteSeusAvisos() {
        List<NotificacaoSaida> aluno = this.listar(12L, PerfilUsuario.ALUNO, null);
        assertEquals(2, aluno.size());
        assertFalse(this.localizar(aluno, 55L).isLida());
        assertTrue(this.localizar(aluno, 56L).isLida());
        List<NotificacaoSaida> responsavel = this.listar(23L, PerfilUsuario.RESPONSAVEL, "false");
        assertEquals(2, responsavel.size());
        assertFalse(this.localizar(responsavel, 92L).isLida());
        assertTrue(this.localizar(responsavel, 93L).isLida());
    }

    @Test
    public void filtroDeNaoLidasMantemDestinatarioENaoMarcaAvisoComoLido() {
        List<NotificacaoSaida> aluno = this.listar(12L, PerfilUsuario.ALUNO, "true");
        List<NotificacaoSaida> responsavel = this.listar(23L, PerfilUsuario.RESPONSAVEL, "true");
        this.conferirUnicoAvisoNaoLido(aluno, 55L);
        this.conferirUnicoAvisoNaoLido(responsavel, 92L);
        assertFalse(this.localizar(this.listar(12L, PerfilUsuario.ALUNO, "false"), 55L).isLida());
    }

    @Test
    public void usuarioSemAvisosRecebeListaVaziaMesmoComAvisosDeOutros() {
        assertTrue(this.listar(31L, PerfilUsuario.ALUNO, null).isEmpty());
        assertTrue(this.listar(31L, PerfilUsuario.ALUNO, "true").isEmpty());
    }

    @Test
    public void consultaPreservaOsCamposEAReferenciaOpcional() {
        List<NotificacaoSaida> avisos = this.listar(12L, PerfilUsuario.ALUNO, null);
        NotificacaoSaida aviso = this.localizar(avisos, 55L);
        assertEquals("PEDIDO_PRONTO", aviso.getTipo());
        assertEquals("Pedido disponível para retirada", aviso.getMensagem());
        assertEquals(LocalDateTime.of(2026, 10, 13, 12, 15, 10), aviso.getCriadaEm());
        assertEquals(101L, aviso.getSolicitacaoId());
        assertNull(this.localizar(avisos, 56L).getSolicitacaoId());
    }

    private List<NotificacaoSaida> listar(Long usuarioId, PerfilUsuario perfil, String filtro) {
        return this.servico.listarNotificacoes(new UsuarioAutenticado(usuarioId, perfil), filtro).getNotificacoes();
    }

    private void conferirUnicoAvisoNaoLido(List<NotificacaoSaida> avisos, Long id) {
        assertEquals(1, avisos.size());
        assertFalse(this.localizar(avisos, id).isLida());
    }

    private NotificacaoSaida localizar(List<NotificacaoSaida> avisos, Long id) {
        for (NotificacaoSaida aviso : avisos) {
            if (id.equals(aviso.getNotificacaoId())) {
                return aviso;
            }
        }
        throw new AssertionError("Aviso esperado não encontrado: " + id);
    }
}
