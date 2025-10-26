# Primitive Obsession

## Índice

1. [O Que É Primitive Obsession](#o-que-é-primitive-obsession)
2. [O Problema no Código](#o-problema-no-código)
3. [A Solução: Value Objects](#a-solução-value-objects)
4. [Comparação: Antes vs. Depois](#comparação-antes-vs-depois)
5. [Benefícios da Refatoração](#benefícios-da-refatoração)

---

## 1. O Que É Primitive Obsession

**Primitive Obsession** é um code smell que ocorre quando usamos tipos primitivos (String, Int, Boolean) para representar conceitos de domínio que mereceriam classes próprias. Em vez de criar pequenos objetos que encapsulem comportamento e validação, recorremos a tipos básicos que não oferecem qualquer garantia ou clareza sobre o que representam.

### Sintomas Típicos

- String para representar datas, horas, emails ou códigos postais
- Int para representar moedas ou percentagens
- Constantes numéricas para codificar informação (`USER_ADMIN = 1`)

### Por Que Acontece?

Primitive Obsession nasce da preguiça: *"É só um campo para guardar dados!"* - criar um primitivo é mais rápido que criar uma classe. Com o tempo, estes primitivos espalham-se pelo código, tornando-o frágil e difícil de manter.

### Analogia do Mundo Real

Imagina que temos uma série de caixas sem rótulos. Umas têm ovos, outras leite, outras farinha. Sem identificação, teremos de abrir cada caixa para saber o que lá está. Se uma caixa se partir, misturamos tudo. **Value Objects são como caixas etiquetadas e à prova de erro** - sabemos sempre o que contêm e garantimos que o conteúdo é válido.

**"Make the implicit explicit"** - torna explícito aquilo que está implícito. Se uma String representa uma data, cria um tipo `Data`. Se um Int representa dinheiro, cria um tipo `Dinheiro`. O código fica mais claro, mais seguro e mais fácil de manter.

---

## 2. O Problema no Código

### Contexto: Aplicação NataBase

Na aplicação NataBase, a classe `MapaProducao` representa a produção diária de uma pastelaria:

```kotlin
data class MapaProducao(
    val data: String,              // String para representar data
    val diaSemana: String,         // String para representar dia da semana
    val itens: List<ItemProducao>
)

data class ProducaoDia(
    val quantidade: Int,
    val hora: String               // String para representar hora
)
```

### Problemas Identificados

#### 1. Data como String
**Problema:** `val data: String` pode ser `"26/10/2025"`, `"2025-10-26"` ou `"abc123"` - sem validação, formato ambíguo e operações impossíveis.

```kotlin
val mapa1 = MapaProducao("26/10/2025", "Segunda", itens)
val mapa2 = MapaProducao("2025-10-26", "Monday", itens) // Formato diferente!

if (mapa1.data > mapa2.data) { ... } // Compara strings, não datas!
val proximaSemana = mapa1.data + 7 // Impossível!
```

#### 2. Dia da Semana como String
**Problema:** `val diaSemana: String` pode ser `"Segunda"`, `"segunda"`, `"SEG"`, `"Monday"` - inconsistente e dependente de idioma.

```kotlin
when (mapa.diaSemana) {
    "Segunda" -> fazAlgo()
    "segunda" -> fazOutraCoisa()  // Duplicação!
    "Monday" -> fazAlgo()         // Idioma diferente!
}
```

#### 3. Hora como String
**Problema:** `val hora: String` pode ser `"14:30"`, `"2:30 PM"` ou `"25:99"` - sem validação e ordenação alfabética incorreta.

```kotlin
val producoes = listOf(
    ProducaoDia(50, "14:30"),
    ProducaoDia(30, "9:00")
)
producoes.sortedBy { it.hora } 
// Resultado: ["14:30", "9:00"] (ordem alfabética, não cronológica!)
```

### Impacto Real

No código da aplicação, vemos parsing manual repetido em múltiplos locais:

```kotlin
// Parsing manual duplicado
val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
val dataObj = sdf.parse(mapaProducao.data)

// Validação manual frágil
if (mapaProducao.diaSemana.lowercase() == "segunda") { ... }

// Comparação incorreta de horas
if (producao.hora.replace(":", "").toInt() > 1200) { ... }
```

---

## 3. A Solução: Value Objects

### Conceito

A solução é criar **Value Objects** - pequenas classes que encapsulam um valor primitivo com validação, comportamento e semântica clara. Representam conceitos do domínio e garantem que apenas valores válidos possam existir.

### Implementação

```kotlin
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

data class MapaProducao(
    val data: LocalDate,           // Era String
    val diaSemana: DayOfWeek,      // Era String  
    val itens: List<ItemProducao>
) {
    companion object {
        fun criar(data: LocalDate, itens: List<ItemProducao>) = MapaProducao(
            data = data,
            diaSemana = data.dayOfWeek,  // Calculado automaticamente!
            itens = itens
        )
    }
    
    fun dataFormatada() = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

data class ProducaoDia(
    val quantidade: Int = 0,
    val hora: LocalTime = LocalTime.now()
) {
    fun horaFormatada() = hora.format(DateTimeFormatter.ofPattern("HH:mm"))
}
```

**LocalDate** - Data com validação automática e operações (`plusDays()`, `isBefore()`).  
**DayOfWeek** - Enum com 7 valores possíveis, independente de idioma.  
**LocalTime** - Hora com comparação cronológica correcta.

---

## 4. Comparação: Antes vs. Depois

Para demonstrar o quão extenso o código fica só por causa disto, demonstro vários cenários abaixo:

### Cenário 1: Validação de Dados

**ANTES (Primitives):**
```kotlin
val mapa = MapaProducao("32/13/2025", "Terçz", itens) // Compila!
// Runtime: crash ou comportamento estranho
```

**DEPOIS (Value Objects):**
```kotlin
val mapa = MapaProducao(
    LocalDate.of(2025, 13, 32),  // Erro de compilação! Mês inválido
    DayOfWeek.valueOf("TERÇZ"),  // Erro de compilação! Dia inválido
    itens
)
// Erros apanhados no desenvolvimento, não em produção
```

### Cenário 2: Operações com Datas

**ANTES (Primitives):**
```kotlin
val hoje = "26/10/2025"
val proximaSemana = ??? // Como adicionar 7 dias?

// Solução manual complexa
val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
val dataObj = sdf.parse(hoje)
val calendar = Calendar.getInstance()
calendar.time = dataObj
calendar.add(Calendar.DAY_OF_MONTH, 7)
val proximaSemana = sdf.format(calendar.time)
```

**DEPOIS (Value Objects):**
```kotlin
val hoje = LocalDate.now()
val proximaSemana = hoje.plusDays(7)  // Simples e seguro
```

### Cenário 3: Comparações e Ordenação

**ANTES (Primitives):**
```kotlin
val producoes = listOf("14:30", "9:00", "10:15")
producoes.sorted()  // Resultado: ["10:15", "14:30", "9:00"] 
```

**DEPOIS (Value Objects):**
```kotlin
val producoes = listOf(
    LocalTime.of(14, 30),
    LocalTime.of(9, 0),
    LocalTime.of(10, 15)
)
producoes.sorted()  // Resultado: [09:00, 10:15, 14:30] 
```

---

## 5. Benefícios da Refatoração

### 1. **Type Safety - Segurança de Tipos**

Com primitivos, qualquer String é aceite. Com Value Objects, apenas valores válidos são possíveis. O compilador torna-se o teu aliado, apanhando erros antes do runtime.

```kotlin
// Antes: tudo compila
val data = "abc"  
val dia = "Ontem"

// Depois: erros de compilação
val data = LocalDate.of(2025, 13, 32)  // Mês inválido
val dia = DayOfWeek.valueOf("ONTEM")   // Dia inválido
```

### 2. **Código Mais Limpo e Expressivo**

Operações complexas tornam-se simples. Comparações fazem sentido. O código lê-se como prosa:

```kotlin
// Antes: confuso
val diferencaDias = /* 15 linhas de Calendar/SimpleDateFormat */

// Depois: claro
val diferencaDias = ChronoUnit.DAYS.between(data1, data2)
val proximaSemana = data.plusWeeks(1)
val ehSegunda = diaSemana == DayOfWeek.MONDAY
```

### 3. **Eliminação de Código Duplicado**

Parsing, validação e formatação acontecem num único sítio. Não há mais `SimpleDateFormat` espalhado por 10 ficheiros diferentes:

```kotlin
// Antes: parsing em todo o lado
val sdf1 = SimpleDateFormat("dd/MM/yyyy", ...)
val sdf2 = SimpleDateFormat("dd/MM/yyyy", ...)
val sdf3 = SimpleDateFormat("dd/MM/yyyy", ...)

// Depois: parsing centralizado
val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
val data = LocalDate.parse(input, formatter)
```

### 4. **Manutenibilidade**

Se precisares mudar o formato de uma data, mudas num único sítio. Com Strings, tens de procurar por todo o código e rezar para não te esquecer de nada.

### 5. **Testes Mais Simples**

Value Objects são testáveis por natureza. Não precisas simular estados inválidos porque eles simplesmente não podem existir:

```kotlin
@Test
fun testOperacoesDatas() {
    val data = LocalDate.of(2025, 10, 26)
    assertEquals(DayOfWeek.SUNDAY, data.dayOfWeek)
    assertEquals(LocalDate.of(2025, 11, 2), data.plusWeeks(1))
}
```

---

## Conclusão

Primitive Obsession é um smell subtil mas perigoso. Começa com *"é só uma String"* e termina com código frágil, duplicação desenfreada e bugs em produção. A solução - Value Objects - não é complicada, mas exige disciplina: **trata os conceitos do teu domínio com respeito, dá-lhes tipos próprios**.

No caso da aplicação NataBase, substituir `String` por `LocalDate`, `LocalTime` e `DayOfWeek` transformou código frágil em código robusto. Validação automática, operações nativas e comparações correctas tornaram-se gratuitas. O custo? Algumas linhas extra no início. O retorno? Um código que se mantém sozinho.

**Regra prática:** Se estivermos a validar uma String repetidamente, se tivermos parsing duplicado, se comparações falham de forma estranha - temos Primitive Obsession. Criar um Value Object. O nosso eu do futuro agradece.

---
