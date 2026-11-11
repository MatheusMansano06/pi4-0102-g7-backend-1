package com.archflow.usuario;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.EmissorDeToken;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// A operacao autenticar do CT01 (H02.1): confere e-mail e senha e devolve o
// perfil e o token. E-mail que nao existe e senha errada dao a mesma resposta,
// para nao dizer quais e-mails tem conta.
@Service
public class AutenticacaoService {

    private final UsuarioRepository repositorio;

    private final PasswordEncoder conferenteDeSenha;

    private final EmissorDeToken emissor;

    public AutenticacaoService(UsuarioRepository repositorio, PasswordEncoder conferenteDeSenha,
            EmissorDeToken emissor) {
        this.repositorio = repositorio;
        this.conferenteDeSenha = conferenteDeSenha;
        this.emissor = emissor;
    }

    public AutenticacaoSaida autenticar(AutenticacaoEntrada entrada) {
        this.exigirEmailESenha(entrada);
        Usuario usuario = this.repositorio.findByEmail(entrada.getEmail().trim());
        this.exigirSenhaCorreta(usuario, entrada.getSenha());
        this.exigirContaAtiva(usuario);
        String token = this.emissor.emitirPara(usuario.getUsuarioId(), usuario.getPerfil(),
                usuario.getEmail());
        return new AutenticacaoSaida(usuario, token);
    }

    private void exigirEmailESenha(AutenticacaoEntrada entrada) {
        if (entrada == null || this.vazio(entrada.getEmail()) || this.vazio(entrada.getSenha())) {
            throw new NegocioException(CodigosDeErro.DADOS_INVALIDOS,
                    "Informe o e-mail e a senha.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void exigirSenhaCorreta(Usuario usuario, String senha) {
        if (usuario == null) {
            throw new NaoAutenticadoException(CodigosDeErro.CREDENCIAIS_INVALIDAS,
                    "E-mail ou senha incorretos.");
        }
        if (!this.conferenteDeSenha.matches(senha, usuario.getSenhaHash())) {
            throw new NaoAutenticadoException(CodigosDeErro.CREDENCIAIS_INVALIDAS,
                    "E-mail ou senha incorretos.");
        }
    }

    private void exigirContaAtiva(Usuario usuario) {
        if (!usuario.isAtivo()) {
            throw new AcessoNegadoException(CodigosDeErro.USUARIO_INATIVO,
                    "Esta conta está inativa. Procure o responsável pelo laboratório.");
        }
    }
}
