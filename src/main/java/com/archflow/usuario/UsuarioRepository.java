package com.archflow.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

// Consulta do CT10: achar o usuario pelo e-mail. Devolve null quando
// nenhum bate.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);

    // O banco tem indice unico por LOWER(email), entao o CT14 precisa conferir
    // o e-mail sem diferenciar maiuscula de minuscula antes de gravar.
    Usuario findByEmailIgnoreCase(String email);

    // O RA tambem e unico no banco; o CT14 recusa o repetido com mensagem
    // propria em vez de deixar o indice estourar.
    Usuario findByRa(String ra);
}
