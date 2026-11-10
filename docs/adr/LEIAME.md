# Decisoes de arquitetura (ADR)

Cada arquivo registra uma decisao: o contexto, a decisao, o que foi descartado e o que isso
custa. Serve para ninguem precisar perguntar depois "por que foi feito assim".

| ADR | Assunto | Onde esta |
| :--- | :--- | :--- |
| 0001 | Java com Spring Boot no backend | aqui |
| 0002 | React no frontend | repositorio do frontend |
| 0003 | Proposta de token opaco não adotada | substituída pelo ADR 0007 |
| 0004 | Padrao de erros da API | aqui |
| 0005 | Criar o banco por script SQL | aqui |
| 0006 | Ambiente Docker com dois repositorios | aqui |
| 0007 | Keycloak (decisão histórica, substituída) | [decisão](0007-adotar-keycloak.md) |
| 0008 | Login próprio e JWT assinado pelo backend (vigente) | [decisão](0008-login-proprio-sem-keycloak.md) |

O ADR 0002 foi apagado por engano do repositorio do frontend, no commit que criou o projeto
React. Ele continua no historico do git e precisa ser restaurado.
