package com.archflow.solicitacao;

import java.time.LocalDateTime;

// O que o CT06 recebe para mudar o status de um pedido. O status chega como
// texto porque e o que o JSON manda; quem confere se ele existe e o Service.
public class SolicitacaoStatusEntrada {

    private String status;

    private LocalDateTime previsaoRetirada;

    public SolicitacaoStatusEntrada() {
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPrevisaoRetirada() {
        return this.previsaoRetirada;
    }

    public void setPrevisaoRetirada(LocalDateTime previsaoRetirada) {
        this.previsaoRetirada = previsaoRetirada;
    }
}
