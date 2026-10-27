package com.archflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Classe que liga o sistema. O Spring procura as outras classes a partir
// deste pacote (com.archflow) e de tudo que esta dentro dele.
@SpringBootApplication
public class ArchflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchflowApplication.class, args);
    }
}
