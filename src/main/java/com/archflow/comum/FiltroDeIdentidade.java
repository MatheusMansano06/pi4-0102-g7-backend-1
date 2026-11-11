package com.archflow.comum;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

// Confere a identidade antes de qualquer operacao da API (H02.2).
public class FiltroDeIdentidade implements Filter {

    public static final String ATRIBUTO_IDENTIDADE = "usuarioAutenticado";

    private static final String METODO_DE_PRE_VOO = "OPTIONS";

    // O login nao exige token: o token so existe depois de entrar.
    private static final String ROTA_DE_LOGIN = "/api/auth/";

    private final LeitorDeToken leitor;

    private final ObjectMapper conversorDeJson;

    public FiltroDeIdentidade(LeitorDeToken leitor, ObjectMapper conversorDeJson) {
        this.leitor = leitor;
        this.conversorDeJson = conversorDeJson;
    }

    @Override
    public void doFilter(ServletRequest pedido, ServletResponse resposta, FilterChain corrente)
            throws IOException, ServletException {
        HttpServletRequest pedidoHttp = (HttpServletRequest) pedido;
        HttpServletResponse respostaHttp = (HttpServletResponse) resposta;
        // O OPTIONS que o navegador manda antes da chamada vai sem token.
        if (METODO_DE_PRE_VOO.equalsIgnoreCase(pedidoHttp.getMethod())) {
            corrente.doFilter(pedido, resposta);
            return;
        }
        if (this.eRotaPublica(pedidoHttp)) {
            corrente.doFilter(pedido, resposta);
            return;
        }
        if (!this.identificar(pedidoHttp, respostaHttp)) {
            return;
        }
        corrente.doFilter(pedido, resposta);
    }

    private boolean eRotaPublica(HttpServletRequest pedido) {
        String caminho = pedido.getRequestURI();
        return caminho != null && caminho.startsWith(ROTA_DE_LOGIN);
    }

    private boolean identificar(HttpServletRequest pedido, HttpServletResponse resposta)
            throws IOException {
        try {
            pedido.setAttribute(ATRIBUTO_IDENTIDADE,
                    this.leitor.ler(pedido.getHeader("Authorization")));
            return true;
        } catch (NegocioException erro) {
            this.recusar(pedido, resposta, erro);
            return false;
        }
    }

    private void recusar(HttpServletRequest pedido, HttpServletResponse resposta,
            NegocioException erro) throws IOException {
        this.liberarLeituraPelaTela(pedido, resposta);
        resposta.setStatus(this.escolherStatus(erro).value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        resposta.getWriter().write(this.conversorDeJson
                .writeValueAsString(new ErroSaida(erro.getCodigo(), erro.getMessage())));
    }

    private HttpStatus escolherStatus(NegocioException erro) {
        if (erro instanceof NaoAutenticadoException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (erro instanceof AcessoNegadoException) {
            return HttpStatus.FORBIDDEN;
        }
        if (erro instanceof ConfiguracaoPendenteException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private void liberarLeituraPelaTela(HttpServletRequest pedido, HttpServletResponse resposta) {
        String origem = pedido.getHeader("Origin");
        if (origem == null || !ConfiguracaoDeCors.eTelaDoArchflow(origem)) {
            return;
        }
        resposta.setHeader("Access-Control-Allow-Origin", origem);
        resposta.setHeader("Vary", "Origin");
    }
}
