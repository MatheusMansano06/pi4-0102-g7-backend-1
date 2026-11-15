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

// Rotas publicas: sao as unicas da API que nao exigem token. Quem ainda nao
// tem conta precisa poder criar a dela e entrar.
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService servico;

    private final CadastroAlunoService cadastro;

    public AutenticacaoController(AutenticacaoService servico, CadastroAlunoService cadastro) {
        this.servico = servico;
        this.cadastro = cadastro;
    }

    // CT01: POST /api/auth/login
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AutenticacaoSaida entrar(@RequestBody AutenticacaoEntrada entrada) {
        return this.servico.autenticar(entrada);
    }

    // CT14: POST /api/auth/cadastro
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AutenticacaoSaida cadastrarAluno(@RequestBody CadastroAlunoEntrada entrada) {
        return this.cadastro.cadastrarAluno(entrada);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroSaida tratarCorpoInvalido(HttpMessageNotReadableException erro) {
        return new ErroSaida(CodigosDeErro.DADOS_INVALIDOS,
                "Informe um objeto JSON com os campos que a ficha pede.");
    }
}
