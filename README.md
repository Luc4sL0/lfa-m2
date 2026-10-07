# TRABALHO M2.2 – Interpretador de números binários

O trabalho propõe desenvolver uma gramática e um interpretador utilizando o GALS, aplicando linguagens livres de contexto à construção de compiladores. A implementação em Java realiza as análises léxica, sintática e semântica de uma linguagem para cálculos com números binários inteiros.

**Instituição:** Universidade do Vale do Itajaí

**Curso:** Ciências da Computação

**Disciplina:** Linguagens Formais e Autômatos

**Professor:** Alex Rese

**Acadêmicos:**

* Allan Slomski de Araujo
* Eduardo José de Souza
* Lucas Lopes Baroni

## Conteúdo do repositório

- `ENUNCIADO - LFA M2.pdf`: objetivo e requisitos do trabalho.
- `GRAMÁTICA - LFA M2.gals`: tokens, regras gramaticais e ações semânticas, com analisador SLR configurado no GALS.
- `CÓDIGO - LFA M2/`: código Java dos analisadores, tabelas e classes auxiliares. `Main.java` controla a execução e `Semantico.java` implementa os cálculos e as variáveis.
- `CÓDIGO - LFA M2/exemplos.txt`: programa de exemplo para execução.

## Recursos da linguagem

A linguagem aceita literais formados por `0` e `1`, variáveis, parênteses e os operadores `+`, `-`, `*`, `/` e `**`. `Log` calcula o logaritmo natural com resultado truncado para inteiro, e `Show` exibe o resultado em decimal e binário. Atribuições e chamadas de `Show` terminam com `;`.

Os cálculos usam inteiros de 32 bits e divisão inteira. Erros léxicos, sintáticos e semânticos são informados com linha e coluna, incluindo variável sem valor atribuído, divisão por zero e resultado fora do intervalo permitido.

## Como executar

É necessário ter o **JDK 11 ou superior** instalado. A partir da raiz do repositório, compile e execute o exemplo:

```sh
cd "CÓDIGO - LFA M2"
javac -encoding UTF-8 -d build *.java
java -cp build Main --arquivo exemplos.txt
```

Saída esperada:

```text
Resultado: 13 (binário: 1101)
```

Também é possível passar um programa diretamente:

```sh
java -cp build Main "x = 101; Show(x + 1);"
```

Ou iniciar o modo interativo:

```sh
java -cp build Main --interativo
```

Nesse modo, digite um programa por linha; as variáveis são preservadas durante a sessão. Use `:sair` para encerrar. O GALS é necessário apenas para editar a gramática e gerar novamente os analisadores.
