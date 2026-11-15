-- Dados de exemplo, para o sistema abrir com conteudo na tela.
--
-- Sem isto o catalogo e a fila respondem lista vazia e parece que nada funciona.
-- A tela de cadastro nao faz parte do MVP; o POST H04.1 ja foi integrado parcialmente,
-- mas ainda depende de identidade validada. Estes sao dados de demonstracao.
--
-- Os numeros de aluno (1, 2 e 3) sao provisorios: a tabela usuarios ainda nao
-- existe, entao nao ha de onde puxar nome nem curso.

-- #1136: Luis confirmou dimensao e diametro opcionais para todos os materiais.
-- Gera hashes BCrypt compativeis com o PasswordEncoder do Spring.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- USUARIOS


INSERT INTO usuarios (
    usuario_id,
    nome,
    email,
    senha_hash,
    perfil,
    ativo,
    ra,
    curso,
    matricula_funcional,
    cargo
) VALUES
(
    1, 'Aluno Demonstracao 1',
    'aluno1@archflow.demo',
    crypt('Archflow@123', gen_salt('bf', 10)),
    'ALUNO', TRUE, '2026001',
    'Sistemas de Informacao', NULL, NULL
),
(
    2, 'Aluno Demonstracao 2',
    'aluno2@archflow.demo',
    crypt('Archflow@123', gen_salt('bf', 10)),
    'ALUNO', TRUE, '2026002',
    'Engenharia de Software', NULL, NULL
),
(
    3, 'Aluno Demonstracao 3',
    'aluno3@archflow.demo',
    crypt('Archflow@123', gen_salt('bf', 10)),
    'ALUNO', TRUE, '2026003',
    'Ciencia da Computacao', NULL, NULL
)
ON CONFLICT (usuario_id) DO UPDATE SET
    nome = EXCLUDED.nome,
    email = EXCLUDED.email,
    senha_hash = EXCLUDED.senha_hash,
    perfil = EXCLUDED.perfil,
    ativo = EXCLUDED.ativo,
    ra = EXCLUDED.ra,
    curso = EXCLUDED.curso,
    matricula_funcional = EXCLUDED.matricula_funcional,
    cargo = EXCLUDED.cargo;

-- MATERIAIS


INSERT INTO materiais (
    material_id,
    nome,
    descricao,
    especificacao,
    quantidade_disponivel,
    quantidade_minima,
    ativo,
    atualizada_em,
    aceita_dimensao,
    exige_dimensao,
    aceita_diametro,
    exige_diametro
) VALUES
(
    1, 'Placa de MDF',
    'Chapa para maquete',
    'Espessura 12 mm, tamanho 600 x 400 mm',
    40, 10, TRUE, CURRENT_TIMESTAMP,
    TRUE, FALSE, TRUE, FALSE
),
(
    2, 'Papel Cartao',
    'Papel cartao para trabalhos e maquetes',
    'Folha de papel cartao',
    100, 20, TRUE, CURRENT_TIMESTAMP,
    TRUE, FALSE, TRUE, FALSE
),
(
    3, 'Tinta Acrilica',
    'Tinta acrilica para pintura de projetos',
    'Tinta para trabalhos academicos',
    30, 5, TRUE, CURRENT_TIMESTAMP,
    TRUE, FALSE, TRUE, FALSE
),
(
    4, 'Barra de Aluminio',
    'Barra de aluminio para estruturas',
    'Material metalico para projetos',
    15, 5, TRUE, CURRENT_TIMESTAMP,
    TRUE, FALSE, TRUE, FALSE
),
(
    5, 'Cola Branca',
    'Adesivo para papel e madeira',
    'Cola branca escolar',
    50, 10, TRUE, CURRENT_TIMESTAMP,
    TRUE, FALSE, TRUE, FALSE
),
(
    6, 'Tubo de PVC',
    'Tubo de PVC para estruturas',
    'Tubo para montagem de projetos',
    25, 5, TRUE, CURRENT_TIMESTAMP,
    TRUE, FALSE, TRUE, FALSE
)
ON CONFLICT (material_id) DO UPDATE SET
    nome = EXCLUDED.nome,
    descricao = EXCLUDED.descricao,
    especificacao = EXCLUDED.especificacao,
    quantidade_disponivel = EXCLUDED.quantidade_disponivel,
    quantidade_minima = EXCLUDED.quantidade_minima,
    ativo = EXCLUDED.ativo,
    atualizada_em = EXCLUDED.atualizada_em,
    aceita_dimensao = EXCLUDED.aceita_dimensao,
    exige_dimensao = EXCLUDED.exige_dimensao,
    aceita_diametro = EXCLUDED.aceita_diametro,
    exige_diametro = EXCLUDED.exige_diametro;

-- SOLICITACOES

INSERT INTO solicitacoes (
    solicitacao_id,
    usuario_id,
    material_id,
    quantidade,
    status,
    criada_em,
    atualizada_em,
    previsao_retirada,
    observacao,
    dimensao,
    diametro
) VALUES
(
    1, 1, 1, 2,
    'PENDENTE',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP + INTERVAL '2 days',
    'Material para desenvolvimento de maquete',
    '600 x 400 mm',
    NULL
),
(
    2, 2, 2, 10,
    'EM_SEPARACAO',
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP + INTERVAL '1 day',
    'Material para atividade academica',
    NULL,
    NULL
),
(
    3, 3, 3, 3,
    'CONCLUIDO',
    CURRENT_TIMESTAMP - INTERVAL '7 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    'Material utilizado em projeto de pintura',
    NULL,
    NULL
),
(
    4, 1, 4, 2,
    'AGUARDANDO_COMPRA',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP + INTERVAL '3 days',
    'Barras para montagem de estrutura',
    '2 metros',
    10.00
),
(
    5, 2, 5, 5,
    'CANCELADO',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    NULL,
    'Solicitacao cancelada para atividade',
    NULL,
    NULL
)
ON CONFLICT (solicitacao_id) DO UPDATE SET
    usuario_id = EXCLUDED.usuario_id,
    material_id = EXCLUDED.material_id,
    quantidade = EXCLUDED.quantidade,
    status = EXCLUDED.status,
    criada_em = EXCLUDED.criada_em,
    atualizada_em = EXCLUDED.atualizada_em,
    previsao_retirada = EXCLUDED.previsao_retirada,
    observacao = EXCLUDED.observacao,
    dimensao = EXCLUDED.dimensao,
    diametro = EXCLUDED.diametro;

-- AJUSTE DAS SEQUENCIAS

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