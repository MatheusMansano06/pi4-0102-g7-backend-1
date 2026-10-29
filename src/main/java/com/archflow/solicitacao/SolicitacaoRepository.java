package com.archflow.solicitacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    List<Solicitacao> findAllByOrderByCriadaEmAsc();

    List<Solicitacao> findByStatusOrderByCriadaEmAsc(StatusSolicitacao status);
}
