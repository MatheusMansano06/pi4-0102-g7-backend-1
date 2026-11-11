Contratos técnicos: ArchFlow

Revisão do **#1136 — H01.4**, conduzida pelo Luis. Campos e política do MVP
foram definidos: medidas opcionais para todos os materiais. Ver o
[dicionário das seis colunas](../especificacao/colunas-materiais-e-solicitacoes.md),
[alinhamento](../especificacao/H01.4-alinhamento.md),
[autenticação](../especificacao/autenticacao.md) e
[requisitos de banco](../especificacao/requisitos-banco.md).
O login próprio do ADR 0008 já está no backend (CT01/CT10). O teste de ponta
a ponta da tela, API e banco continua no #1141.

CT01–CT09 e CT14 são HTTP. CT10–CT13 são interfaces internas com o banco, não endpoints.
Os opcionais do pedido precisam aparecer tanto no CT03 quanto no CT12. A lista
direta de CT02/CT04 foi mantida conforme o código já publicado; as formas das
rotas ainda não implementadas estão identificadas como propostas nas fichas.

1. Organização

CT01 — Autenticação.

CT02 — Listagem de materiais.

CT03 — Criação de solicitação.

CT04 — Listagem da fila de atendimento.

CT05 — Atualização de estoque.

CT06 — Atualização de status.

CT07 — Listagem de notificações.

CT08 — Listagem do histórico.

CT09 — Listagem de prazos.

CT10 — Acesso aos dados de usuários.

CT11 — Acesso aos materiais e estoque.

CT12 — Acesso às solicitações.

CT13 — Acesso às notificações.

CT14 — Cadastro de aluno pela tela. HTTP, rota pública, acrescentada em
11/11/2026 para a tela `StudentRegister` do frontend, que existia sem nenhuma
operação do backend para chamar. Card #1350, aberto depois da implementação.
