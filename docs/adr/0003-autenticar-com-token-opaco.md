# ADR 0003: autenticar com token opaco guardado no banco

**Status:** proposto (aguardando aprovacao do grupo)

**Contexto:** O CT01 devolve um campo chamado `token`, com o exemplo "token de sessao". As duas palavras aparecem no mesmo lugar e nenhum documento do projeto decide entre as duas coisas, que sao diferentes: na sessao o servidor guarda quem esta logado e o navegador leva um cookie sozinho; no token o servidor nao guarda nada e o frontend envia a credencial a cada chamada. O frontend em React roda numa porta diferente da API, o que torna o uso de cookie mais trabalhoso e expoe o sistema a CSRF.

**Decisao:** Autenticar por token opaco. No login o backend gera um UUID, grava numa tabela `sessoes` junto com o usuario e a validade, e devolve esse valor no campo `token` do CT01. O frontend envia o token em cada requisicao e o backend confere na tabela antes de atender.

**Alternativas consideradas:**
- JWT: descartado porque exige biblioteca externa (jjwt) que nao faz parte do material da disciplina, e porque um token assinado nao pode ser revogado sem guardar estado assim mesmo.
- Sessao com cookie e Spring Security: descartado porque contradiz o campo `token` do CT01, exige configuracao extra de CORS com credenciais e abre risco de CSRF.

**Consequencias:**
- Positivas: nao depende de biblioteca de fora; o logout funciona de verdade, apagando a linha da tabela; nao ha risco de CSRF porque nada viaja automatico no navegador; a conferencia do token cabe num interceptor escrito em Java puro, usando `interface` e `implements`, que a disciplina ensina.
- Negativas: cada requisicao faz uma consulta a mais no banco; a tabela `sessoes` precisa ser criada pela Lara; sessoes vencidas precisam ser limpas em algum momento.
