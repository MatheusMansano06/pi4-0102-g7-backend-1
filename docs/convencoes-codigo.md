# Convencoes de codigo do backend

Base: o material da disciplina fornecido em `Conteúdo Java.zip`: aulas POO_01 a
POO_05, apostilas e exemplos Java. O ADR 0001 registra a infraestrutura Spring Boot
ja adotada pelo projeto; isso nao significa que essa infraestrutura seja ensinada
nos exemplos da disciplina.

As regras abaixo separam conteudo estudado de escolhas de estilo da equipe. Para
os proximos codigos, usar os conceitos demonstrados nesse material, sem buscar
solucoes externas ou introduzir recursos novos sem referencia e alinhamento.

## Estilo, em qualquer classe

Estas sao escolhas de consistencia do projeto. Nao sao uma lista do que foi ou nao
foi ensinado na disciplina.

- Nomes do dominio em portugues, sem acento: `solicitacaoId`, `quantidadeDisponivel`, `listarFila`.
  Ingles so no que e da linguagem ou do framework: `get`, `set`, `toString`, `equals`, `findBy`.
- Os nomes dos campos sao exatamente os dos contratos em `docs/contratos`. Assim o JSON sai certo sozinho.
- Quatro espacos de recuo, `{` na mesma linha, sem espaco antes de `(`.
- Imports escritos um por linha, nunca com `*`.
- Comentarios so com `//`, em portugues e com acento. Nunca `/* */`, nunca Javadoc.
- Comentario explica a regra, nao o obvio. Exemplo: `// A previsao nao pode ser no passado.`
- Ordem dentro da classe: atributos `private`, construtores, getters e setters, regras, `toString`.
- `this.` sempre explicito em construtor, getter e setter.
- Setter valida e recusa: `if (invalido) { throw new IllegalArgumentException("..."); }`.
- Metodo com ate 12 linhas. Passou disso, quebre em um metodo `private` com nome do que ele devolve.
- Por escolha de estilo, preferimos guardas a `else`: testa o caso errado, lanca ou retorna, segue o caminho certo.
- Mantemos a preferencia por `if` e `for` em vez de ternario, `switch` ou `while`, sem afirmar que os outros recursos nao foram ensinados.
- Nao introduzir Lombok, `record`, `var`, `Optional`, `stream` ou lambda: nao foram identificados nos materiais examinados.
- `enum`, `try/catch` e `throws` aparecem no material e podem ser usados quando a regra exigir, mantendo o padrao do arquivo existente. Nao converter todos os valores em constantes nem capturar excecoes sem necessidade.

## Referencias no material da disciplina

Os nomes abaixo identificam arquivos do pacote fornecido, nao arquivos novos a
adicionar ao repositorio.

| Conteudo | Referencia |
| :--- | :--- |
| Classes, construtores, encapsulamento, getters e setters | POO_01.pdf, POO_02.pdf e ContaBancaria.java |
| Referencias nulas, inicializacao e estado de instancia | POO_03.pdf |
| Wrappers e metodos de Object | POO_04.pdf |
| Heranca, polimorfismo, classes abstratas e interfaces | POO_05.pdf, exemploheranca (2).zip, exemplopolimorfismo.zip e pagamentos.zip |
| Vetores, ArrayList e genericos | ExemploArray.java e ClasseGenérica.zip |
| Validacao e excecoes, incluindo try/catch e throws | Livro.java e TryCatch.java |
| Enumeracoes | pagamentos.zip, arquivo StatusPagamento.java, e capitulo de enumeracoes das apostilas |
| Pacotes, imports e organizacao de programa | ApostilaAndrePOO.pdf e ApostilaProfAndre.pdf |

A atividade de revisao contem erros propositais. Usar o gabarito para compreender
as correcoes, sem copiar o codigo com falhas como implementacao pronta.

## O que entra em cada camada

- **Entity**: `@Entity`, construtor vazio (o JPA exige) e construtor completo que chama `this.setX(...)`,
  para o objeto ja nascer validado. Id so com getter. Para conjuntos fechados, como perfis e
  status, usar `enum` quando fizer sentido, conforme os exemplos e as enumeracoes ja existentes no backend.
- **DTO**: duas classes por contrato, `XxxEntrada` e `XxxSaida`. A Entrada tem construtor vazio e
  get/set sem validacao. A Saida tem um construtor que recebe a entidade e so getters.
- **Repository**: `interface XxxRepository extends JpaRepository<Xxx, Long>`, so com assinaturas.
  Para buscar um registro use `Xxx findByAlgumaCoisa(...)` e teste `null` com `if`. Nao use `findById`,
  que devolve `Optional`.
- **Service**: `@Service`, dependencias em atributos `private final` recebidos pelo construtor, sem `@Autowired`.
  Um metodo publico por contrato, com o mesmo nome do contrato. Passos numerados `// 1)`, `// 2)`.
  As regras de negocio e os codigos de erro dos contratos ficam aqui.
- **Controller**: `@RestController`, metodo de uma ou duas linhas que so chama o Service.
  Acima de cada metodo, o comentario do contrato: `// CT03: POST /api/solicitacoes`.
  Devolve DTO ou `List<XxxSaida>`. Nunca devolve Entity.
- **Erros**: a Entity lanca `IllegalArgumentException`. O Service lanca `NegocioException`, que carrega
  o codigo do contrato. A classe `TratadorDeErros` converte em resposta HTTP, conforme o ADR 0004.
- Listas: `List<T>` percorrida com for-each. Nunca `.stream()`.
- Service e Controller nao guardam estado. `static` so em constante.

## Infraestrutura ja presente e limites do material

`package`, `import`, interfaces e genericos sao conceitos presentes no material.
Isso nao significa que as bibliotecas importadas ou as interfaces do Spring tambem
sejam ensinadas nele. Por exemplo, uma interface Java e `JpaRepository` nao sao a
mesma referencia de aprendizagem.

O backend ja utiliza anotacoes Spring/JPA, `JpaRepository`, `List`, `LocalDateTime`,
`@RestControllerAdvice` e JUnit. As camadas descritas acima registram a organizacao
existente. Nao foram identificadas aulas de Spring/JPA, autenticacao por token ou
JUnit no pacote fornecido. Nao apresentar essas tecnologias como conteudo das aulas.

Esta revisao nao altera a infraestrutura nem libera novas dependencias. Antes de
acrescentar um uso que ultrapasse o material, explicitar a necessidade e alinhar
com o usuario e a equipe. Acrescentar um nome a este documento, sozinho, nao
constitui autorizacao. Documentar a decisao quando estiver confirmada.

## Branches, commits e revisao

- Nunca fazer commit ou push direto na `main`.
- Usar uma branch por entrega, seguindo a referencia do card quando ela estiver confirmada.
- Fazer as alteracoes e verificacoes, commitar e enviar a branch, e abrir uma PR com destino a `main`.
- A PR pode reunir varios commits do mesmo assunto. Novos commits na branch atualizam a mesma PR.
- A entrada na `main` exige PR e revisao; abrir a PR nao autoriza fazer o merge por conta propria.
- Sem acesso ao OpenProject, nao inventar numero de card nem atualizar os cards. Registrar a vinculacao pendente na PR quando necessario.

### Mensagens e tempo dedicado

O historico recente usa resumo em portugues, explicacao do motivo e `refs #numero`
quando o card esta identificado. Nao foi encontrado um formato de horas nos commits
examinados. Para os proximos commits, manter essa estrutura e acrescentar no corpo:

```text
Tempo dedicado: 0h30min.
```

O valor acima e apenas um exemplo. Registrar horas e minutos efetivamente dedicados
a alteracao, medidos na sessao ou informados explicitamente pelo integrante. Nao confundir estimativa de planejamento
com horas realizadas, nem repetir o mesmo tempo em commits diferentes. Esse registro
na mensagem nao comprova lancamento de horas no OpenProject.
