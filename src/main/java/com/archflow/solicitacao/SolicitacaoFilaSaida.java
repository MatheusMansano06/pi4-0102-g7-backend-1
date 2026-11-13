package com.archflow.solicitacao;

import java.time.LocalDateTime;

// O que o CT04 devolve por pedido da fila. O responsavel decide a triagem
// pela dimensao, pelo diametro e pela observacao, entao os tres vao junto.
// O nome do aluno entra agora que a tabela usuarios existe (H01.2).
public class SolicitacaoFilaSaida {

    private Long solicitacaoId;

    private String aluno;

    private String material;

    private int quantidade;

    private StatusSolicitacao status;

    private LocalDateTime criadaEm;

    private String dimensao;

    private Double diametro;

    private String observacao;

    public SolicitacaoFilaSaida(Solicitacao solicitacao) {
        this.solicitacaoId = solicitacao.getSolicitacaoId();
        this.aluno = nomeDoAluno(solicitacao);
        this.material = solicitacao.getMaterial().getNome();
        this.quantidade = solicitacao.getQuantidade();
        this.status = solicitacao.getStatus();
        this.criadaEm = solicitacao.getCriadaEm();
        this.dimensao = solicitacao.getDimensao();
        this.diametro = solicitacao.getDiametro();
        this.observacao = solicitacao.getObservacao();
    }

    // Pedido montado so na memoria nao tem o aluno carregado. Nesse caso o
    // campo vai nulo em vez de quebrar a fila inteira.
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

    public String getDimensao() {
        return this.dimensao;
    }

    public Double getDiametro() {
        return this.diametro;
    }

    public String getObservacao() {
        return this.observacao;
    }
}
