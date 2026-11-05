# CT12 consultar e persistir solicitações

Ficha interna corrigida pelo Luis no #1136, conciliada com o POST da PR #14.
Tipos físicos no [dicionário](../especificacao/colunas-materiais-e-solicitacoes.md).

| Linha | Valor |
| --- | --- |
| Fronteira | Backend ↔ PostgreSQL, sem endpoint HTTP |
| Quem chama | Backend API após validações |
| Atende | E3, E4, E6, E8 e E9 |
| Operações | inclusão, consulta e atualização de solicitações |

## Entrada e saída interna

| Campo | Tipo / coluna | Entrada | Recuperação |
| --- | --- | --- | --- |
| solicitacaoId | Long / solicitacao_id BIGINT | necessário para consultar/alterar | gerado na inclusão |
| usuarioId | Long / usuario_id BIGINT | identidade validada ao criar | usuário associado |
| materialId | Long / material_id BIGINT | material existente/ativo ao criar | relação com material |
| quantidade | Integer / quantidade INTEGER | inteira positiva | valor salvo |
| dimensao | String / dimensao VARCHAR(255), anulável | opcional para todos os materiais | texto salvo ou null |
| diametro | Double / diametro DOUBLE PRECISION, anulável | opcional, positivo/finito em mm | número salvo ou null |
| observacao | String / observacao VARCHAR(255), anulável | opcional | texto salvo ou null |
| status | texto / status VARCHAR(255) | PENDENTE na inclusão; transições de CT06 na alteração | estado salvo |
| criadaEm | LocalDateTime / criada_em TIMESTAMP | horário do servidor na inclusão | data original preservada |
| atualizadaEm | LocalDateTime / atualizada_em TIMESTAMP | igual à criação inicialmente; atualizada na alteração | horário salvo |
| previsaoRetirada | LocalDateTime / previsao_retirada TIMESTAMP, anulável | fluxo do responsável em CT06; não é dataLimite | valor salvo ou null |

## Regras de gravação e consulta

- Identidade vem da API validada, nunca de usuarioId livre no JSON CT03.
- Gravar material, quantidade, dimensão, diâmetro e observação juntos, sem
  sobrescrever especificacao do material. Ausência das duas medidas é permitida.
- Preservar os três opcionais e criadaEm nas mudanças de status/previsão.
- Confirmar gravação antes do sucesso HTTP 201 em CT03; erro não vira sucesso.
- Filtros de leitura respeitam identidade/perfil; DTOs HTTP de fila/histórico
  seguem seus contratos, sem prometer automaticamente todos os campos internos.
- Manter timestamps como no esquema existente; esta revisão não migra fuso.
- Não criar data_limite nem outras colunas fora do dicionário desta entrega.

## Erros

| Situação | Tratamento |
| --- | --- |
| Pedido inexistente | SOLICITACAO_NAO_ENCONTRADA |
| Dados inválidos | API valida conforme CT03/CT06 antes da operação |
| Falha ao consultar/gravar | erro de persistência tratado como ERRO_INTERNO, sem detalhes internos |

As duas medidas já existem na entidade e no script integrado pela PR #14.
A [atualização de banco existente](../sql/H04.1-colunas-propostas.sql) continua
necessária se o volume recebeu o SQL anterior; esta ficha não executa migração.
