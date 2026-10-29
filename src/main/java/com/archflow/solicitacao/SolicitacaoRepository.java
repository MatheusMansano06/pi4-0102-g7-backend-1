package com.archflow.solicitacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    List<Solicitacao> findByStatusNotOrderByCriadaEmAsc(StatusSolicitacao status);

    List<Solicitacao> findByStatusOrderByCriadaEmAsc(StatusSolicitacao status);
}
