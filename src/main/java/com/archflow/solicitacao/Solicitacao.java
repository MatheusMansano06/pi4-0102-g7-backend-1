package com.archflow.solicitacao;

import com.archflow.material.Material;
import com.archflow.usuario.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long solicitacaoId;

    private Long usuarioId;

    // O mesmo usuario_id, agora lido como o aluno inteiro, para a fila e o
    // historico mostrarem o nome de quem pediu. So leitura: quem grava o dono
    // do pedido continua sendo o usuarioId acima, por isso insertable e
    // updatable ficam falsos e a coluna nao e escrita duas vezes.
    @ManyToOne
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario aluno;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    private int quantidade;

    @Enumerated(EnumType.STRING)
    private StatusSolicitacao status;

    private LocalDateTime criadaEm;

    private LocalDateTime atualizadaEm;

    private LocalDateTime previsaoRetirada;

    private String observacao;

    private String dimensao;

    private Double diametro;

    public Solicitacao() {
    }

    public Solicitacao(Long usuarioId, Material material, int quantidade, String observacao) {
        this.setUsuarioId(usuarioId);
        this.setMaterial(material);
        this.setQuantidade(quantidade);
        this.setObservacao(observacao);
        this.setStatus(StatusSolicitacao.PENDENTE);
        this.criadaEm = this.atualizadaEm;
    }

    public Long getSolicitacaoId() {
        return this.solicitacaoId;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("O pedido precisa ser de algum aluno.");
        }
        this.usuarioId = usuarioId;
    }

    // Vem nulo no pedido recem-montado na memoria, que ainda nao passou pelo
    // banco: quem mostra o nome checa antes.
    public Usuario getAluno() {
        return this.aluno;
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        if (material == null) {
            throw new IllegalArgumentException("O pedido precisa de um material.");
        }
        this.material = material;
    }

    public int getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public StatusSolicitacao getStatus() {
        return this.status;
    }

    // Toda troca de status marca a hora, que e o que o CT12 devolve em atualizadaEm.
    public void setStatus(StatusSolicitacao status) {
        if (status == null) {
            throw new IllegalArgumentException("O pedido precisa de um status.");
        }
        this.status = status;
        this.atualizadaEm = LocalDateTime.now();
    }

    public LocalDateTime getCriadaEm() {
        return this.criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return this.atualizadaEm;
    }

    public LocalDateTime getPrevisaoRetirada() {
        return this.previsaoRetirada;
    }

    public void setPrevisaoRetirada(LocalDateTime previsaoRetirada) {
        this.previsaoRetirada = previsaoRetirada;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getDimensao() {
        return this.dimensao;
    }

    public void setDimensao(String dimensao) {
        this.dimensao = dimensao;
    }

    public Double getDiametro() {
        return this.diametro;
    }

    public void setDiametro(Double diametro) {
        this.diametro = diametro;
    }

    // CT06: para onde o pedido pode ir a partir de onde ele esta. As setas
    // que voltam existem para o responsavel corrigir engano.
    public boolean podeIrPara(StatusSolicitacao novoStatus) {
        return this.estaNaLista(novoStatus, this.destinosPermitidos());
    }

    private StatusSolicitacao[] destinosPermitidos() {
        if (this.status == StatusSolicitacao.PENDENTE) {
            return new StatusSolicitacao[] { StatusSolicitacao.EM_SEPARACAO,
                    StatusSolicitacao.AGUARDANDO_COMPRA, StatusSolicitacao.CANCELADO };
        }
        if (this.status == StatusSolicitacao.EM_SEPARACAO) {
            return new StatusSolicitacao[] { StatusSolicitacao.PRONTO_PARA_RETIRADA,
                    StatusSolicitacao.AGUARDANDO_COMPRA, StatusSolicitacao.PENDENTE,
                    StatusSolicitacao.CANCELADO };
        }
        return this.destinosDepoisDaSeparacao();
    }

    // Pedido CONCLUIDO ou CANCELADO esta fechado: a lista dele volta vazia.
    private StatusSolicitacao[] destinosDepoisDaSeparacao() {
        if (this.status == StatusSolicitacao.AGUARDANDO_COMPRA) {
            return new StatusSolicitacao[] { StatusSolicitacao.EM_SEPARACAO,
                    StatusSolicitacao.CANCELADO };
        }
        if (this.status == StatusSolicitacao.PRONTO_PARA_RETIRADA) {
            return new StatusSolicitacao[] { StatusSolicitacao.CONCLUIDO,
                    StatusSolicitacao.EM_SEPARACAO, StatusSolicitacao.CANCELADO };
        }
        return new StatusSolicitacao[0];
    }

    private boolean estaNaLista(StatusSolicitacao novoStatus, StatusSolicitacao[] permitidos) {
        for (int i = 0; i < permitidos.length; i++) {
            if (permitidos[i] == novoStatus) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Pedido de " + this.quantidade + " " + this.material.getNome() + " (" + this.status + ")";
    }
}
