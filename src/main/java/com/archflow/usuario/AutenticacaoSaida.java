package com.archflow.usuario;

import com.archflow.comum.PerfilUsuario;

// O que o CT01 devolve quando o login da certo. Senha e hash nunca voltam.
// O curso vai junto porque a barra de cima da tela mostra o nome do aluno e,
// embaixo, o curso dele. Em conta de responsavel o campo e nulo: curso e dado
// de aluno, e o banco so deixa um dos dois conjuntos preenchido.
public class AutenticacaoSaida {

    private final Long usuarioId;

    private final String nome;

    private final String curso;

    private final PerfilUsuario perfil;

    private final String token;

    public AutenticacaoSaida(Usuario usuario, String token) {
        this.usuarioId = usuario.getUsuarioId();
        this.nome = usuario.getNome();
        this.curso = usuario.getCurso();
        this.perfil = usuario.getPerfil();
        this.token = token;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
    }

    public String getNome() {
        return this.nome;
    }

    public String getCurso() {
        return this.curso;
    }

    public PerfilUsuario getPerfil() {
        return this.perfil;
    }

    public String getToken() {
        return this.token;
    }
}
