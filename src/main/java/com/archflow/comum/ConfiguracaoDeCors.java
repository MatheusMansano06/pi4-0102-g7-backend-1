package com.archflow.comum;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Libera a tela do ArchFlow para chamar esta API.
//
// O navegador recusa, por padrao, uma pagina de um endereco pedir dados a
// outro endereco. A tela roda na porta 5173 e a API na 8080: sao enderecos
// diferentes, entao sem esta liberacao o navegador bloqueia a chamada antes
// mesmo dela sair.
//
// So a tela do projeto entra na lista. Liberar qualquer endereco deixaria
// qualquer site do mundo chamar esta API pelo navegador do aluno.
@Configuration
public class ConfiguracaoDeCors implements WebMvcConfigurer {

    // Enderecos onde a tela roda: o do dia a dia e o de dentro do Docker.
    private static final String TELA_NO_COMPUTADOR = "http://localhost:5173";

    private static final String TELA_NO_DOCKER = "http://127.0.0.1:5173";

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(TELA_NO_COMPUTADOR, TELA_NO_DOCKER)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
