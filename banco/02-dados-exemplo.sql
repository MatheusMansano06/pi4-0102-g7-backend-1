-- Dados de exemplo, para o sistema abrir com conteudo na tela.
--
-- Sem isto o catalogo e a fila respondem lista vazia e parece que nada funciona.
-- A tela de cadastro nao faz parte do MVP; o POST H04.1 ja foi integrado parcialmente,
-- mas ainda depende de identidade validada. Estes sao dados de demonstracao.
--
-- Os numeros de aluno (1, 2 e 3) sao provisorios: a tabela usuarios ainda nao
-- existe, entao nao ha de onde puxar nome nem curso.

-- #1136: Luis confirmou dimensao e diametro opcionais para todos os materiais.
INSERT INTO materiais (nome, descricao, especificacao, quantidade_disponivel, quantidade_minima, ativo,
                      aceita_dimensao, exige_dimensao, aceita_diametro, exige_diametro) VALUES
    -- saldo acima do minimo: aparece como DISPONIVEL
    ('Placa de MDF',        'Chapa para maquete',      '12 mm, 600 x 400 mm',  40, 10, TRUE, TRUE, FALSE, TRUE, FALSE),
    ('Acrilico cristal',    'Chapa transparente',      '3 mm, 600 x 400 mm',   25,  8, TRUE, TRUE, FALSE, TRUE, FALSE),
    -- saldo igual ou abaixo do minimo: aparece como BAIXO
    ('Papel Parana',        'Papel rigido para corte', '1,5 mm, 500 x 700 mm', 12, 15, TRUE, TRUE, FALSE, TRUE, FALSE),
    ('Cola branca escolar', 'Frasco de 500 g',         '500 g',                 7, 10, TRUE, TRUE, FALSE, TRUE, FALSE),
    -- saldo zero: aparece como INDISPONIVEL
    ('Vareta de pinus',     'Perfil quadrado',         '10 x 10 mm, 1000 mm',   0,  5, TRUE, TRUE, FALSE, TRUE, FALSE),
    -- inativo: o catalogo nao mostra, conforme o CT02
    ('Espuma depreciada',   'Material fora de uso',    'descontinuado',        99,  1, FALSE, TRUE, FALSE, TRUE, FALSE);

INSERT INTO solicitacoes (usuario_id, material_id, quantidade, status, criada_em, atualizada_em, previsao_retirada, observacao) VALUES
    (1, 1, 4, 'PENDENTE',             NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NULL,                      'Maquete da disciplina de Projeto III'),
    (2, 3, 2, 'EM_SEPARACAO',         NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day',  NOW() + INTERVAL '2 days', 'Retirar na parte da tarde'),
    (1, 2, 1, 'PRONTO_PARA_RETIRADA', NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day',  NOW() + INTERVAL '1 day',  NULL),
    (3, 4, 3, 'AGUARDANDO_COMPRA',    NOW() - INTERVAL '6 days', NOW() - INTERVAL '4 days', NULL,                      'Estoque acabou'),
    -- concluido: a fila padrao esconde este, conforme o CT04
    (2, 1, 6, 'CONCLUIDO',            NOW() - INTERVAL '9 days', NOW() - INTERVAL '7 days', NULL,                      NULL);
