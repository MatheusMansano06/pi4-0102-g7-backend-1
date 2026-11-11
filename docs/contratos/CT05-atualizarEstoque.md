# CT05: atualizarEstoque

> Complemento #1154: o saldo e a data são gravados na mesma transação. A
> rota segue desativada por padrão até a validação integrada.

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E5 |
| Como chamar | `PATCH /api/materiais/{materialId}/estoque` |
| Autenticação | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro (na URL) | sim | 7 |
| quantidadeDisponivel | inteiro não negativo | sim | 24 |

Exemplo do corpo JSON:

```json
{
  "quantidadeDisponivel": 24
}
```

A operação substitui o saldo atual pelo valor informado: saldo 10 com entrada 24
resulta em 24, não em 34. Zero é válido. Campo ausente, `null`, fração, texto,
booleano e valores fora do intervalo de um inteiro Java são recusados; o backend
não arredonda nem converte texto em quantidade.

### Estado da implementação

**Entrega parcial do #1154:** a rota não é registrada na configuração padrão.
O catálogo continua disponível. O controller de estoque é separado e ativado
somente na suíte de testes pela propriedade `archflow.estoque.em-desenvolvimento=true`.
Não definir essa propriedade nos ambientes compartilhados ou de apresentação.

Quando habilitada, a operação exige RESPONSAVEL identificado pelo JWT do
CT01. X-Perfil não autentica. Os testes isolados usam identidade de teste;
a prova integrada exige PostgreSQL com o esquema atualizado.
Ver [escopo e pendências da entrega parcial](../ESTOQUE-PARCIAL.md).

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro | sim | 7 |
| quantidadeDisponivel | inteiro | sim | 24 |
| atualizadaEm | data e hora persistida (ISO 8601) | sim | 2026-10-13T12:05:00 |

Sucesso: HTTP 200 com a data gravada junto com a quantidade. O backend
registra seu horário local com precisão de segundos, sem deslocamento de fuso
no JSON. O horário não é recebido da tela. A coluna de materiais pode ficar
NULL até a primeira alteração de estoque; o PATCH bem-sucedido devolve a data.

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Token ausente, inválido ou expirado | 401 | Erro `NAO_AUTENTICADO` |
| Perfil diferente de RESPONSAVEL | 403 | Erro `ACESSO_NEGADO` |
| Material inexistente | 404 | Erro `MATERIAL_NAO_ENCONTRADO` |
| Quantidade ausente, negativa ou inválida; corpo ilegível | 400 | Erro `QUANTIDADE_INVALIDA` |
| Falha ao gravar | 500 | Erro `ERRO_INTERNO` |

Os erros mantêm o formato `{ "codigo": "...", "mensagem": "..." }` do ADR 0004.
Banco PostgreSQL e credenciais reais são dependências da demonstração integrada;
os testes isolados da rota não comprovam essas integrações.
