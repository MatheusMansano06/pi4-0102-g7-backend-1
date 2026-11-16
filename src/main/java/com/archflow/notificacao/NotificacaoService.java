package com.archflow.notificacao;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.UsuarioAutenticado;
import com.archflow.solicitacao.Solicitacao;
import com.archflow.solicitacao.StatusSolicitacao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    // Cria uma notificação conforme o novo status da solicitação.

public void registrarMudancaDeStatus(
        Solicitacao solicitacao,
        StatusSolicitacao novoStatus) {

    if (solicitacao == null || novoStatus == null
            || solicitacao.getUsuarioId() == null) {
        throw new IllegalArgumentException(
            "A solicitação e o usuário são obrigatórios."
        );
    }

    String tipo;
    String mensagem;

    switch (novoStatus) {
        case PENDENTE:
            tipo = "PEDIDO_RECEBIDO";
            mensagem = "Sua solicitação foi recebida.";
            break;

        case EM_SEPARACAO:
            tipo = "PEDIDO_EM_SEPARACAO";
            mensagem = "Seu pedido está em separação.";
            break;

        case AGUARDANDO_COMPRA:
            tipo = "PEDIDO_AGUARDANDO_COMPRA";
            mensagem = "Seu pedido está aguardando a compra do material.";
            break;

        case PRONTO_PARA_RETIRADA:
            tipo = "PEDIDO_PRONTO";
            mensagem = "Seu pedido está pronto para retirada.";
            break;

        case CONCLUIDO:
            tipo = "PEDIDO_CONCLUIDO";
            mensagem = "Seu pedido foi concluído.";
            break;

        default:
            tipo = "STATUS_ATUALIZADO";
            mensagem = "O status do seu pedido foi atualizado para "
                    + novoStatus.name().replace("_", " ").toLowerCase();
            break;
    }

    Notificacao notificacao = new Notificacao(
        solicitacao.getUsuarioId(),
        solicitacao.getSolicitacaoId(),
        tipo,
        mensagem
    );

    notificacaoRepository.save(notificacao);
}

    // Consulta as notificações do usuário autenticado.
    public NotificacoesSaida listar(
            UsuarioAutenticado identidade,
            boolean somenteNaoLidas) {

        if (identidade == null) {
            throw new NaoAutenticadoException(
                    CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso estar autenticado para consultar notificações."
            );
        }

        List<Notificacao> encontradas;

        if (somenteNaoLidas) {

            encontradas = notificacaoRepository
                    .findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(
                            identidade.getUsuarioId());

        } else {

            encontradas = notificacaoRepository
                    .findByUsuarioIdOrderByCriadoEmDesc(
                            identidade.getUsuarioId());
        }

        List<NotificacaoSaida> saida =
                new ArrayList<NotificacaoSaida>();

        for (Notificacao notificacao : encontradas) {
            saida.add(new NotificacaoSaida(notificacao));
        }

        return new NotificacoesSaida(saida);
    }
}