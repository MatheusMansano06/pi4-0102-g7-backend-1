package com.archflow.solicitacao;

import java.time.LocalDateTime;

// O que o CT08 devolve por pedido do historico, com a data da ultima mudanca.
// A dimensao entra porque a tela do historico do responsavel tem a coluna e
// deixa ordenar por ela. Pedido sem medida volta com o campo nulo.
// O nome do aluno entra agora que a tabela usuarios existe (H01.2): e o que o
// responsavel usa para saber de quem e cada pedido da lista dele.
// A previsao de retirada vai junto porque o painel de prazos do aluno mostra,
// por pedido, o status e a data marcada. Pedido sem data volta com o campo nulo.
public class SolicitacaoHistoricoSaida {

    private Long solicitacaoId;

    private String aluno;

    private String material;

    private int quantidade;

    private StatusSolicitacao status;

    private LocalDateTime criadaEm;

    private LocalDateTime atualizadaEm;

    private String dimensao;

    private LocalDateTime previsaoRetirada;

    public SolicitacaoHistoricoSaida(Solicitacao solicitacao) {
        this.solicitacaoId = solicitacao.getSolicitacaoId();
        this.aluno = nomeDoAluno(solicitacao);
        this.material = solicitacao.getMaterial().getNome();
        this.quantidade = solicitacao.getQuantidade();
        this.status = solicitacao.getStatus();
        this.criadaEm = solicitacao.getCriadaEm();
        this.atualizadaEm = solicitacao.getAtualizadaEm();
        this.dimensao = solicitacao.getDimensao();
        this.previsaoRetirada = solicitacao.getPrevisaoRetirada();
    }

    // Pedido montado so na memoria nao tem o aluno carregado. Nesse caso o
    // campo vai nulo em vez de quebrar o historico inteiro.
    private static String nomeDoAluno(Solicitacao solicitacao) {
        if (solicitacao.getAluno() == null) {
            return null;
        }
        return solicitacao.getAluno().getNome();
    }

    public Long getSolicitacaoId() {
        return this.solicitacaoId;
    }

    public String getAluno() {
        return this.aluno;
    }

    public String getMaterial() {
        return this.material;
    }

    public int getQuantidade() {
        return this.quantidade;
    }

    public StatusSolicitacao getStatus() {
        return this.status;
    }

    public LocalDateTime getCriadaEm() {
        return this.criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return this.atualizadaEm;
    }

    public String getDimensao() {
        return this.dimensao;
    }

    public LocalDateTime getPrevisaoRetirada() {
        return this.previsaoRetirada;
    }
}
