# #1136 — revisão documental concluída após as integrações

Início autorizado pelo Luis em 14/11/2026, após a conclusão do #1156.
Base backend `85d3296`; frontend main `af5c775`. Branch
`h01-4-revisao-integracoes`. Escopo documental, sem mudanças de código/banco.
Conclusão documental em **15/11/2026**, incluindo contratos backend e fichas,
matriz/fluxos frontend na branch `h01-4-concluir-fichas`. As duas PRs precisam
de revisão e autorização específica antes de merge/fechamento do card.

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

## Critérios da revisão conferidos

- CT01/CT14: curso presente na sessão, cadastro público somente ALUNO;
  nome/RA/curso/email/senha e correspondência com usuarios/BCrypt. RA/curso já
  mapeados; matrícula/cargo fora do cadastro público. Validação de senha usa
  String.length() e não comprova limite de bytes Unicode; limites físicos
  RA30/curso120 não têm antecipação completa no serviço. Limitações registradas,
  sem modificar autenticação de Matheus ou prometer correção de código.
- CT04/CT08: nome do aluno, dimensão e previsão nas respostas; fila também
  projeta diâmetro/observação. Null não é texto vazio. Escopo por identidade
  do token preservado. Fila sem filtro ainda inclui CANCELADO, explicitado.
- CT08/CT09/E8/E9: E9 usa CT08 e exclui CANCELADO, incluindo os demais estados.
  CT09 separado é proposto. Previsão ausente aparece como Sem previsão;
  dataLimite não é definida ou persistida. O rótulo retirada de E8 deriva de
  atualizadaEm/criadaEm, sem data própria de retirada efetiva comprovada.
- CT02/CT11: metadados aceitos/obrigatórios continuam alvo definido, ausente no
  DTO; regra uniforme de medidas opcionais preservada. Implementar a projeção
  continua no escopo de H03.1/Lara, não é entrega de código desta revisão.
- Tabelas H01.4, matriz, fichas e fluxos versionados atualizados juntos. Avisos
  antigos sobre seed/DEMO_SEM_LOGIN identificados como históricos: PR28 já
  corrigiu a carga. Provas frontend24/25/26 referenciadas sem repetição.

Critérios originais do #1136 tratam de especificação, fichas e fluxos. As fontes
acadêmicas originais (PDFs/relatório) são preservadas; esta revisão atualiza os
documentos versionados que traduzem essas fontes, sem afirmar nova aprovação
acadêmica ou alterar escopo de colega. Pendências de implementação/implantação
são explicitadas, não ocultadas para declarar documentação pronta.

CT05 continua desligado por padrão. #1154 conserva a pendência de implantação/
migração coordenada com H01.2/Lara. #1161/PR backend22 continua dependendo do
esquema oficial de notificações e de H09.1/Lara; não retomar por suposição.

Validação: confronto com DTOs/serviços, DDL/carga e componentes das bases
indicadas, diff, links locais, cercas Markdown e blocos JSON dos documentos
alterados. Sem alteração executável, não foram repetidas suítes de código.

Esforço humano: 12h30 anteriores do Luis no #1136 preservadas, além das 2h
anteriores de outra conta. Duração efetiva desta retomada não informada;
nenhum novo apontamento numérico ou alteração retroativa.
