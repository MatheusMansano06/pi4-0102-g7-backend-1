# Consulta de notificações #1161

O CT07 está preparado em NotificacaoController/NotificacaoService. Recebe o
usuário da identidade validada e pede a ConsultaDeNotificacoes somente os
avisos desse destinatário, com filtro opcional de não lidas. A consulta não
cria avisos nem marca leitura. Os campos de saída estão em CT07/CT13.

A rota e o serviço permanecem desativados na configuração padrão. Não há
adaptador de produção ou tabela oficial de notificações nesta entrega.
H01.2 define o esquema e H09.1 gera os avisos nas mudanças de status previstas.

ConsultaDeNotificacoesTemporaria existe apenas em src/test. Usa uma tabela
temporária de sessão, consulta parametrizada e rollback para validar o filtro.
Os nomes físicos dessa fixture não são uma proposta de migração da equipe.
Ver o [roteiro do CT07](../../../../../../docs/contratos/CT07-listarNotificacoes.md).
