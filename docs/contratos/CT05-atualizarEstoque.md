# CT05: atualizarEstoque

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E5 |
| Como chamar | `PATCH /api/materiais/{materialId}/estoque` |

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

Os testes da operação ainda usam `X-Perfil: RESPONSAVEL`. Esse cabeçalho é
informado pelo próprio cliente e **não autentica o usuário**. O grupo escolheu
Keycloak; integrar a identidade e as permissões do H02.2 antes de liberar a rota.
Ver [escopo e pendências da entrega parcial](../ESTOQUE-PARCIAL.md).

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro | sim | 7 |
| quantidadeDisponivel | inteiro | sim | 24 |
| atualizadaEm | data e hora (ISO 8601) | sim | 2026-10-13T12:05:00 |

Contrato alvo: HTTP 200, devolvendo a data gravada junto com a quantidade.
Na implementação parcial exercitada pelos testes, `atualizadaEm` ainda é calculada
apenas na resposta. Isso **não atende** a persistência exigida no CT11/#1154.
Alinhar a coluna com o SQL da equipe antes de implementar e liberar a operação.

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação; provisoriamente, perfil ausente/vazio | 401 | Erro `NAO_AUTENTICADO` |
| Perfil diferente de RESPONSAVEL | 403 | Erro `ACESSO_NEGADO` |
| Material inexistente | 404 | Erro `MATERIAL_NAO_ENCONTRADO` |
| Quantidade ausente, negativa ou inválida; corpo ilegível | 400 | Erro `QUANTIDADE_INVALIDA` |
| Falha ao gravar | 500 | Erro `ERRO_INTERNO` |

Os erros mantêm o formato `{ "codigo": "...", "mensagem": "..." }` do ADR 0004.
Banco PostgreSQL e autenticação real são dependências da demonstração integrada;
os testes da rota substituem apenas o repositório e não comprovam essas integrações.
