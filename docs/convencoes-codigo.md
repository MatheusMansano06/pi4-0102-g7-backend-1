# Convencoes de codigo do backend

Base: o material de POO da disciplina (aulas POO_01 a POO_05) e o ADR 0001.
O Spring obriga alguns recursos que o material nao ensina. Eles estao listados no fim,
e todo uso vem com um comentario em portugues explicando o que e e por que precisamos.

## Estilo, em qualquer classe

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
- Nao escrevemos `else`. Use guarda: testa o caso errado, lanca ou retorna, segue o caminho certo.
- Nao usamos: Lombok, `record`, `var`, `Optional`, `stream`, lambda, ternario, `switch`, `while`, `enum`, `try/catch`, `throws` na assinatura.

## O que entra em cada camada

- **Entity**: `@Entity`, construtor vazio (o JPA exige) e construtor completo que chama `this.setX(...)`,
  para o objeto ja nascer validado. Id so com getter. Lista fixa de valores vira `public static final String`.
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

## O que o Spring obriga e o material nao ensina

Cada item abaixo pode ser usado, sempre com um comentario curto explicando:

`package` e `import` de bibliotecas, anotacoes alem de `@Override`, `interface` com generics
(`JpaRepository<Material, Long>`), `List`, `LocalDateTime`, `Classe.class` e `@RestControllerAdvice`.

Precisou de algo fora do material e fora desta lista? Acrescente aqui antes de usar.
