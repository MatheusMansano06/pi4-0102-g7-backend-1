# Consulta de notificações #1161

Controller/Service/Repository/Entity oficiais publicados pela Lara na PR37.
O filtro JWT identifica o destinatário; a consulta JPA devolve somente os seus
avisos, com filtro opcional estrito de não lidas. A rota está registrada sem
flag de desenvolvimento desde a integração oficial. Ler não cria avisos nem
marca leitura. A tabela e o campo criado_em são os do SQL oficial.

A geração H09.1 é chamada na mesma transação da mudança de status. O #1161
preserva essa implementação e comprova a consulta com HTTP/login/JWT/banco
reais, incluindo rollback em falha do aviso. Não há adaptador paralelo.

[Contrato CT07](../../../../../../docs/contratos/CT07-listarNotificacoes.md) e
[prova/reprodução](../../../../../../docs/integracao/H09.2-retomada-jwt.md).
