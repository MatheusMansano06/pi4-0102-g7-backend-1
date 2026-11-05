package com.archflow.comum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

// Liga a conferencia de identidade nas rotas da API.
//
// O endereco das chaves do Keycloak vem do ambiente, porque muda de maquina
// para maquina. As chaves sao buscadas na primeira chamada com token, e nao
// quando o backend sobe: assim o backend continua subindo mesmo com o Keycloak
// desligado, e so as rotas da API ficam indisponiveis.
@Configuration
public class ConfiguracaoDeIdentidade {

    private static final String ROTAS_DA_API = "/api/*";

    @Value("${archflow.keycloak.certificados}")
    private String enderecoDasChavesDoKeycloak;

    @Bean
    public JwtDecoder decodificadorDeToken() {
        return NimbusJwtDecoder.withJwkSetUri(this.enderecoDasChavesDoKeycloak).build();
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
