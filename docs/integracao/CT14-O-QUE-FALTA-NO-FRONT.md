# Cadastro de aluno: o que falta no frontend

O backend já tem a operação. Falta a tela chamar.

Rota: `POST /api/auth/cadastro`. Ficha completa em
[CT14](../contratos/CT14-cadastrarAluno.md). É pública, igual ao login: não
precisa de token.

## 1. Acrescentar a função em `src/api.js`

Ao lado da `entrar`, que já existe:

```js
// Cria a conta do aluno (CT14) e ja deixa a pessoa logada: o backend devolve
// o token junto, entao nao precisa mandar ela entrar de novo.
export async function cadastrarAluno(dados) {
  const resposta = await fetch(`${ENDERECO_DA_API}/api/auth/cadastro`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      nome: dados.nome,
      ra: dados.ra,
      curso: dados.curso,
      email: dados.email,
      senha: dados.senha,
    }),
  })

  const corpo = await resposta.json().catch(() => null)

  if (!resposta.ok) {
    throw new Error(
      corpo?.mensagem ?? 'Não foi possível criar o cadastro. O servidor está no ar?'
    )
  }

  guardarSessao(corpo)
  return corpo
}
```

## 2. Ligar o `handleSubmit` em `StudentRegister.jsx`

Hoje a função está assim, e é exatamente isso que a Lara apontou:

```js
function handleSubmit(event) {
  event.preventDefault()

}
```

Os campos do formulário já têm `name` (`name`, `ra`, `course`, `email`,
`password`, `confirmPassword`), então dá para ler tudo do próprio formulário:

```js
async function handleSubmit(event) {
  event.preventDefault()
  setErro('')

  const form = new FormData(event.target)

  // A conferencia das duas senhas e da tela: o backend recebe so uma.
  if (form.get('password') !== form.get('confirmPassword')) {
    setErro('As duas senhas precisam ser iguais.')
    return
  }

  try {
    await cadastrarAluno({
      nome: form.get('name'),
      ra: form.get('ra'),
      curso: form.get('course'),
      email: form.get('email'),
      senha: form.get('password'),
    })
    navigate('/student/catalog')
  } catch (falha) {
    setErro(falha.message)
  }
}
```

Precisa dos dois imports e de um estado para a mensagem:

```js
import { useState } from 'react'
import { cadastrarAluno } from '../../../api'
```

```js
const [erro, setErro] = useState('')
```

E mostrar o erro antes do botão:

```jsx
{erro && <p className="student-register-erro">{erro}</p>}
```

## 3. O que o backend recusa, e a mensagem já vem pronta

A tela não precisa decidir nada: é só mostrar `corpo.mensagem`.

| Situação | O que o aluno vê |
| :--- | :--- |
| Campo em branco | Informe nome, RA, curso, e-mail e senha. |
| Senha com menos de 8 caracteres | A senha precisa de pelo menos 8 caracteres. |
| E-mail já usado | Já existe uma conta com esse e-mail. |
| RA já usado | Já existe uma conta com esse RA. |

A senha mínima é 8. Se a tela quiser avisar antes de mandar, o número é esse.

## 4. Por que isso também resolve o login da demonstração

Os usuários de `banco/02-dados-exemplo.sql` estão gravados com
`senha_hash = 'DEMO_SEM_LOGIN'`. Isso não é um hash BCrypt, então **nenhum
deles consegue entrar**, por mais certo que o backend esteja.

Com o cadastro ligado, dá para criar uma conta pela própria tela e entrar com
ela na hora. Serve para demonstrar o fluxo inteiro sem depender da correção da
carga de exemplos.

## 5. O que continua faltando, e não é aqui

Rodando o `banco/02-dados-exemplo.sql` num PostgreSQL limpo, com o
`banco/01-tabelas.sql` do mesmo commit, ele para na primeira linha de material:

```
ERROR:  column "categoria" of relation "materiais" does not exist
LINE 2:     material_id, nome, descricao, categoria, unidade_medida,
```

Os 3 usuários entram; **nenhum material entra**. Enquanto isso não for
corrigido, o catálogo fica vazio e não há o que pedir, mesmo com cadastro e
login funcionando. As divergências exatas estão em
[requisitos-banco](../especificacao/requisitos-banco.md) e o conserto é do
H01.2 (#1134).
