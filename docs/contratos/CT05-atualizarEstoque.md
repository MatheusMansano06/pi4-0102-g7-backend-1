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

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro | sim | 7 |
| quantidadeDisponivel | inteiro | sim | 24 |
| atualizadaEm | data e hora (ISO 8601) | sim | 2026-10-13T12:05:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Usuário sem autenticação | Erro `NAO_AUTENTICADO` |
| Perfil diferente de RESPONSAVEL | Erro `ACESSO_NEGADO` |
| Material inexistente | Erro `MATERIAL_NAO_ENCONTRADO` |
| Quantidade negativa ou inválida | Erro `QUANTIDADE_INVALIDA` |
| Falha ao gravar | Erro `ERRO_INTERNO` |
