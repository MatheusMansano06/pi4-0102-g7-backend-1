# CT03 criarSolicitacao

Definição #1136 conciliada com o POST parcial integrado pela PR #14.
[Campos e regras](../especificacao/colunas-materiais-e-solicitacoes.md) definidos
pelo Luis; [integração de identidade](../H04.1-CRIAR-SOLICITACAO.md) ainda necessária.

| Linha | Valor |
| --- | --- |
| Fronteira | HTTP, frontend → Backend API |
| Atende | E3 |
| Chamada | POST /api/solicitacoes |
| Acesso | ALUNO; identidade validada no servidor, a integrar com Keycloak/H02.2 |
| Transporte alvo da credencial | Authorization: Bearer; não confundir header livre com contexto validado |
| Sucesso | HTTP 201 após gravação confirmada |

## Entrada

| Campo | Tipo | Obrigatório | Regra |
| --- | --- | --- | --- |
| materialId | inteiro positivo, Long | sim | material existente e ativo |
| quantidade | inteiro positivo, Integer | sim | sem arredondar ou converter texto |
| dimensao | texto, capacidade 255 caracteres | não | mm; não apenas espaços; exemplo 300 x 200 mm |
| diametro | número JSON positivo/finito | não | mm, exemplo 4.5; não enviar unidade no número |
| observacao | texto, capacidade 255 caracteres | não | preservado no pedido |

Dimensão e diâmetro são permitidos e opcionais para TODOS os materiais do MVP,
conforme confirmação do Luis em 04/11/2026. Pode enviar ambos, um ou nenhum.
Ausente/null é permitido; configuração NULL no material não equivale a essa
opcionalidade e continua sendo erro 503 até atualizar/configurar o banco.

```json
{
  "materialId": 1,
  "quantidade": 2,
  "dimensao": "300 x 200 mm",
  "diametro": 4.5,
  "observacao": "Uso em maquete"
}
```

Também é válido:

```json
{
  "materialId": 1,
  "quantidade": 2
}
```

Não aceita outros campos: usuarioId, perfil, status, criadaEm, atualizadaEm,
dataLimite e nomes desconhecidos recebem DADOS_INVALIDOS. PENDENTE, aluno e datas
são definidos no servidor. dataLimite não é previsaoRetirada.

## Saída

| Campo | Tipo | Regra |
| --- | --- | --- |
| solicitacaoId | inteiro | gerado pelo banco |
| materialId | inteiro | material salvo |
| quantidade | inteiro | quantidade salva |
| status | texto | sempre PENDENTE na criação |
| criadaEm | data/hora ISO 8601 | horário do servidor |
| dimensao | texto ou null | valor salvo |
| diametro | número ou null | valor salvo |
| observacao | texto ou null | valor salvo |

## Erros

| Situação | HTTP | Código |
| --- | --- | --- |
| Identidade validada ausente | 401 | NAO_AUTENTICADO |
| Identidade não ALUNO | 403 | ACESSO_NEGADO |
| materialId/corpo/campo desconhecido inválido | 400 | DADOS_INVALIDOS |
| dimensao ou observacao acima de 255 na contagem Java | 400 | DADOS_INVALIDOS |
| Quantidade ausente, não inteira ou não positiva | 400 | QUANTIDADE_INVALIDA |
| Material inexistente/inativo | 404 | MATERIAL_NAO_ENCONTRADO |
| Medida inválida ou incompatível com política explicitamente configurada | 400 | PARAMETROS_INVALIDOS |
| Política NULL/incoerente no material | 503 | CONFIGURACAO_PENDENTE |
| Falha de gravação | 500 | ERRO_INTERNO |

Formato: objeto com codigo e mensagem. Binding de corpo ilegível pode gerar 400
antes do serviço; ordem de autenticação real será validada no filtro H02.2.
X-Perfil, X-Usuario-Id e nome de Principal genérico não autenticam este POST.

## Persistência e diferenças para a integração

CT12 grava pedido PENDENTE, horários inicialmente iguais, aluno validado e os
opcionais; medidas são preservadas em mudanças de status/previsão. Não baixa ou
reserva saldo e não cria notificações nesta operação.

A entrada valida dimensão e observação com String.length(): até 255 unidades
UTF-16, sem contar bytes, cortar ou aparar o texto. Acima disso, recusa antes
da persistência e identifica o campo na mensagem; null/ausência são permitidos.
A API não analisa os números no texto dimensao. O formulário
compõe largura/altura em mm na futura integração #1147. Keycloak ainda precisa
produzir UsuarioAutenticado; corpo válido sem esse contexto recebe 401.
