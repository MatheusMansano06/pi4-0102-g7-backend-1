package com.archflow.solicitacao;

// Os status pelos quais um pedido passa, na ordem do CT06.
// CANCELADO nao esta no CT04 nem no CT12: entrou pela secao 3 de
// docs/pendencias.md, porque sem ele pedido errado fica preso na fila.
public enum StatusSolicitacao {
    PENDENTE,
    EM_SEPARACAO,
    AGUARDANDO_COMPRA,
    PRONTO_PARA_RETIRADA,
    CONCLUIDO,
    CANCELADO
}
