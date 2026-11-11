package com.archflow.comum;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

// Diz quem esta chamando a API, a partir do token que a tela recebeu no login.
public class LeitorDeToken {

    private static final String PREFIXO_BEARER = "Bearer ";

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

    // Token com assinatura errada ou vencido cai aqui.
    private Jwt abrir(String token) {
        try {
            return this.decodificador.decode(token);
        } catch (JwtException erro) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "Token de acesso inválido ou expirado.");
        }
    }

    // Token sem perfil, ou com perfil desconhecido, nao vale.
    private PerfilUsuario lerPerfil(Jwt token) {
        String perfil = token.getClaimAsString(EmissorDeToken.CAMPO_PERFIL);
        if (PerfilUsuario.RESPONSAVEL.name().equals(perfil)) {
            return PerfilUsuario.RESPONSAVEL;
        }
        if (PerfilUsuario.ALUNO.name().equals(perfil)) {
            return PerfilUsuario.ALUNO;
        }
        throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                "Token de acesso inválido ou expirado.");
    }

    private Long lerUsuarioId(Jwt token) {
        Object valor = token.getClaim(EmissorDeToken.CAMPO_USUARIO_ID);
        if (valor instanceof Number) {
            return this.exigirNumeroDeUsuario(Long.valueOf(((Number) valor).longValue()));
        }
        if (valor instanceof String) {
            return this.converterUsuarioId((String) valor);
        }
        throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                "Token de acesso inválido ou expirado.");
    }

    private Long converterUsuarioId(String valor) {
        try {
            return this.exigirNumeroDeUsuario(Long.valueOf(valor.trim()));
        } catch (NumberFormatException erro) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "Token de acesso inválido ou expirado.");
        }
    }

    private Long exigirNumeroDeUsuario(Long usuarioId) {
        if (usuarioId.longValue() <= 0) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "Token de acesso inválido ou expirado.");
        }
        return usuarioId;
    }
}
