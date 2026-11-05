package com.archflow.comum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

// Diz quem esta chamando a API, a partir do token que a tela recebeu do Keycloak.
//
// A assinatura e a validade do token sao conferidas com as chaves publicas do
// proprio Keycloak: token inventado, alterado ou vencido nao passa daqui. O
// perfil sai dos papeis do realm (aluno ou responsavel) e o numero do aluno sai
// do campo usuarioId, que o Keycloak coloca no token.
public class LeitorDeToken {

    private static final String PREFIXO_BEARER = "Bearer ";

    private static final String CAMPO_PAPEIS_DO_REALM = "realm_access";

    private static final String CAMPO_PAPEIS = "roles";

    private static final String CAMPO_USUARIO_ID = "usuarioId";

    private static final String PAPEL_ALUNO = "aluno";

    private static final String PAPEL_RESPONSAVEL = "responsavel";

    private final JwtDecoder decodificador;

    public LeitorDeToken(JwtDecoder decodificador) {
        this.decodificador = decodificador;
    }

    public UsuarioAutenticado ler(String cabecalhoAuthorization) {
        Jwt token = this.abrir(this.extrairToken(cabecalhoAuthorization));
        return new UsuarioAutenticado(this.lerUsuarioId(token), this.lerPerfil(token));
    }

    // Sem cabecalho Authorization ninguem se identificou: e 401, nao 403.
    private String extrairToken(String cabecalhoAuthorization) {
        if (cabecalhoAuthorization == null || !cabecalhoAuthorization.startsWith(PREFIXO_BEARER)) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "Envie o token de acesso no cabeçalho Authorization.");
        }
        String token = cabecalhoAuthorization.substring(PREFIXO_BEARER.length()).trim();
        if (token.isEmpty()) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "Envie o token de acesso no cabeçalho Authorization.");
        }
        return token;
    }

    // Token com assinatura errada, vencido ou de outro realm cai aqui.
    private Jwt abrir(String token) {
        try {
            return this.decodificador.decode(token);
        } catch (JwtException erro) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "Token de acesso inválido ou expirado.");
        }
    }

    // O responsavel vem antes do aluno: quem tem os dois papeis atende a fila.
    private PerfilUsuario lerPerfil(Jwt token) {
        if (this.temPapel(token, PAPEL_RESPONSAVEL)) {
            return PerfilUsuario.RESPONSAVEL;
        }
        if (this.temPapel(token, PAPEL_ALUNO)) {
            return PerfilUsuario.ALUNO;
        }
        throw new AcessoNegadoException(CodigosDeErro.ACESSO_NEGADO,
                "A conta não tem perfil de aluno nem de responsável no ArchFlow.");
    }

    private boolean temPapel(Jwt token, String papel) {
        List<?> papeis = this.lerPapeis(token);
        int posicao = 0;
        while (posicao < papeis.size()) {
            if (papel.equals(papeis.get(posicao))) {
                return true;
            }
            posicao = posicao + 1;
        }
        return false;
    }

    private List<?> lerPapeis(Jwt token) {
        Map<String, Object> acessoAoRealm = token.getClaimAsMap(CAMPO_PAPEIS_DO_REALM);
        if (acessoAoRealm == null) {
            return new ArrayList<Object>();
        }
        Object papeis = acessoAoRealm.get(CAMPO_PAPEIS);
        if (papeis instanceof List) {
            return (List<?>) papeis;
        }
        return new ArrayList<Object>();
    }

    // O pedido e o historico do aluno sao gravados por numero de usuario. Sem
    // esse numero no token o backend nao tem como saber de quem e o pedido, e
    // isso e falta de configuracao do Keycloak, nao erro de quem chamou.
    private Long lerUsuarioId(Jwt token) {
        Object valor = token.getClaim(CAMPO_USUARIO_ID);
        if (valor instanceof Number) {
            return this.exigirNumeroDeUsuario(Long.valueOf(((Number) valor).longValue()));
        }
        if (valor instanceof String) {
            return this.converterUsuarioId((String) valor);
        }
        throw new ConfiguracaoPendenteException(
                "O Keycloak não está enviando o usuarioId no token. Configure o mapeamento no realm.");
    }

    private Long converterUsuarioId(String valor) {
        try {
            return this.exigirNumeroDeUsuario(Long.valueOf(valor.trim()));
        } catch (NumberFormatException erro) {
            throw new ConfiguracaoPendenteException(
                    "O usuarioId enviado pelo Keycloak não é um número: \"" + valor + "\".");
        }
    }

    private Long exigirNumeroDeUsuario(Long usuarioId) {
        if (usuarioId.longValue() <= 0) {
            throw new ConfiguracaoPendenteException(
                    "O usuarioId enviado pelo Keycloak precisa ser maior que zero.");
        }
        return usuarioId;
    }
}
