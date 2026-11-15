package com.archflow.notificacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long> {

    // Busca todas as notificações de um usuário.
    List<Notificacao> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    // Busca somente as notificações não lidas.
    List<Notificacao> findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(
            Long usuarioId);
}