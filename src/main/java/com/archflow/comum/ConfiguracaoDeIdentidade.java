package com.archflow.comum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

// Liga a conferencia de identidade nas rotas da API.
@Configuration
public class ConfiguracaoDeIdentidade {

    private static final String ROTAS_DA_API = "/api/*";

    private static final String ALGORITMO_DA_ASSINATURA = "HmacSHA256";

    private static final int TAMANHO_MINIMO_DO_SEGREDO = 32;

    @Value("${archflow.token.segredo}")
    private String segredoDoToken;

    @Bean
    public SecretKey chaveDoToken() {
        byte[] segredo = this.segredoDoToken.getBytes(StandardCharsets.UTF_8);
        if (segredo.length < TAMANHO_MINIMO_DO_SEGREDO) {
            throw new IllegalStateException("A variável ARCHFLOW_TOKEN_SEGREDO precisa de pelo menos "
                    + TAMANHO_MINIMO_DO_SEGREDO + " caracteres.");
        }
        return new SecretKeySpec(segredo, ALGORITMO_DA_ASSINATURA);
    }

    @Bean
    public JwtEncoder assinadorDeToken(SecretKey chaveDoToken) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(chaveDoToken));
    }

    @Bean
    public JwtDecoder decodificadorDeToken(SecretKey chaveDoToken) {
        return NimbusJwtDecoder.withSecretKey(chaveDoToken).build();
    }

    @Bean
    public EmissorDeToken emissorDeToken(JwtEncoder assinadorDeToken) {
        return new EmissorDeToken(assinadorDeToken);
    }

    @Bean
    public PasswordEncoder conferenteDeSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<FiltroDeIdentidade> filtroDeIdentidade(
            JwtDecoder decodificadorDeToken, ObjectMapper conversorDeJson) {
        FilterRegistrationBean<FiltroDeIdentidade> registro =
                new FilterRegistrationBean<FiltroDeIdentidade>();
        registro.setFilter(new FiltroDeIdentidade(
                new LeitorDeToken(decodificadorDeToken), conversorDeJson));
        registro.addUrlPatterns(ROTAS_DA_API);
        return registro;
    }
}
