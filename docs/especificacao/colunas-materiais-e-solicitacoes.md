# Campos dos materiais e das solicitações

Definição do Luis para #1136/H01.4, atualizada em 04/11/2026 após o POST da
PR #14. Esta especificação informa o que H01.2 deve representar no banco;
não transfere à Lara a elaboração das regras funcionais.

**Regra confirmada pelo Luis: dimensão e diâmetro permitidos e opcionais para
todos os materiais do MVP.** Pode-se informar ambos, um ou nenhum. Quantidade
continua obrigatória; material inativo continua impedindo a criação do pedido.

## Responsabilidades e origem

| Entrega | Responsável |
| --- | --- |
| #1136/H01.4: campos, regras, contratos e correspondência tela/banco | Luis |
| #1145/H04.1: validação e persistência na API | Luis |
| H01.2: estrutura física e aplicação coordenada dos scripts | Lara |
| H04.2 e #1147: formulário e integração conforme o contrato | Interface e Luis, respectivamente |

Fontes: E3/CT03 acadêmicos, TXT dos cards #1136/#1145 e confirmação do Luis
nesta revisão. Os quatro booleanos são a solução técnica já integrada na
PR #14, não uma imposição do professor. Ficam definidos para este MVP.

## Dicionário das seis colunas

| Tabela e coluna | Atributo Java / contrato | PostgreSQL | Aceita NULL | DEFAULT | Significado |
| --- | --- | --- | --- | --- | --- |
| materiais.aceita_dimensao | Material.aceitaDimensao / CT11 | BOOLEAN | sim | TRUE | Dimensão permitida |
| materiais.exige_dimensao | Material.exigeDimensao / CT11 | BOOLEAN | sim | FALSE | Dimensão opcional |
| materiais.aceita_diametro | Material.aceitaDiametro / CT11 | BOOLEAN | sim | TRUE | Diâmetro permitido |
| materiais.exige_diametro | Material.exigeDiametro / CT11 | BOOLEAN | sim | FALSE | Diâmetro opcional |
| solicitacoes.dimensao | Solicitacao.dimensao / CT03/CT12 | VARCHAR(255) | sim | sem DEFAULT | Medida textual do pedido |
| solicitacoes.diametro | Solicitacao.diametro / CT03/CT12 | DOUBLE PRECISION | sim | sem DEFAULT | Número positivo em mm |

observacao já existe: VARCHAR(255) anulável, sem DEFAULT; não é uma sétima coluna
nova. Não criar largura/altura adicionais para este recorte. Medidas do pedido
não sobrescrevem especificacao do material.

NULL nas regras permanece fisicamente permitido para dados legados/configuração
incompleta; não significa FALSE. A configuração do MVP é TRUE/FALSE/TRUE/FALSE.
O código continua recusando NULL ou exigir algo não aceito com HTTP 503 /
CONFIGURACAO_PENDENTE. Os defaults do SQL não transformam um NULL explicitamente
enviado pelo JPA em valor preenchido; os scripts configuram as linhas existentes.

## Formato capacidade e validação

| Campo | Definição |
| --- | --- |
| dimensao | Texto opcional, capacidade de 255 caracteres, em mm. O modal compõe `300 x 200 mm`; ausente/null significa não informado. Texto apenas com espaços é inválido. A API preserva o texto, sem analisar numericamente largura/altura. |
| diametro | Número JSON positivo e finito em mm, por exemplo 4.5, sem unidade no valor. Ausente/null é permitido; texto, zero, negativo e não finito são inválidos. |
| observacao | Texto opcional de até 255 caracteres; ausente/null permitido, sem conversão em regra ou medida. |
| quantidade | Número JSON inteiro entre 1 e 2147483647; não converter fração, texto ou booleano. |
| materialId | Inteiro positivo no intervalo de Long, sem aceitar fração, texto ou booleano. |

255 é a capacidade técnica do VARCHAR atual, não resultado de pesquisa com
usuários. DOUBLE PRECISION acompanha o Double da entidade: não define escala
fixa, arredondamento ou precisão decimal exata. Não há máximo físico de medida
estabelecido nas fontes; não inventar limites de fabricação por material.

No modal, largura e altura são opcionais em conjunto: ambas vazias produzem
null/ausência de dimensao; se informadas, ambas positivas em mm compõem o texto.
Só uma preenchida deve ser corrigida antes do envio. Não converter cm/mm
implicitamente. O exemplo antigo em cm é substituído pelo formato em mm da tela.
Essas verificações da dupla são tarefa da integração do formulário: a API atual
recebe texto e não promete analisar os números contidos nele.

**Diferença de implementação:** a API ainda não antecipa a rejeição dos textos
acima de 255 caracteres; o banco impõe o tamanho. H04.1 deve acrescentar essa
validação e erro 400 DADOS_INVALIDOS antes de ser declarado completo. Esta
revisão documental/SQL não altera essa validação Java.

## Política explícita dos materiais de exemplo

| Material | aceita_dimensao | exige_dimensao | aceita_diametro | exige_diametro |
| --- | --- | --- | --- | --- |
| Placa de MDF | TRUE | FALSE | TRUE | FALSE |
| Acrilico cristal | TRUE | FALSE | TRUE | FALSE |
| Papel Parana | TRUE | FALSE | TRUE | FALSE |
| Cola branca escolar | TRUE | FALSE | TRUE | FALSE |
| Vareta de pinus | TRUE | FALSE | TRUE | FALSE |
| Espuma depreciada, inativa | TRUE | FALSE | TRUE | FALSE |

Valores iguais decorrem da decisão explícita para todos os materiais, não de
inferência pelo nome. A linha inativa permanece inativa. Não exige cadastro de
materiais, novos pedidos ou baixa automática. Exceções futuras exigem revisão.

## CT02 e CT11

CT11 lê as quatro regras da entidade para validar pedidos. Não criar colunas
ou tabelas de arrays parametrosAceitos/parametrosObrigatorios. Essas listas são
projeção alvo de CT02: parametrosAceitos = ["DIMENSAO", "DIAMETRO"] e
parametrosObrigatorios = []. Devem ser derivadas dos booleanos. Configuração
NULL/incoerente deve produzir ambos os metadados null, não uma lista vazia que
significaria proibição. Ainda falta implementar a projeção em MaterialSaida
antes de #1147; não afirmar que o catálogo já a devolve.

## CT03 e CT12

POST recebe só materialId, quantidade, dimensao, diametro e observacao.
Identidade vem do contexto interno validado; PENDENTE, criadaEm e atualizadaEm
vêm do servidor. Retorna HTTP 201 com medidas/observação da entidade salva.
Campos desconhecidos, inclusive usuarioId, perfil, status e dataLimite, são
recusados. Sem identidade validada um corpo válido recebe 401; não ALUNO, 403.

CT12 guarda/recupera medidas e observação e as preserva em mudanças de status.
Isso não obriga DTOs de fila/histórico a expor campos ausentes dos seus contratos.
previsaoRetirada é do fluxo do responsável, distinta da dataLimite ainda pendente.

Exemplo para qualquer material ativo configurado conforme o MVP:

```json
{
  "materialId": 1,
  "quantidade": 2,
  "dimensao": "300 x 200 mm",
  "diametro": 4.5,
  "observacao": "Uso em maquete"
}
```

Também é válido enviar só materialId e quantidade. Ausência das medidas não é
erro no MVP. Não criar identidade fictícia para demonstrar essa regra.

## Criação e atualização do banco

- Instalação nova: [01-tabelas.sql](../../banco/01-tabelas.sql) contém os campos
  e defaults; [02-dados-exemplo.sql](../../banco/02-dados-exemplo.sql) explicita
  a mesma política nas seis linhas de material.
- Banco existente: [script de atualização](../sql/H04.1-colunas-propostas.sql)
  adiciona campos ausentes, estabelece os quatro defaults e configura as regras
  de todos os materiais. O nome histórico foi mantido para preservar links.
- O UPDATE muda apenas os quatro indicadores, conforme a decisão uniforme.
  Não altera saldo, identidade, ativo, pedidos, medidas, datas ou relações.
- Pode repetir sem duplicar colunas/mudar o resultado final. IF NOT EXISTS não
  corrige tipo incompatível já existente: conferir o dicionário e validate.
- Lara combina a execução no destino; aplicar antes de iniciar a API atualizada.
  Trocar um arquivo de inicialização não migra volume existente. Não apagar volume.

O SQL é entregue para revisão e aplicação coordenada; não foi executado no banco
compartilhado. Keycloak, dataLimite e envio do formulário são dependências
separadas; a definição das seis colunas não permanece delegada à Lara.
