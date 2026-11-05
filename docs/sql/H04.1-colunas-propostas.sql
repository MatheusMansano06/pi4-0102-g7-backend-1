-- ATUALIZACAO DEFINIDA NO #1136; APLICACAO COORDENADA COM H01.2/LARA.
-- Luis confirmou em 04/11/2026: dimensao e diametro permitidos e opcionais
-- para TODOS os materiais do MVP. Substitui a pendencia anterior de politica.
-- Dicionario: docs/especificacao/colunas-materiais-e-solicitacoes.md.
-- Nome historico mantido para preservar links. Fora da inicializacao automatica.
-- Revisar destino/copia de seguranca; testar primeiro em copia.
-- Muda esquema e quatro indicadores. Nao altera saldo, ativo, pedidos ou datas.
-- IF NOT EXISTS nao corrige tipos incompatíveis de colunas preexistentes.

BEGIN;

ALTER TABLE materiais
    ADD COLUMN IF NOT EXISTS aceita_dimensao BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS exige_dimensao BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS aceita_diametro BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS exige_diametro BOOLEAN DEFAULT FALSE;

-- Tambem configura defaults quando a versao anterior ja criou as colunas.
ALTER TABLE materiais
    ALTER COLUMN aceita_dimensao SET DEFAULT TRUE,
    ALTER COLUMN exige_dimensao SET DEFAULT FALSE,
    ALTER COLUMN aceita_diametro SET DEFAULT TRUE,
    ALTER COLUMN exige_diametro SET DEFAULT FALSE;

ALTER TABLE solicitacoes
    ADD COLUMN IF NOT EXISTS dimensao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS diametro DOUBLE PRECISION;

-- Decisao uniforme explicita: inclui materiais ativos/inativos ja existentes.
-- Sobrescreve regras divergentes por essa politica do MVP; nao adivinhar pelo nome.
-- Reexecutar mantem o mesmo resultado, sem novos materiais/pedidos.
UPDATE materiais
SET aceita_dimensao = TRUE,
    exige_dimensao = FALSE,
    aceita_diametro = TRUE,
    exige_diametro = FALSE
WHERE aceita_dimensao IS DISTINCT FROM TRUE
   OR exige_dimensao IS DISTINCT FROM FALSE
   OR aceita_diametro IS DISTINCT FROM TRUE
   OR exige_diametro IS DISTINCT FROM FALSE;

COMMIT;

-- Manter ddl-auto: validate. Nao acrescentar data_limite/senha_hash/sessoes.
-- Esta entrega prepara o SQL; nao aplica automaticamente no banco compartilhado.
