package com.archflow.comum;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Libera a tela do ArchFlow para chamar esta API.
@Configuration
public class ConfiguracaoDeCors implements WebMvcConfigurer {

    // Enderecos onde a tela roda: o do dia a dia e o de dentro do Docker.
    private static final String TELA_NO_COMPUTADOR = "http://localhost:5173";

    private static final String TELA_NO_DOCKER = "http://127.0.0.1:5173";

    public static boolean eTelaDoArchflow(String origem) {
        return TELA_NO_COMPUTADOR.equals(origem) || TELA_NO_DOCKER.equals(origem);
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(TELA_NO_COMPUTADOR, TELA_NO_DOCKER)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
