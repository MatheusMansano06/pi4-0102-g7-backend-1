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

    public Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, PerfilUsuario perfil) {
        this.setNome(nome);
        this.setEmail(email);
        this.setSenhaHash(senhaHash);
        this.setPerfil(perfil);
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

    public boolean isAtivo() {
        return this.ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
