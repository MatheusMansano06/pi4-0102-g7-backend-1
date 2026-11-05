package com.archflow.solicitacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    List<Solicitacao> findByStatusNotOrderByCriadaEmAsc(StatusSolicitacao status);

    List<Solicitacao> findByStatusOrderByCriadaEmAsc(StatusSolicitacao status);

    Solicitacao findBySolicitacaoId(Long solicitacaoId);

    // CT08: o historico comeca pelo pedido mais novo, porque e o que o aluno
    // procura primeiro quando abre a tela.
    List<Solicitacao> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);

    List<Solicitacao> findAllByOrderByCriadaEmDesc();
}
