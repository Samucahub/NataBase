# Refactoring: Shotgun Surgery na CalculadoraActivity

## Índice
1. [O que é Shotgun Surgery?](#o-que-é-shotgun-surgery)
2. [Por que é problemático neste código?](#por-que-é-problemático-neste-código)
3. [Comparação Lado a Lado](#comparação-lado-a-lado)
4. [Análise do Impacto](#análise-do-impacto)

---

## O que é Shotgun Surgery?

**Shotgun Surgery** é um code smell onde uma simples mudança requer alterações em múltiplos lugares do código, como se estivessemos a disparar com uma shotgun e os estilhaços (alterações) se espalhassem por todo lado.

### Exemplo
Adicionar vibração ao clicar nos botões numéricos requer:
- Alterar **cada um dos 10 setOnClickListener**
- Adicionar `vibrar()` em **10 locais diferentes**
- Repetir o mesmo código 10 vezes

**Resultado:** 1 mudança = 10+ linhas alteradas

---

## Por que é problemático neste código?

### Cenário: Adicionar Feedback Tátil

Imaginemos que queremos adicionar **vibração** quando o utilizador clica num botão numérico. É uma feature comum e esperada em calculadoras no telemóvel para feedback tátil.

#### ANTES: Shotgun Surgery em Ação
```kotlin
// Precisas alterar CADA botão individualmente
findViewById<Button>(R.id.btn0).setOnClickListener { 
    vibrar() // Adicionar aqui
    appendNumber("0") 
}
findViewById<Button>(R.id.btn1).setOnClickListener { 
    vibrar() // E aqui
    appendNumber("1") 
}
findViewById<Button>(R.id.btn2).setOnClickListener { 
    vibrar() // E aqui
    appendNumber("2") 
}
findViewById<Button>(R.id.btn3).setOnClickListener { 
    vibrar() // E aqui
    appendNumber("3") 
}
findViewById<Button>(R.id.btn4).setOnClickListener { 
    vibrar() // E aqui
    appendNumber("4") 
}
findViewById<Button>(R.id.btn5).setOnClickListener { 
    vibrar() // E aqui
    appendNumber("5") 
}
findViewById<Button>(R.id.btn6).setOnClickListener { 
    vibrar() // E aqui
    appendNumber("6") 
}
findViewById<Button>(R.id.btn7).setOnClickListener { 
    vibrar() // E aqui
    appendNumber("7") 
}
findViewById<Button>(R.id.btn8).setOnClickListener { 
    vibrar() // E aqui
    appendNumber("8") 
}
findViewById<Button>(R.id.btn9).setOnClickListener { 
    vibrar() // E aqui também
    appendNumber("9") 
}
```

**Problemas:**
- **10 linhas** precisam ser alteradas
- Alto risco de **esquecer um botão**
- Se quisermos mudar o padrão de vibração → **10 linhas novamente**
- Impossível garantir **consistência**
- Código **frágil** e difícil de manter

#### DEPOIS: Uma Única Mudança
```kotlin
numberButtons.forEach { (buttonId, digit) ->
    findViewById<Button>(buttonId).setOnClickListener {
        vibrar() // Adicionar apenas 1x
        appendNumber(digit)
    }
}
```

**Vantagens:**
- **1 linha** alterada para todos os botões
- **Consistência garantida** automaticamente
- Mudar comportamento = **alterar 1 local**
- Código **robusto** e fácil de manter

---

## Comparação Lado a Lado

### ANTES: Código Original (Shotgun Surgery)

```kotlin
private fun setupClickListeners() {
    // 10 linhas praticamente IDÊNTICAS
    findViewById<Button>(R.id.btn0).setOnClickListener { appendNumber("0") }
    findViewById<Button>(R.id.btn1).setOnClickListener { appendNumber("1") }
    findViewById<Button>(R.id.btn2).setOnClickListener { appendNumber("2") }
    findViewById<Button>(R.id.btn3).setOnClickListener { appendNumber("3") }
    findViewById<Button>(R.id.btn4).setOnClickListener { appendNumber("4") }
    findViewById<Button>(R.id.btn5).setOnClickListener { appendNumber("5") }
    findViewById<Button>(R.id.btn6).setOnClickListener { appendNumber("6") }
    findViewById<Button>(R.id.btn7).setOnClickListener { appendNumber("7") }
    findViewById<Button>(R.id.btn8).setOnClickListener { appendNumber("8") }
    findViewById<Button>(R.id.btn9).setOnClickListener { appendNumber("9") }

    findViewById<Button>(R.id.btnClear).setOnClickListener { clearInput() }
    findViewById<Button>(R.id.btnBackspace).setOnClickListener { backspace() }
    findViewById<Button>(R.id.btnCancelar).setOnClickListener { finish() }
    findViewById<Button>(R.id.btnConfirmar).setOnClickListener { confirmar() }
}
```

**Problemas:**
- **10 linhas duplicadas** com apenas 1 caractere de diferença
- Adicionar funcionalidade = alterar 10 locais
- Alterar comportamento = mexer em 10 lugares
- Alto risco de inconsistência
- Difícil de manter e estender

---

### DEPOIS: Código Refatorado

```kotlin
private fun setupClickListeners() {
    setupNumberButtons()
    setupActionButtons()
}

/**
 * Config dos botões numéricos
 * Elimina Shotgun Surgery
 */
private fun setupNumberButtons() {
    // DADOS centralizados em estrutura
    val numberButtons = mapOf(
        R.id.btn0 to "0",
        R.id.btn1 to "1",
        R.id.btn2 to "2",
        R.id.btn3 to "3",
        R.id.btn4 to "4",
        R.id.btn5 to "5",
        R.id.btn6 to "6",
        R.id.btn7 to "7",
        R.id.btn8 to "8",
        R.id.btn9 to "9"
    )

    // LÓGICA única aplicada a todos
    numberButtons.forEach { (buttonId, digit) ->
        findViewById<Button>(buttonId).setOnClickListener { 
            appendNumber(digit) 
        }
    }
}

private fun setupActionButtons() {
    findViewById<Button>(R.id.btnClear).setOnClickListener { clearInput() }
    findViewById<Button>(R.id.btnBackspace).setOnClickListener { backspace() }
    findViewById<Button>(R.id.btnCancelar).setOnClickListener { finish() }
    findViewById<Button>(R.id.btnConfirmar).setOnClickListener { confirmar() }
}
```

**Melhorias alcançadas:**
- **Dados separados da lógica** (Data-Driven Design)
- Alterar comportamento = alterar 1 linha
- Código mais legível e maintível
- Fácil de estender
- Consistência garantida

---

### Tabela

| Aspecto | ANTES | DEPOIS |
|---------|----------|-----------|
| **Linhas alteradas** | 10 linhas (cada botão) | 1 linha (no forEach) |
| **Locais de mudança** | 10 locais diferentes | 1 local único |
| **Risco de inconsistência** | Alto (fácil esquecer) | Nulo (aplicado a todos) |
| **Complexidade** | O(n) alterações | O(1) alteração |

---

### ANTES: Shotgun Surgery

```kotlin
private fun setupClickListeners() {
    findViewById<Button>(R.id.btn0).setOnClickListener { 
        vibrar() // Adicionar linha 1
        appendNumber("0") 
    }
    findViewById<Button>(R.id.btn1).setOnClickListener { 
        vibrar() // Adicionar linha 2
        appendNumber("1") 
    }
    findViewById<Button>(R.id.btn2).setOnClickListener { 
        vibrar() // Adicionar linha 3
        appendNumber("2") 
    }
    findViewById<Button>(R.id.btn3).setOnClickListener { 
        vibrar() // Adicionar linha 4
        appendNumber("3") 
    }
    findViewById<Button>(R.id.btn4).setOnClickListener { 
        vibrar() // Adicionar linha 5
        appendNumber("4") 
    }
    findViewById<Button>(R.id.btn5).setOnClickListener { 
        vibrar() // Adicionar linha 6
        appendNumber("5") 
    }
    findViewById<Button>(R.id.btn6).setOnClickListener { 
        vibrar() // Adicionar linha 7
        appendNumber("6") 
    }
    findViewById<Button>(R.id.btn7).setOnClickListener { 
        vibrar() // Adicionar linha 8
        appendNumber("7") 
    }
    findViewById<Button>(R.id.btn8).setOnClickListener { 
        vibrar() // Adicionar linha 9
        appendNumber("8") 
    }
    findViewById<Button>(R.id.btn9).setOnClickListener { 
        vibrar() // Adicionar linha 10
        appendNumber("9") 
    }
    
    // ... ainda tinhamos de adicionar os botões das restantes funções
}
```

**Resultado:** 10 linhas alteradas, 10 oportunidades de erro

---

### DEPOIS: Single Point of Change

```kotlin
private fun setupNumberButtons() {
    val numberButtons = mapOf(
        R.id.btn0 to "0",
        R.id.btn1 to "1",
        R.id.btn2 to "2",
        R.id.btn3 to "3",
        R.id.btn4 to "4",
        R.id.btn5 to "5",
        R.id.btn6 to "6",
        R.id.btn7 to "7",
        R.id.btn8 to "8",
        R.id.btn9 to "9"
    )
    
    numberButtons.forEach { (buttonId, digit) ->
        findViewById<Button>(buttonId).setOnClickListener {
            vibrar() // Adicionar apenas 1x
            appendNumber(digit)
        }
    }
}
```

**Resultado:** 1 linha alterada, aplicada automaticamente a todos os botões

---

## Conclusão

O **Shotgun Surgery** na `CalculadoraActivity` foi eliminado através de:

1. **DRY Principle**: Don't Repeat Yourself
2. **Single Point of Change**: Mudanças centralizadas
3. **Separation of Concerns**: Métodos com responsabilidade única

### Antes vs. Depois
```
ANTES: Adicionar vibração → 10 linhas alteradas → Alto risco de erro
DEPOIS: Adicionar vibração → 1 linha alterada → Consistência garantida
```

### Impacto Escalável
Imaginemos que além de vibração, queremos adicionar:
- Som ao clicar
- Animação do botão
- Analytics/tracking
- Mudança de cor temporária

**ANTES:** 10 linhas × 4 features = **40 alterações**  
**DEPOIS:** 1 linha × 4 features = **4 alterações**

Esta refatoração reduz o esforço de manutenção e elimina completamente o risco de inconsistências entre botões.
