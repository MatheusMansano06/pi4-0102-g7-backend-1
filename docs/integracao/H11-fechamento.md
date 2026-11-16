# H11 — fechamento e validação

Autorização do Luis em 15/11/2026: seguir código da Lara e concluir integrações, documentação, PRs, merges, releases e cards com evidências.

Validação executada antes da publicação: 139 testes backend aprovados, zero falhas/erros/ignorados. Inclui PostgreSQL real com DDL oficial e testes HTTP de cadastro de material, autorização, medidas opcionais e recusa de quantidades JSON inválidas; geração/consulta de avisos por destinatário e rollback transacional. Cinco bancos descartáveis removidos, contagens finais zeradas e instância própria encerrada.

Frontend: 48 testes aprovados antes da revisão final; lint e build aprovados. Resultado final e ensaio de instalação serão registrados abaixo após sua execução. Não confundir a prova PostgreSQL 16 portátil com o ensaio Compose PostgreSQL 15.

Não houve alteração em banco compartilhado nem novo lançamento de horas. Os registros anteriores e as atribuições permanecem preservados.

## Ensaio de instalação limpa (Compose)

Projeto exclusivo archflow-fechamento-1511, imagens Linux compiladas dos dois repositórios, PostgreSQL 15 e volume inicialmente vazio. Não é uma segunda máquina física: é uma instalação Docker limpa, sem exigir Java/Maven/Node na máquina hospedeira. Inicialização automática do esquema e exemplos; login de ALUNO e RESPONSAVEL pelo nginx aprovado após preservar Host com porta no encaminhamento.

Prova HTTP: 22 chamadas aprovadas pela mesma origem localhost:8081, com autenticação dos perfis, isolamento entre alunos, dois avisos oficiais do pedido, previsão persistida, filtro de fila, recusas 401/403/400, medidas opcionais em material novo e estoque 0/12. Pedido/status não descontaram saldo. Resposta de PATCH contém data e saldo; GET do catálogo relê saldo; SQL confirmou a data persistida. Evidência em H11-http-resultados.json.

Pela interface, pedido #8 de Cola Branca criado com observação e sem medidas, avançado até CONCLUIDO pelo responsável com previsão 18/11/2026 14:00. SQL confirmou status/previsão e PEDIDO_EM_SEPARACAO, PEDIDO_PRONTO e PEDIDO_CONCLUIDO. Estoque de Cola Branca alterado manualmente de50 para48, com saldo relido e data confirmada na tela. Capturas ficam na documentação H11 do frontend.

Frontend final: 51 testes, lint e build aprovados. Backend: 139 testes, sem ignorados; build Maven/JAR em Docker aprovado. Nenhuma mudança adicional de código backend depois da suíte, apenas espaço final/comentários/documentação.

O ambiente de demonstração é local, isolado e preservado para conferência. Credenciais de banco/JWT ficam em arquivo privado fora dos repositórios e não integram evidências ou releases. Não foi executada migração em banco compartilhado.
