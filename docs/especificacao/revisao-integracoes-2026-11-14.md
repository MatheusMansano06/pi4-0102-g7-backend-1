# #1136 — início da revisão após as integrações

Início autorizado pelo Luis em 14/11/2026, após a conclusão do #1156.
Base backend `85d3296`; frontend main `af5c775`. Branch
`h01-4-revisao-integracoes`. Escopo documental, sem mudanças de código/banco.

## Diferenças já conciliadas neste incremento

| Tema | Estado comprovado | Referência |
| --- | --- | --- |
| Login #1141 | Sessão/perfil/logout/expiração e Bearer integrados; card Closed | [PR frontend 24](https://github.com/anadiasxavier/pi4-0102-g7-frontend/pull/24) |
| Pedido #1147 | Formulário envia CT03; PENDENTE/aluno/data/opcionais comprovados em banco isolado; card Closed | [PR frontend 25](https://github.com/anadiasxavier/pi4-0102-g7-frontend/pull/25) |
| Estoque #1156 | PATCH pela tela e releitura do catálogo comprovados em instância isolada habilitada para teste | [PR frontend 26](https://github.com/anadiasxavier/pi4-0102-g7-frontend/pull/26) |
| Catálogo CT02 | Bearer já enviado; tela de estoque respeita status da API, inclusive mínimo por material | [Contrato CT02](../contratos/CT02-listarMateriais.md) |
| Medidas CT03 | Opcionais para todos; largura/altura compõem texto em mm, diâmetro independente | [Contrato CT03](../contratos/CT03-criarSolicitacao.md) |

As provas publicadas no frontend ficam em `docs/integracao/H02.4-login-validado.md`,
`H04.3-pedido-validado.md` e `H07.3-estoque-validado.md`, com resultados/capturas.
São provas PostgreSQL 16.15 isoladas, sem demonstração Docker/Linux/PG15 ou
alteração no banco compartilhado. Não representam migração no destino.

## O que ainda exige revisão para concluir o #1136

1. Conferir CT14, campos novos de usuário e correspondência entre contratos,
   dicionário, respostas atuais e fichas/fluxos da equipe.
2. Conciliar CT08/CT09 e o uso de prazos/previsão no frontend. Não atribuir
   significado a dataLimite: continua sem contrato e saiu do formulário.
3. Revisar projeção de parâmetros aceitos/obrigatórios do catálogo: definidos
   como alvo, ainda ausentes no DTO. Não assumir implementação de Lara.
4. Atualizar seletivamente as tabelas antigas de H01.4 e demais referências que
   ainda apresentem login/POST pela tela como futuros; preservar evidências.
5. Reavaliar critérios originais antes de encerrar o card. Este início não é
   revisão completa das fichas acadêmicas nem autorização de merge.

CT05 continua desligado por padrão. #1154 conserva a pendência de implantação/
migração coordenada com H01.2/Lara. #1161/PR backend22 continua dependendo do
esquema oficial de notificações e de H09.1/Lara; não retomar por suposição.

Validação deste incremento: revisão do diff e conferência de links locais.
Sem alteração executável, não foram repetidas suítes de código.

Esforço humano: 12h30 anteriores do Luis no #1136 preservadas, além das 2h
anteriores de outra conta. Duração efetiva desta retomada não informada;
nenhum novo apontamento numérico ou alteração retroativa.
