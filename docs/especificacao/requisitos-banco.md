# Requisitos de banco definidos no H01.4

Luis entrega a especificação #1136; Lara implementa/revisa H01.2 e combina a
aplicação no destino. O [dicionário](colunas-materiais-e-solicitacoes.md) define
as seis colunas. Não há pendência de a Lara escolher regras: o Luis confirmou
as duas medidas opcionais para todos os materiais do MVP.

Base atual: main e0e81ac, com a autenticação própria da PR #19.
O Java continua em ddl-auto: validate.

| Conjunto | Definição / situação |
| --- | --- |
| materiais | Campos básicos existentes; quatro BOOLEAN anuláveis, com defaults TRUE/FALSE/TRUE/FALSE e configuração explícita dos exemplos nesta revisão. |
| solicitacoes | dimensao VARCHAR(255), diametro DOUBLE PRECISION, anuláveis; observacao VARCHAR(255) já existente. Identidade, PENDENTE e datas do servidor. |
| parametrosAceitos / parametrosObrigatorios | Projeções dos booleanos, sem colunas/arrays adicionais. Ainda ausentes no DTO do catálogo. |
| Data do estoque | #1154 mapeia materiais.atualizada_em (TIMESTAMP WITHOUT TIME ZONE, anulável, sem default). Em banco existente, revisar/aplicar com H01.2 antes de iniciar a versão, mesmo com o PATCH desativado. |
| usuarios | CT10/ADR 0008: usuario_id gerado, nome, email, senha_hash BCrypt, perfil ALUNO ou RESPONSAVEL e ativo booleano. A entidade existe; os scripts versionados ainda não criam a tabela. Implementação e aplicação: Lara/#1134. |
| notificacoes | Detalhamento CT13/H09.1 é da Lara; consulta #1161 é do Luis. Não entregues aqui. |

## Banco existente

1. Combinar destino e cópia de segurança; conferir os tipos com o dicionário.
2. Aplicar o [script](../sql/H04.1-colunas-propostas.sql): adiciona campos,
   estabelece defaults e configura as quatro regras em todos os materiais.
3. O UPDATE muda só as regras, sem apagar dados ou alterar saldo, ativo, pedidos
   e datas. Repetição preserva o resultado. Tipos incompatíveis preexistentes
   exigem correção específica; IF NOT EXISTS não os converte.
4. Conferir e iniciar com validate. Arquivo novo não migra um volume inicializado.
5. Para o backend atual iniciar com validate, conferir também a migração
   de usuarios; o script das seis colunas não cria essa tabela.
6. Testar em ambiente dedicado com contas ativas de aluno e responsável.
   Conciliar seus IDs com os pedidos de exemplo sem reatribuir dados existentes
   por suposição. Configurar a assinatura do JWT no backend.

CT10 lê o hash para conferir a senha no backend. A ficha CT01 não o expõe à
tela. O ADR 0008 não requer tabela de sessões.

Não executar DROP/recriar volume. A aplicação no banco compartilhado continua
coordenada com H01.2; esta entrega prepara o script, sem executá-lo nesse banco.
