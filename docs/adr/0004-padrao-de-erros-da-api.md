# ADR 0004: padrao de erros da API

**Status:** proposto (aguardando aprovacao do grupo)

**Contexto:** Os treze contratos descrevem os erros apenas por um codigo em portugues e letras maiusculas, como `NAO_AUTENTICADO` e `QUANTIDADE_INVALIDA`. Nenhum deles diz qual numero HTTP acompanha cada erro, e nenhum mostra o formato do corpo da resposta de erro. Sem isso o frontend nao sabe o que tratar, e qualquer regra de negocio violada acaba virando erro 500, que significa "o servidor quebrou".

**Decisao:** Manter os codigos em portugues que os contratos ja definem e associar cada um a um numero HTTP, devolvendo sempre o mesmo formato de corpo.

| Numero | Quando | Codigos dos contratos |
| :--- | :--- | :--- |
| 400 | o cliente mandou dado invalido | `DADOS_INVALIDOS`, `QUANTIDADE_INVALIDA`, `PARAMETROS_INVALIDOS`, `STATUS_INVALIDO`, `PREVISAO_INVALIDA` |
| 401 | nao esta autenticado | `NAO_AUTENTICADO`, `CREDENCIAIS_INVALIDAS` |
| 403 | esta autenticado mas o perfil nao permite | `ACESSO_NEGADO`, `USUARIO_INATIVO` |
| 404 | o recurso nao existe | `MATERIAL_NAO_ENCONTRADO`, `SOLICITACAO_NAO_ENCONTRADA` |
| 500 | falha inesperada | `ERRO_INTERNO` |

Corpo de toda resposta de erro:

```json
{ "codigo": "QUANTIDADE_INVALIDA", "mensagem": "A quantidade deve ser maior que zero." }
```

**Alternativas consideradas:**
- Devolver sempre 200 com o codigo dentro do corpo: descartado porque o navegador e as ferramentas de rede deixam de distinguir sucesso de falha.
- Usar apenas os numeros HTTP, sem codigo em portugues: descartado porque os contratos ja nomeiam os erros e o frontend precisa distinguir dois erros que compartilham o mesmo numero.

**Consequencias:**
- Positivas: o frontend trata erro pelo numero e mostra a mensagem certa; os contratos continuam valendo como estao; a conversao fica num lugar so, numa classe `TratadorDeErros`.
- Negativas: os treze contratos precisam ganhar uma coluna com o numero, o que da retrabalho de documentacao.
