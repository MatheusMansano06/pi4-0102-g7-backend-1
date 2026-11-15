package com.archflow.usuario;

// O que a tela de cadastro manda no corpo do POST /api/auth/cadastro (CT14).
// O perfil nao vem no corpo: esta rota cria somente conta de aluno.
public class CadastroAlunoEntrada {

    private String nome;

    private String ra;

    private String curso;

    private String email;

    private String senha;

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRa() {
        return this.ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public String getCurso() {
        return this.curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

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
