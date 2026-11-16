# CT09 — listarPrazos

Operação publicada pela Lara e adotada no painel E9.

`GET /api/solicitacoes/prazos`, autenticado por Bearer JWT. Sem parâmetros. Retorna HTTP 200 e uma lista direta, inclusive `[]` quando vazia, no mesmo formato de CT08: `solicitacaoId`, `material`, `quantidade`, `dimensao`, `diametro`, `status`, `criadaEm`, `atualizadaEm` e `previsaoRetirada` opcional. Confira os DTOs do histórico para os campos completos.

ALUNO consulta somente seus pedidos. RESPONSAVEL mantém a abrangência do serviço de histórico. Credencial ausente/inválida recebe 401/NAO_AUTENTICADO; perfil não permitido, 403/ACESSO_NEGADO; falha interna, 500/ERRO_INTERNO. Erros têm `codigo` e `mensagem`.

O painel do aluno exclui CANCELADO, preserva pedidos das outras etapas e mostra Sem previsão quando o servidor retorna null. Não usa `/api/prazos` nem envelope `solicitacoes`, que pertenciam à proposta anterior. Não inventa prazo nem `dataLimite`.

Ver [H01.5](../especificacao/H01.5-fechamento.md).
