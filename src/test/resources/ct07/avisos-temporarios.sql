-- Fixture de leitura do CT07. Não é a migração de notificações de H01.2.
CREATE TEMP TABLE avisos_ct07_teste (
    notificacao_id BIGINT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(255) NOT NULL,
    mensagem VARCHAR(255) NOT NULL,
    criada_em TIMESTAMP NOT NULL,
    lida BOOLEAN NOT NULL,
    solicitacao_id BIGINT
) ON COMMIT DROP;

INSERT INTO pg_temp.avisos_ct07_teste
    (notificacao_id, usuario_id, tipo, mensagem, criada_em, lida, solicitacao_id)
VALUES
    (55, 12, 'PEDIDO_PRONTO', 'Pedido disponível para retirada', '2026-10-13 12:15:10', FALSE, 101),
    (56, 12, 'AVISO', 'Aviso de teste do aluno', '2026-10-13 13:00:00', TRUE, NULL),
    (92, 23, 'AVISO', 'Aviso de teste do responsável', '2026-10-13 14:00:00', FALSE, NULL),
    (93, 23, 'PEDIDO_PRONTO', 'Outro pedido de teste', '2026-10-13 15:00:00', TRUE, 201);
