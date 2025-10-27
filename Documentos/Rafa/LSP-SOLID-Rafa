# Refactoring: Liskov Substitution Principle (LSP) na CalculadoraActivity

## Índice
1. [O que é o Princípio de Substituição de Liskov (LSP)?](#o-que-é-o-princípio-de-substituição-de-liskov-lsp)
2. [Problema no Código Original](#problema-no-código-original)
3. [Solução Aplicando o LSP](#solução-aplicando-o-lsp)
4. [Comparação Lado a Lado](#comparação-lado-a-lado)
5. [Benefícios da Refatoração](#benefícios-da-refatoração)
6. [Análise do Impacto](#análise-do-impacto)
7. [Conclusão](#conclusão)

---

## O que é o Princípio de Substituição de Liskov (LSP)?

> **Definição:**  
> "As subclasses devem poder substituir as suas classes base sem alterar o comportamento correto do programa."

Em outras palavras, se uma classe `B` herda de `A`, então **`B` pode ser usada em qualquer lugar onde `A` é esperada**, sem causar efeitos colaterais ou comportamentos incorretos.

### Analogia do carregador de Telemóvel

Imaginemos que temos um **carregador USB-C**:
- Ligamos um **Samsung** → Funciona
- Ligamos um **OnePlus** → Funciona  
- Ligamos um **Xiaomi** → Funciona

**Porquê?** Porque todos respeitam o **mesmo padrão USB-C** (a interface). Não interessa a marca - todos são **substituíveis**.

Se aparecer um telemóvel "USB-C" que **explode** quando ligamos o carregador → **Viola o LSP**

### Exemplo em Código

```kotlin
// A "TOMADA" - o contrato/interface
interface Animal {
    fun fazerSom(): String
}

// Os "APARELHOS" - implementações
class Cao : Animal {
    override fun fazerSom() = "Au Au"
}

class Gato : Animal {
    override fun fazerSom() = "Miau"
}

// A função que USA a interface (não sabe qual animal é)
fun emitirSom(animal: Animal) {
    println(animal.fazerSom())  // Funciona com QUALQUER Animal
}

// Substituibilidade em ação
emitirSom(Cao())   // "Au Au"
emitirSom(Gato())  // "Miau"
```

**A função `emitirSom()` não sabe se é Cão ou Gato. Não precisa de saber!** Apenas sabe que é um `Animal` e que tem `fazerSom()`. Isto é o **LSP**.

---

## Problema no Código Original

### O que é "Abstração"?

**Abstração** = Esconder os detalhes complicados e mostrar apenas o essencial.

**Exemplo do dia-a-dia:**
- Carregar num botão do comando da TV → A TV liga
- **Não precisamos de saber** como funciona a placa, os circuitos, os transístores...
- **Precisamos de saber** que existe um botão que liga a TV

**No código:**
- **SEM abstração** = Temos que saber TODOS os detalhes de como algo funciona
- **COM abstração** = Só precisamos de saber que "faz X", não interessa como

---

### O Problema Real na CalculadoraActivity

```kotlin
private fun confirmar() {
    val valor = etValor.text.toString().toIntOrNull() ?: 0

    // PROBLEMA: O valor vai direto, sem processamento
    // A Activity está "colada" à lógica
    val resultIntent = Intent().apply {
        putExtra("VALOR", valor) // <- Sempre igual, não há flexibilidade
    }

    setResult(RESULT_OK, resultIntent)
    finish()
}
```

**Imaginemos este cenário:**

1. **Operação "Entrada"** → O valor deve ser o que o utilizador escreveu (ex: 100)
2. **Operação "Saída"** → O valor NÃO pode ser negativo (se escrever -50, deve ser 0)
3. **Operação "Ajuste"** → O valor pode ser positivo ou negativo (ex: +10 ou -10)

**Como fazer isto no código atual?**

```kotlin
private fun confirmar() {
    val valor = etValor.text.toString().toIntOrNull() ?: 0

    // SOLUÇÃO ERRADA: lógica de negócio dentro da Activity
    // e cheia de condicionais
    val valorFinal = when (tipoOperacao) {
        "entrada" -> valor
        "saida" -> if (valor < 0) 0 else valor
        "ajuste" -> valor
        "transferencia" -> if (valor < 10) 10 else valor
        "iva" -> if (valor < 0) -valor else valor
        "tipFuncionario" -> if (valor > 10000) valor + tip else valor
        "destruição" -> if (valor < 100) 0 else valor - 10
        "doacao" -> if (valor > 0) valor else 0
        "bonus" -> valor + 50
        "taxa" -> (valor * 0.05).toInt()
        else -> valor
    }

    // Lógica duplicada e difícil de manter
    println("Valor final: $valorFinal")
}
```

**Problemas desta abordagem:**
- Cada novo tipo de operação = **MODIFICAR a Activity** (código cresce infinitamente)
- A Activity está a fazer **trabalho que não é dela** (só devia tratar da UI)
- **Impossível testar** a lógica sem carregar a Activity inteira (aqueles ifs todos)
- **Impossível reutilizar** esta lógica noutro lado, sempre que for preciso uma calculadora, levamos a calculadora e uma professora de matemática.
- **Não há substituibilidade** - está tudo "hardcoded"

---

## Solução Aplicando o LSP

### A Ideia: Criar uma "Tomada Universal"

Em vez de a Activity **saber como processar cada tipo**, vamos criar uma **"tomada"** (interface) onde **qualquer tipo de processador** pode ser ligado.

**Voltamos à analogia:**
- **Tomada** = Interface `ProcessadorValor`
- **Aparelhos** = Classes que implementam a interface (ProcessadorSimples, ProcessadorComValidacao, etc.)
- **Casa** = A Activity (só precisa de ter a tomada, não precisa de saber que aparelho está ligado)

---

### Criar a "Tomada" (Interface)

```kotlin
// A "TOMADA" - o contrato
interface ProcessadorValor {
    fun processar(valor: Int): Int
}
```

**O que isto significa?**
- Qualquer classe que seja um `ProcessadorValor` **TEM QUE TER** uma função `processar()` **COM UMA FUNÇÃO E SÓ**
- Esta função recebe um número e devolve um número
- **Não interessa COMO ela faz isso**. Só interessa que ela faz

---

### Criar os "Aparelhos" (Implementações)

Agora criamos diferentes tipos de processadores, cada um com a sua lógica:

```kotlin
// APARELHO 1: Processador Simples
// Usado para operações de ENTRADA
class ProcessadorSimples : ProcessadorValor {
    override fun processar(valor: Int) = valor  // Devolve o que recebeu
}

// APARELHO 2: Processador com Validação
// Usado para operações de SAÍDA (não permite negativos)
class ProcessadorComValidacao : ProcessadorValor {
    override fun processar(valor: Int) = if (valor < 0) 0 else valor
}

// APARELHO 3: Processador de Taxa
// Calcula uma taxa de 5%
class ProcessadorTaxa : ProcessadorValor {
    override fun processar(valor: Int) = (valor * 0.05).toInt()
}

// APARELHO 4: TipFuncionario
// Sempre adiciona um bônus fixo
class ProcessadorTip : ProcessadorValor {
    override fun processar(valor: Int) = valor + tip
}
```

**Características importantes:**
- Cada classe é **pequena e simples** (faz uma coisa só)
- Todas **respeitam o contrato** (têm a função `processar()`)
- São **totalmente independentes** (podemos usar uma sem as outras)
- Posso **testar cada uma sozinha** (sem a Activity)

---

### A Activity Usa a "Tomada"

Agora a Activity **não precisa de saber** qual processador está a usar. Ela só sabe que existe um processador e que pode chamar `.processar()`.

```kotlin
class CalculadoraActivity : AppCompatActivity() {
    
    // A Activity tem uma "TOMADA"
    // Não sabe que aparelho está ligado, só sabe que é um ProcessadorValor
    private lateinit var processadorValor: ProcessadorValor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... setup normal ...
        
        tipoOperacao = intent.getStringExtra("TIPO_OPERACAO") ?: ""
        
        // "LIGAR" o aparelho certo à tomada
        processadorValor = when (tipoOperacao.lowercase()) {
            "entrada" -> ProcessadorSimples()
            "saida" -> ProcessadorComValidacao()
            "ajuste" -> ProcessadorAjuste()
            else -> ProcessadorSimples()
        }
        
        initViews()
        setupClickListeners()
    }

    private fun confirmar() {
        val valorBruto = etValor.text.toString().toIntOrNull() ?: 0
        
        // USA A ABSTRAÇÃO
        // A Activity não sabe QUAL processador é, só sabe que tem .processar()
        val valorProcessado = processadorValor.processar(valorBruto)

        val resultIntent = Intent().apply {
            putExtra("PRODUTO_NOME", produtoNome)
            putExtra("TIPO_OPERACAO", tipoOperacao)
            putExtra("VALOR", valorProcessado)  // <- Valor já processado
            putExtra("PRODUCAO_INDEX", producaoIndex)
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
```

**O que mudou?**
- A Activity **não tem IFs** para decidir como processar
- A Activity **não sabe** qual processador está a usar
- A Activity **só usa** a "tomada" (interface)
- Podemos **trocar o processador** sem alterar a Activity

---

## Comparação Lado a Lado

### Tabela Comparativa

| Aspecto | ANTES (sem LSP) | DEPOIS (com LSP) |
|---------|-----------------|------------------|
| **Dependência** | Activity conhece TODOS os detalhes | Activity só conhece a interface |
| **Flexibilidade** | Cada novo tipo = modificar Activity | Cada novo tipo = criar nova classe |
| **Testabilidade** | Preciso da Activity para testar | Testo a lógica sozinha |
| **Reutilização** | Lógica presa na Activity | Lógica reutilizável |
| **Substituibilidade** | Não existe | Qualquer processador funciona |

---

## Conclusão

O **Princípio de Substituição de Liskov (LSP)** foi aplicado ao criar uma **interface comum** (`ProcessadorValor`) que permite **trocar livremente** a implementação sem alterar a Activity.
