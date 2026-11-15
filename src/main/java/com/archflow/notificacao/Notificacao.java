package com.archflow.notificacao;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacao_id")
    private Long notificacaoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "solicitacao_id")
    private Long solicitacaoId;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, columnDefinition = "text")
    private String mensagem;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Notificacao() {
    }

    public Notificacao(Long usuarioId, Long solicitacaoId,
                       String tipo, String mensagem) {
        this.usuarioId = usuarioId;
        this.solicitacaoId = solicitacaoId;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.lida = false;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getNotificacaoId() {
        return notificacaoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getSolicitacaoId() {
        return solicitacaoId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public boolean isLida() {
        return lida;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}