-- #1154: conferência somente de leitura, na conexão confirmada pela equipe.
-- Executar com ON_ERROR_STOP no psql. Não migra nem habilita CT05.
BEGIN TRANSACTION READ ONLY;

SELECT current_database() AS banco,
       current_schema() AS esquema,
       current_setting('server_version') AS versao_postgresql;

-- Resultado ausente indica tabela/coluna ausente neste esquema.
-- Esperado: timestamp without time zone, YES e default NULL.
SELECT table_schema, table_name, column_name, data_type,
       is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = current_schema()
  AND table_name = 'materiais'
  AND column_name = 'atualizada_em';

COMMIT;
