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
//
// Roda antes dos controladores: quem chega sem token valido nem chega na regra
// de negocio. O que passa fica guardado no pedido, no atributo
// usuarioAutenticado, e cada servico decide dali se aquele perfil pode fazer
// aquela operacao.
public class FiltroDeIdentidade implements Filter {

    public static final String ATRIBUTO_IDENTIDADE = "usuarioAutenticado";

    private static final String METODO_DE_PRE_VOO = "OPTIONS";

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
        // O navegador manda um OPTIONS sem token antes da chamada de verdade,
        // so para perguntar se a chamada e permitida. Recusar esse OPTIONS
        // faria o navegador cancelar a chamada antes de enviar o token.
        if (METODO_DE_PRE_VOO.equalsIgnoreCase(pedidoHttp.getMethod())) {
            corrente.doFilter(pedido, resposta);
            return;
        }
        if (!this.identificar(pedidoHttp, respostaHttp)) {
            return;
        }
        corrente.doFilter(pedido, resposta);
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

    // A recusa sai no mesmo formato de erro do resto da API, para a tela
    // mostrar a mensagem em vez de uma pagina de erro do servidor.
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

    // A liberacao de CORS do resto da API acontece depois deste filtro. Sem
    // repetir o cabecalho aqui, o navegador bloqueia a leitura da recusa e a
    // tela nao consegue mostrar por que o acesso foi negado.
    private void liberarLeituraPelaTela(HttpServletRequest pedido, HttpServletResponse resposta) {
        String origem = pedido.getHeader("Origin");
        if (origem == null || !ConfiguracaoDeCors.eTelaDoArchflow(origem)) {
            return;
        }
        resposta.setHeader("Access-Control-Allow-Origin", origem);
        resposta.setHeader("Vary", "Origin");
    }
}
