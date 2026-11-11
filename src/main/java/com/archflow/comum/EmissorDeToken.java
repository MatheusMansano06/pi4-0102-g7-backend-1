package com.archflow.comum;

import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

// Emite o token que a tela guarda depois do login (CT01).
public class EmissorDeToken {

    public static final String CAMPO_USUARIO_ID = "usuarioId";

    public static final String CAMPO_PERFIL = "perfil";

    private static final String EMISSOR = "archflow";

    // Oito horas cobrem um dia de aula inteiro sem obrigar a entrar de novo.
    private static final Duration VALIDADE = Duration.ofHours(8);

    private final JwtEncoder assinador;

    public EmissorDeToken(JwtEncoder assinador) {
        this.assinador = assinador;
    }

    public String emitirPara(Long usuarioId, PerfilUsuario perfil, String email) {
        Instant agora = Instant.now();
        JwtClaimsSet dados = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(email)
                .issuedAt(agora)
                .expiresAt(agora.plus(VALIDADE))
                .claim(CAMPO_USUARIO_ID, usuarioId)
                .claim(CAMPO_PERFIL, perfil.name())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.assinador.encode(JwtEncoderParameters.from(cabecalho, dados)).getTokenValue();
    }
}
