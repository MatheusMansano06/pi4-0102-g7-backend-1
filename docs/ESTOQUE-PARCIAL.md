# Estoque — entrega parcial da PR #8 / refs #1154

O usuário autorizou preparar uma entrega incompleta para revisão e entrada na
main. Esta PR prepara a operação de estoque, sem liberar o PATCH para uso real.
O card H07.1 continua incompleto; a integração pela tela pertence ao H07.3/#1156.

## O que pode ser revisado nesta entrega

- Entrada de quantidade inteira não negativa, sem converter fração ou texto.
- Substituição do saldo (10 com entrada 24 passa a 24), incluindo zero.
- Busca de material, resposta e tratamento dos erros da operação.
- Testes das regras/HTTP com repositório simulado e da configuração padrão.
- Catálogo separado da alteração: GET continua disponível sem ativar o PATCH.

O `MaterialEstoqueController` usa `ConditionalOnProperty`, da infraestrutura
Spring Boot já presente, para não registrar a rota por padrão. A propriedade
`archflow.estoque.em-desenvolvimento` só é ligada na suíte `MaterialEstoqueTest`.
Não foram adicionadas dependências nem configurações aos ambientes de execução.
O SQL, o mapeamento JPA de Material e ddl-auto: validate permanecem sem alteração.

## O que falta antes de liberar a alteração de estoque

1. Alinhar a coluna de data com a responsável pelo banco: nome, tipo, fuso e
   tratamento dos materiais existentes, sem inventar uma data histórica.
2. Gravar quantidade e data juntas; a resposta deve usar a data persistida.
   O horário calculado apenas no DTO atual ainda não cumpre CT05/CT11.
3. Integrar a identidade Keycloak e exigir RESPONSAVEL, substituindo o X-Perfil
   provisório. Decidir essa ligação com o H02.2 do Matheus.
4. Validar no PostgreSQL oficial que saldo e data permanecem após nova consulta
   e que falhas não produzem gravação parcial.
5. Testar ausência/invalidade de credencial e recusa ao ALUNO, além do sucesso
   do RESPONSAVEL. Só então revisar a remoção do bloqueio em outra alteração.

Não ativar a propriedade de desenvolvimento em ambientes compartilhados ou de
apresentação para contornar essas pendências. Esta entrega não instala Keycloak,
não cria o banco e não retoma a etapa Docker da PR #9, cancelada pelo usuário.

## Revisão e rastreabilidade

Os novos commits ficam na mesma branch h07-1-atualizar-estoque e PR #8, com
refs #1154 e tempo efetivo de sessão assistida. Aprovar esta entrega parcial não
significa concluir o card ou comprovar a integração com banco/identidade reais.
As mudanças de CT05 devem ser conciliadas com a PR documental #10 quando integrada.
