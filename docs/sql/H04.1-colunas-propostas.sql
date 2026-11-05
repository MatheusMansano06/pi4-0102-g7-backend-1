-- PROPOSTA TECNICA PARA REVISAO COM A LARA; NAO E SCRIPT DE INICIALIZACAO.
-- Atualiza um banco que ja recebeu Tabelas.sql da PR #12 (main c14fb00).
-- O Tabelas.sql desta branch ja inclui as seis colunas para instalacoes novas.
-- Este arquivo fica em docs/, fora da pasta executada automaticamente pelo banco.
-- Revisar antes de aplicar em ambiente compartilhado; testar primeiro em copia.
-- IF NOT EXISTS permite repetir sem apagar dados; validate deve conferir os tipos.
-- Nao define quais materiais aceitam/exigem parametros: null preserva pendencia.
-- Nomes, tipos, precisao e tamanho de texto precisam de revisao conjunta antes do merge.

ALTER TABLE materiais
    ADD COLUMN IF NOT EXISTS aceita_dimensao BOOLEAN,
    ADD COLUMN IF NOT EXISTS exige_dimensao BOOLEAN,
    ADD COLUMN IF NOT EXISTS aceita_diametro BOOLEAN,
    ADD COLUMN IF NOT EXISTS exige_diametro BOOLEAN;

-- VARCHAR(255) corresponde ao mapeamento String atual; nao e limite de negocio aprovado.
-- DOUBLE PRECISION corresponde ao Double proposto; precisao de negocio ainda pendente.
ALTER TABLE solicitacoes
    ADD COLUMN IF NOT EXISTS dimensao VARCHAR(255),
    ADD COLUMN IF NOT EXISTS diametro DOUBLE PRECISION;

-- Sem DEFAULT nas politicas e sem UPDATE dos materiais existentes.
-- O POST devolve CONFIGURACAO_PENDENTE se algum dos quatro indicadores for null.
-- Nao acrescentar data_limite, senha_hash ou sessoes por suposicao.
