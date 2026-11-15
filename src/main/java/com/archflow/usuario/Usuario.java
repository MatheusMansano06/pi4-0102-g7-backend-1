package com.archflow.usuario;

import com.archflow.comum.PerfilUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Quem usa o ArchFlow, conforme o CT10: aluno ou responsavel.
// A senha fica no banco so como hash, nunca como a pessoa digitou.
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usuarioId;

    private String nome;

    private String email;

    @Column(name = "senha_hash")
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    private PerfilUsuario perfil;

    private boolean ativo;

    // Dados que so o aluno tem. O responsavel guarda matricula e cargo, que o
    // cadastro pela tela nao cria: a conta de responsavel nasce pelo script.
    private String ra;

    private String curso;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, PerfilUsuario perfil) {
        this.setNome(nome);
        this.setEmail(email);
        this.setSenhaHash(senhaHash);
        this.setPerfil(perfil);
        this.ativo = true;
    }

    // Conta de aluno criada pela tela de cadastro (CT14). O banco exige RA e
    // curso preenchidos quando o perfil e ALUNO.
    public Usuario(String nome, String email, String senhaHash, String ra, String curso) {
        this.setNome(nome);
        this.setEmail(email);
        this.setSenhaHash(senhaHash);
        this.setPerfil(PerfilUsuario.ALUNO);
        this.setRa(ra);
        this.setCurso(curso);
        this.ativo = true;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O usuário precisa de um nome.");
        }
        this.nome = nome.trim();
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("O usuário precisa de um e-mail.");
        }
        this.email = email.trim();
    }

    public String getSenhaHash() {
        return this.senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        if (senhaHash == null || senhaHash.trim().isEmpty()) {
            throw new IllegalArgumentException("O usuário precisa de uma senha gravada.");
        }
        this.senhaHash = senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return this.perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("O usuário precisa de um perfil.");
        }
        this.perfil = perfil;
    }

    public String getRa() {
        return this.ra;
    }

    public void setRa(String ra) {
        if (ra == null || ra.trim().isEmpty()) {
            throw new IllegalArgumentException("O aluno precisa de um RA.");
        }
        this.ra = ra.trim();
    }

    public String getCurso() {
        return this.curso;
    }

    public void setCurso(String curso) {
        if (curso == null || curso.trim().isEmpty()) {
            throw new IllegalArgumentException("O aluno precisa de um curso.");
        }
        this.curso = curso.trim();
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
