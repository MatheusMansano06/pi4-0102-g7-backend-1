package com.archflow.usuario;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.EmissorDeToken;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// A operacao autenticar do CT01 (H02.1): confere e-mail e senha e, dando
// certo, devolve o perfil e o token que a tela usa nas chamadas seguintes.
//
// E-mail que nao existe e senha errada dao a mesma resposta de proposito:
// dizer qual dos dois errou ajudaria quem esta tentando adivinhar conta.
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

    // A conferencia da senha roda mesmo quando o e-mail nao existe, com um
    // hash de mentira, para que errar o e-mail demore o mesmo que errar a
    // senha: a diferenca de tempo entregaria quais e-mails tem conta.
    private void exigirSenhaCorreta(Usuario usuario, String senha) {
        String hashGuardado = "$2a$10$0000000000000000000000000000000000000000000000000000";
        if (usuario != null) {
            hashGuardado = usuario.getSenhaHash();
        }
        boolean senhaBate = this.conferenteDeSenha.matches(senha, hashGuardado);
        if (usuario == null || !senhaBate) {
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
