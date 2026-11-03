# CT05: atualizarEstoque

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E5 |
| Como chamar | `PATCH /api/materiais/{materialId}/estoque` |
| Autenticação proposta | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

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

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro | sim | 7 |
| quantidadeDisponivel | inteiro | sim | 24 |
| atualizadaEm | data e hora persistida (ISO 8601) | sim | 2026-10-13T12:05:00 |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Perfil diferente de RESPONSAVEL | 403 | Erro `ACESSO_NEGADO` |
| Material inexistente | 404 | Erro `MATERIAL_NAO_ENCONTRADO` |
| Quantidade negativa ou inválida | 400 | Erro `QUANTIDADE_INVALIDA` |
| Falha ao gravar | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida segue a proposta comum de HTTP 401 / NAO_AUTENTICADO.

## Regra de gravação

O responsável substitui o saldo: 10 com entrada 24 resulta em 24. Zero é válido;
ausência, null, fração, texto e quantidade negativa retornam QUANTIDADE_INVALIDA.
CT11 grava quantidadeDisponivel e atualizadaEm juntos; CT05 devolve o horário
persistido. Sucesso proposto: HTTP 200, somente após gravação confirmada.

A PR #8 tem a rota/validações, mas gera horário apenas no DTO e usa X-Perfil.
Isso não conclui a gravação da data nem a autenticação requeridas. Sua documentação
e implementação devem ser atualizadas após esta revisão, não reduzindo o requisito.
Campo, precisão/fuso e inicialização de datas legadas estão na [lista para o banco](../especificacao/requisitos-banco.md).
