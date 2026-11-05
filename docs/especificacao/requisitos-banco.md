# Requisitos de banco definidos no H01.4

Luis entrega a especificação #1136; Lara implementa/revisa H01.2 e combina a
aplicação no destino. O [dicionário](colunas-materiais-e-solicitacoes.md) define
as seis colunas. Não há pendência de a Lara escolher regras: o Luis confirmou
as duas medidas opcionais para todos os materiais do MVP.

Base: main 6181761, com banco, histórico, POST parcial e integração do catálogo.
O Java continua em ddl-auto: validate.

| Conjunto | Definição / situação |
| --- | --- |
| materiais | Campos básicos existentes; quatro BOOLEAN anuláveis, com defaults TRUE/FALSE/TRUE/FALSE e configuração explícita dos exemplos nesta revisão. |
| solicitacoes | dimensao VARCHAR(255), diametro DOUBLE PRECISION, anuláveis; observacao VARCHAR(255) já existente. Identidade, PENDENTE e datas do servidor. |
| parametrosAceitos / parametrosObrigatorios | Projeções dos booleanos, sem colunas/arrays adicionais. Ainda ausentes no DTO do catálogo. |
| Data do estoque | Persistência de atualizadaEm é outro recorte #1154; não inferir horário passado. |
| usuarios / Keycloak | Vínculo com usuarioId e origem dos perfis dependem de H02.1/H02.2; não criar senha_hash/sessoes da proposta própria superada. |
| notificacoes | Detalhamento CT13/H09.1 é da Lara; consulta #1161 é do Luis. Não entregues aqui. |

## Banco existente

1. Combinar destino e cópia de segurança; conferir os tipos com o dicionário.
2. Aplicar o [script](../sql/H04.1-colunas-propostas.sql): adiciona campos,
   estabelece defaults e configura as quatro regras em todos os materiais.
3. O UPDATE muda só as regras, sem apagar dados ou alterar saldo, ativo, pedidos
   e datas. Repetição preserva o resultado. Tipos incompatíveis preexistentes
   exigem correção específica; IF NOT EXISTS não os converte.
4. Conferir e iniciar com validate. Arquivo novo não migra um volume inicializado.
5. Testar em ambiente dedicado. Pedido real ainda depende de identidade Keycloak.

Não executar DROP/recriar volume. A aplicação no banco compartilhado continua
coordenada com H01.2; esta entrega prepara o script, sem executá-lo nesse banco.
