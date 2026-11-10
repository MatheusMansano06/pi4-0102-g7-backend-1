package com.archflow.notificacao;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@WebMvcTest(NotificacaoController.class)
@Import(NotificacaoService.class)
public class NotificacaoDesabilitadaTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    public void configuracaoPadraoNaoExigeConsultaNemRegistraController() {
        assertTrue(this.contexto.getBeansOfType(NotificacaoService.class).isEmpty());
        assertTrue(this.contexto.getBeansOfType(NotificacaoController.class).isEmpty());
    }
}
