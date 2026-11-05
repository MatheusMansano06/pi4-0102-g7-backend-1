-- Dados de exemplo, para o sistema abrir com conteudo na tela.
--
-- Sem isto o catalogo e a fila respondem lista vazia e parece que nada funciona.
-- Enquanto nao existe a tela de cadastro nem o POST de pedido (H04.1), e daqui
-- que saem os materiais e os pedidos usados para conferir o banco.
--
-- Os numeros de aluno (1, 2 e 3) sao provisorios: a tabela usuarios ainda nao
-- existe, entao nao ha de onde puxar nome nem curso.

INSERT INTO materiais (nome, descricao, especificacao, quantidade_disponivel, quantidade_minima, ativo) VALUES
    -- saldo acima do minimo: aparece como DISPONIVEL
    ('Placa de MDF',        'Chapa para maquete',      '12 mm, 600 x 400 mm',  40, 10, TRUE),
    ('Acrilico cristal',    'Chapa transparente',      '3 mm, 600 x 400 mm',   25,  8, TRUE),
    -- saldo igual ou abaixo do minimo: aparece como BAIXO
    ('Papel Parana',        'Papel rigido para corte', '1,5 mm, 500 x 700 mm', 12, 15, TRUE),
    ('Cola branca escolar', 'Frasco de 500 g',         '500 g',                 7, 10, TRUE),
    -- saldo zero: aparece como INDISPONIVEL
    ('Vareta de pinus',     'Perfil quadrado',         '10 x 10 mm, 1000 mm',   0,  5, TRUE),
    -- inativo: o catalogo nao mostra, conforme o CT02
    ('Espuma depreciada',   'Material fora de uso',    'descontinuado',        99,  1, FALSE);

INSERT INTO solicitacoes (usuario_id, material_id, quantidade, status, criada_em, atualizada_em, previsao_retirada, observacao) VALUES
    (1, 1, 4, 'PENDENTE',             NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NULL,                      'Maquete da disciplina de Projeto III'),
    (2, 3, 2, 'EM_SEPARACAO',         NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day',  NOW() + INTERVAL '2 days', 'Retirar na parte da tarde'),
    (1, 2, 1, 'PRONTO_PARA_RETIRADA', NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day',  NOW() + INTERVAL '1 day',  NULL),
    (3, 4, 3, 'AGUARDANDO_COMPRA',    NOW() - INTERVAL '6 days', NOW() - INTERVAL '4 days', NULL,                      'Estoque acabou'),
    -- concluido: a fila padrao esconde este, conforme o CT04
    (2, 1, 6, 'CONCLUIDO',            NOW() - INTERVAL '9 days', NOW() - INTERVAL '7 days', NULL,                      NULL);
