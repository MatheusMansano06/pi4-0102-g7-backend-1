package com.archflow.usuario;

// O que a tela de login manda no corpo do POST /api/auth/login (CT01).
public class AutenticacaoEntrada {

    private String email;

    private String senha;

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
