package com.archflow.solicitacao;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.ErroSaida;
import com.archflow.comum.NegocioException;
import com.archflow.comum.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoCriacaoController {

    private final SolicitacaoCriacaoService servico;

    public SolicitacaoCriacaoController(SolicitacaoCriacaoService servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoCriadaSaida criar(
            @RequestAttribute(name = "usuarioAutenticado", required = false) UsuarioAutenticado identidade,
            @RequestBody SolicitacaoCriacaoEntrada entrada) {
        return this.servico.criar(identidade, entrada);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroSaida tratarCorpoInvalido(HttpMessageNotReadableException erro) {
        Throwable causa = erro;
        while (causa != null) {
            if (causa instanceof NegocioException) {
                NegocioException regra = (NegocioException) causa;
                return new ErroSaida(regra.getCodigo(), regra.getMessage());
            }
            causa = causa.getCause();
        }
        return new ErroSaida(CodigosDeErro.DADOS_INVALIDOS, "Informe um objeto JSON válido para o pedido.");
    }
}
