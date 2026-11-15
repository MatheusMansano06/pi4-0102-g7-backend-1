package com.archflow.usuario;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.EmissorDeToken;
import com.archflow.comum.NegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// A operacao cadastrarAluno do CT14: cria a conta do aluno pela tela e ja
// devolve o token, para a pessoa nao ter que entrar de novo logo depois.
// Esta rota so cria perfil ALUNO. Conta de responsavel nasce pelo script do
// banco, para ninguem virar responsavel pela tela publica.
@Service
public class CadastroAlunoService {

    // O banco guarda o hash em VARCHAR(255) e o BCrypt so le os 72 primeiros
    // bytes da senha, entao o limite evita um truncamento silencioso.
    private static final int TAMANHO_MINIMO_DA_SENHA = 8;

    private static final int TAMANHO_MAXIMO_DA_SENHA = 72;

    private final UsuarioRepository repositorio;

    private final PasswordEncoder conferenteDeSenha;

    private final EmissorDeToken emissor;

    public CadastroAlunoService(UsuarioRepository repositorio, PasswordEncoder conferenteDeSenha,
            EmissorDeToken emissor) {
        this.repositorio = repositorio;
        this.conferenteDeSenha = conferenteDeSenha;
        this.emissor = emissor;
    }

    public AutenticacaoSaida cadastrarAluno(CadastroAlunoEntrada entrada) {
        // 1) Todos os campos da ficha sao obrigatorios.
        this.exigirCamposPreenchidos(entrada);
        // 2) A senha precisa do tamanho que o contrato pede.
        this.exigirSenhaAceitavel(entrada.getSenha());
        // 3) E-mail e RA sao unicos no banco; a recusa aqui e mais clara que
        //    a violacao do indice.
        this.exigirEmailLivre(entrada.getEmail());
        this.exigirRaLivre(entrada.getRa());
        // 4) Grava so o hash. A senha digitada nunca vai para o banco.
        Usuario novo = this.gravar(entrada);
        // 5) Devolve o mesmo corpo do CT01, com o token ja emitido.
        return this.sessaoRecemCriada(novo);
    }

    private AutenticacaoSaida sessaoRecemCriada(Usuario novo) {
        String token = this.emissor.emitirPara(novo.getUsuarioId(), novo.getPerfil(),
                novo.getEmail());
        return new AutenticacaoSaida(novo, token);
    }

    private Usuario gravar(CadastroAlunoEntrada entrada) {
        String hash = this.conferenteDeSenha.encode(entrada.getSenha());
        Usuario novo = new Usuario(entrada.getNome(), entrada.getEmail(), hash,
                entrada.getRa(), entrada.getCurso());
        return this.repositorio.save(novo);
    }

    private void exigirCamposPreenchidos(CadastroAlunoEntrada entrada) {
        if (entrada == null || this.vazio(entrada.getNome()) || this.vazio(entrada.getRa())
                || this.vazio(entrada.getCurso()) || this.vazio(entrada.getEmail())
                || this.vazio(entrada.getSenha())) {
            throw new NegocioException(CodigosDeErro.DADOS_INVALIDOS,
                    "Informe nome, RA, curso, e-mail e senha.");
        }
    }

    private void exigirSenhaAceitavel(String senha) {
        if (senha.length() < TAMANHO_MINIMO_DA_SENHA) {
            throw new NegocioException(CodigosDeErro.SENHA_FRACA,
                    "A senha precisa de pelo menos " + TAMANHO_MINIMO_DA_SENHA + " caracteres.");
        }
        if (senha.length() > TAMANHO_MAXIMO_DA_SENHA) {
            throw new NegocioException(CodigosDeErro.SENHA_FRACA,
                    "A senha pode ter no máximo " + TAMANHO_MAXIMO_DA_SENHA + " caracteres.");
        }
    }

    private void exigirEmailLivre(String email) {
        if (this.repositorio.findByEmailIgnoreCase(email.trim()) != null) {
            throw new NegocioException(CodigosDeErro.EMAIL_JA_CADASTRADO,
                    "Já existe uma conta com esse e-mail.");
        }
    }

    private void exigirRaLivre(String ra) {
        if (this.repositorio.findByRa(ra.trim()) != null) {
            throw new NegocioException(CodigosDeErro.RA_JA_CADASTRADO,
                    "Já existe uma conta com esse RA.");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
