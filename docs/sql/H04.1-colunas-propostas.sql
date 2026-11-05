-- PROPOSTA TECNICA PARA REVISAO COM A LARA; NAO E SCRIPT DE INICIALIZACAO.
-- Base: banco/Tabelas.sql da PR #12, commit ac646019.
-- Este arquivo fica em docs/, fora da pasta executada automaticamente pelo banco.
-- Aplicar uma vez apenas em banco dedicado de teste/revisao.
-- Nao define quais materiais aceitam/exigem parametros: null preserva pendencia.
-- Nomes, tipos, precisao e tamanho de texto precisam de revisao conjunta antes do merge.

ALTER TABLE materiais
    ADD COLUMN aceita_dimensao BOOLEAN,
    ADD COLUMN exige_dimensao BOOLEAN,
    ADD COLUMN aceita_diametro BOOLEAN,
    ADD COLUMN exige_diametro BOOLEAN;

-- VARCHAR(255) corresponde ao mapeamento String atual; nao e limite de negocio aprovado.
-- DOUBLE PRECISION corresponde ao Double proposto; precisao de negocio ainda pendente.
ALTER TABLE solicitacoes
    ADD COLUMN dimensao VARCHAR(255),
    ADD COLUMN diametro DOUBLE PRECISION;

-- Sem DEFAULT nas politicas e sem UPDATE dos materiais existentes.
-- O POST devolve CONFIGURACAO_PENDENTE se algum dos quatro indicadores for null.
-- Nao acrescentar data_limite, senha_hash ou sessoes por suposicao.
