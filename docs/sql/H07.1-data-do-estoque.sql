-- #1154: alinhar destino e aplicação com H01.2 antes de subir o backend.
-- Materiais antigos ficam sem data até a próxima atualização pelo CT05.
-- Não altera saldo ou pedidos e não habilita a rota de estoque.
BEGIN;

ALTER TABLE materiais
    ADD COLUMN IF NOT EXISTS atualizada_em TIMESTAMP WITHOUT TIME ZONE;

COMMIT;
