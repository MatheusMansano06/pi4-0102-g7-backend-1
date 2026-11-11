package com.archflow.usuario;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.ErroSaida;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Rota publica: e a unica da API que nao exige token.
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService servico;

    public AutenticacaoController(AutenticacaoService servico) {
        this.servico = servico;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AutenticacaoSaida entrar(@RequestBody AutenticacaoEntrada entrada) {
        return this.servico.autenticar(entrada);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroSaida tratarCorpoInvalido(HttpMessageNotReadableException erro) {
        return new ErroSaida(CodigosDeErro.DADOS_INVALIDOS,
                "Informe um objeto JSON com e-mail e senha.");
    }
}
