package com.archflow.usuario;

import com.archflow.comum.PerfilUsuario;

// O que o CT01 devolve quando o login da certo. Senha e hash nunca voltam.
public class AutenticacaoSaida {

    private final Long usuarioId;

    private final String nome;

    private final PerfilUsuario perfil;

    private final String token;

    public AutenticacaoSaida(Usuario usuario, String token) {
        this.usuarioId = usuario.getUsuarioId();
        this.nome = usuario.getNome();
        this.perfil = usuario.getPerfil();
        this.token = token;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
    }

    public String getNome() {
        return this.nome;
    }

    public PerfilUsuario getPerfil() {
        return this.perfil;
    }

    public String getToken() {
        return this.token;
    }
}
