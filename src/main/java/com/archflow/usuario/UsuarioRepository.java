package com.archflow.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

// Consulta do CT10: achar o usuario pelo e-mail. Devolve null quando
// nenhum bate.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);
}
