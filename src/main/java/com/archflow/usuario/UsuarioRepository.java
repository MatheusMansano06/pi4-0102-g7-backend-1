package com.archflow.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

// Consulta do CT10: achar o usuario pelo e-mail que a tela mandou.
// Devolve null quando nenhum e-mail bate, e o login trata isso como
// credencial invalida, sem dizer se o que errou foi o e-mail ou a senha.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);
}
