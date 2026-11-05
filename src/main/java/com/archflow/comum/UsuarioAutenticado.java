package com.archflow.comum;

// Identidade interna que H02.2 deve produzir após validar Keycloak e usuarioId.
// Não é um token, não valida credenciais e não é preenchida pelo corpo/header HTTP.
public class UsuarioAutenticado {

    private final Long usuarioId;

    private final PerfilUsuario perfil;

    public UsuarioAutenticado(Long usuarioId, PerfilUsuario perfil) {
        if (usuarioId == null || usuarioId <= 0 || perfil == null) {
            throw new IllegalArgumentException("A identidade precisa de usuário e perfil válidos.");
        }
        this.usuarioId = usuarioId;
        this.perfil = perfil;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
    }

    public PerfilUsuario getPerfil() {
        return this.perfil;
    }
}
