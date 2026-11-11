-- Dados de exemplo, para o sistema abrir com conteudo na tela.
--
-- Sem isto o catalogo e a fila respondem lista vazia e parece que nada funciona.
-- A tela de cadastro nao faz parte do MVP; o POST H04.1 ja foi integrado parcialmente,
-- mas ainda depende de identidade validada. Estes sao dados de demonstracao.
--
-- Os numeros de aluno (1, 2 e 3) sao provisorios: a tabela usuarios ainda nao
-- existe, entao nao ha de onde puxar nome nem curso.

-- #1136: Luis confirmou dimensao e diametro opcionais para todos os materiais.
INSERT INTO usuarios (
    usuario_id, nome, email, senha_hash, perfil, ra, curso
) VALUES
    (1, 'Aluno Demonstracao 1', 'aluno1@archflow.demo', 'DEMO_SEM_LOGIN', 'ALUNO', '2026001', 'Sistemas de Informacao'),
    (2, 'Aluno Demonstracao 2', 'aluno2@archflow.demo', 'DEMO_SEM_LOGIN', 'ALUNO', '2026002', 'Engenharia de Software'),
    (3, 'Aluno Demonstracao 3', 'aluno3@archflow.demo', 'DEMO_SEM_LOGIN', 'ALUNO', '2026003', 'Ciencia da Computacao')
ON CONFLICT (usuario_id) DO NOTHING;

-- MATERIAIS

INSERT INTO materiais (
    material_id, nome, descricao, categoria, unidade_medida,
    quantidade_estoque, quantidade_minima, ativo,
    aceita_dimensao, exige_dimensao, aceita_diametro, exige_diametro
) VALUES
    (1, 'Placa de MDF', 'Chapa para maquete, espessura de 12 mm e tamanho de 600 x 400 mm', 'Madeira', 'UNIDADE', 40, 10, TRUE, TRUE, FALSE, TRUE, FALSE),
    (2, 'Papel Cartao', 'Papel cartao para trabalhos e maquetes', 'Papelaria', 'FOLHA', 100, 20, TRUE, FALSE, FALSE, FALSE, FALSE),
    (3, 'Tinta Acrilica', 'Tinta acrilica para pintura de projetos', 'Pintura', 'UNIDADE', 30, 5, TRUE, FALSE, FALSE, FALSE, FALSE),
    (4, 'Barra de Aluminio', 'Barra de aluminio para estruturas e projetos', 'Metal', 'UNIDADE', 15, 5, TRUE, TRUE, FALSE, TRUE, TRUE),
    (5, 'Cola Branca', 'Adesivo para papel, madeira e materiais diversos', 'Adesivos', 'UNIDADE', 50, 10, TRUE, FALSE, FALSE, FALSE, FALSE),
    (6, 'Tubo de PVC', 'Tubo de PVC para montagem de estruturas', 'Construcao', 'METRO', 25, 5, TRUE, TRUE, FALSE, TRUE, FALSE)
ON CONFLICT (material_id) DO NOTHING;

-- SOLICITACOES

INSERT INTO solicitacoes (
    solicitacao_id, usuario_id, material_id, quantidade,
    status, observacao, dimensao, diametro,
    data_solicitacao, data_atualizacao, retirada_prevista
) VALUES
    (1, 1, 1, 2, 'PENDENTE', 'Material para desenvolvimento de maquete', '600 x 400 mm', NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '2 days'),

    (2, 2, 2, 10, 'APROVADA', 'Material para atividade academica', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '1 day'),

    (3, 3, 3, 3, 'RETIRADA', 'Material utilizado em projeto de pintura', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),

    (4, 1, 4, 2, 'PENDENTE', 'Barras para montagem de estrutura', '2 metros', 10.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '3 days'),

    (5, 2, 5, 5, 'REJEITADA', 'Solicitacao de material para atividade', NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL)
ON CONFLICT (solicitacao_id) DO NOTHING;

-- AJUSTE DOS IDs AUTOMATICOS

SELECT setval(
    pg_get_serial_sequence('usuarios', 'usuario_id'),
    COALESCE((SELECT MAX(usuario_id) FROM usuarios), 1),
    EXISTS (SELECT 1 FROM usuarios)
);

SELECT setval(
    pg_get_serial_sequence('materiais', 'material_id'),
    COALESCE((SELECT MAX(material_id) FROM materiais), 1),
    EXISTS (SELECT 1 FROM materiais)
);

SELECT setval(
    pg_get_serial_sequence('solicitacoes', 'solicitacao_id'),
    COALESCE((SELECT MAX(solicitacao_id) FROM solicitacoes), 1),
    EXISTS (SELECT 1 FROM solicitacoes)
);
