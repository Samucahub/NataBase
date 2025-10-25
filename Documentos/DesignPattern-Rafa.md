# Factory Method
## Índice
1. [Introdução](#introdução)
2. [O Problema Original](#o-problema-original)
3. [A Solução: Factory Method](#a-solução-factory-method)
4. [Comparação Lado a Lado](#comparação-lado-a-lado)
5. [Benefícios da Implementação](#benefícios-da-implementação)
6. [Quando Usar Este Pattern](#quando-usar-este-pattern)

---

## Introdução

Este documento explica a aplicação do **Factory Method Pattern**. O objetivo foi simplificar a criação dos diálogos de confirmação, reduzimos assim a duplicação de código e melhoramos a manutenção do mesmo.

---

## O Problema Original

### Código Antes (Duplicação)

```kotlin
private fun mostrarDialogoLimpeza() {
    AlertDialog.Builder(this)
        .setTitle("Limpar Produções")
        .setMessage("Tem certeza que deseja limpar TODAS as produções? Esta ação não pode ser desfeita.")
        .setPositiveButton("Sim") { _, _ ->
            limparProducoes()
        }
        .setNegativeButton("Cancelar", null)
        .show()
}

private fun mostrarDialogoRegeneracao() {
    AlertDialog.Builder(this)
        .setTitle("Criar Novo Excel")
        .setMessage("Tem a certeza que deseja criar um NOVO arquivo Excel? Isto apagará o arquivo atual e criará um novo com a estrutura correta (sem Validade Exposição).")
        .setPositiveButton("Sim") { _, _ ->
            regenerarExcelCompleto()
        }
        .setNegativeButton("Cancelar", null)
        .show()
}
```

### Problemas

1. **Duplicação de Código**: A estrutura `AlertDialog.Builder()` é repetida em ambos os métodos
2. **Difícil Manutenção**: Se precisarmos alterar o estilo dos diálogos, temos que mudar em vários lugares
3. **Baixa Reutilização**: Não há forma fácil de reutilizar a lógica de criação de diálogos, sempre que precisarmos, temos de criar um **""calhamasso""** daqueles.
4. **Testabilidade Limitada**: Métodos privados que criam e mostram diálogos são difíceis de testar

---

## A Solução: Factory Method

### O Que É Factory Method?

O **Factory Method** é um padrão de design criacional que fornece uma interface para criar objetos, mas permite que as subclasses (ou neste caso, métodos específicos) decidam qual classe instanciar, é como se fosse chamar uma função em python.

### Implementação

```kotlin
object DialogFactory {
    fun criarDialogoLimpeza(
        context: Context,
        onConfirm: () -> Unit
    ): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle("Limpar Produções")
            .setMessage("Tem certeza que deseja limpar TODAS as produções? Esta ação não pode ser desfeita.")
            .setPositiveButton("Sim") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .create()
    }

    fun criarDialogoRegeneracao(
        context: Context,
        onConfirm: () -> Unit
    ): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle("Criar Novo Excel")
            .setMessage("Tem certeza que deseja criar um NOVO arquivo Excel? Isto apagará o arquivo atual e criará um novo com a estrutura correta (sem Validade Exposição).")
            .setPositiveButton("Sim") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .create()
    }
}
```

### Características da Implementação

- **Object Kotlin**: Usamos `object` para criar um singleton (padrão em Kotlin para factories estáticas)
- **Separação de Responsabilidades**: A factory só cria diálogos, não os mostra
- **Injeção de Dependências**: `context` e `onConfirm` são passados como parâmetros
- **Retorno de Objeto**: Retorna `AlertDialog` em vez de chamar `.show()` diretamente

---

## Comparação Lado a Lado

### ANTES vs DEPOIS

| **Aspecto** | **Código Original** | **Com Factory Method** |
|-------------|---------------------|------------------------|
| **Linhas de Código** | ~20 linhas (2 métodos) | ~25 linhas (factory) + 6 linhas (uso) |
| **Duplicação** | Alta (estrutura repetida) | Nenhuma |
| **Responsabilidade** | Métodos criam E mostram diálogos | Factory cria, métodos mostram |
| **Testabilidade** | Difícil (métodos privados) | Fácil (factory pública) |
| **Reutilização** | Impossível | Alta |
| **Manutenção** | Mudanças em N lugares | Mudanças em 1 lugar |

### Uso Simplificado

#### ANTES
```kotlin
private fun mostrarDialogoLimpeza() {
    AlertDialog.Builder(this)
        .setTitle("Limpar Produções")
        .setMessage("Tem certeza que deseja limpar TODAS as produções? Esta ação não pode ser desfeita.")
        .setPositiveButton("Sim") { _, _ ->
            limparProducoes()
        }
        .setNegativeButton("Cancelar", null)
        .show()
}
```

#### DEPOIS
```kotlin
private fun mostrarDialogoLimpeza() {
    DialogFactory.criarDialogoLimpeza(this) {
        limparProducoes()
    }.show()
}
```

**Redução**: De 9 linhas para 3 linhas! 📉

---

## Benefícios da Implementação

### 1. **Centralização da Lógica**
Toda a lógica de criação de diálogos está num único lugar (`DialogFactory`). Se precisarmos adicionar um tema customizado ou logging, alteramos apenas a factory.

### 2. **Melhor Testabilidade**
```kotlin
// Agora podemos testar facilmente:
@Test
fun `dialogo de limpeza tem titulo correto`() {
    val dialog = DialogFactory.criarDialogoLimpeza(context) {}
    assertEquals("Limpar Produções", dialog.title)
}
```

### 3. **Reutilização Entre Activities**
Se outra Activity precisar do mesmo diálogo:
```kotlin
// Em qualquer Activity:
DialogFactory.criarDialogoLimpeza(this) {
    // ação específica
}.show()
```

### 4. **Facilita Extensão**
Adicionar novos diálogos é simples:
```kotlin
object DialogFactory {
    // ... diálogos existentes ...
    
    fun criarDialogoExportacao(
        context: Context,
        onConfirm: () -> Unit
    ): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle("Exportar Dados")
            .setMessage("Deseja exportar os dados?")
            .setPositiveButton("Sim") { _, _ -> onConfirm() }
            .setNegativeButton("Não", null)
            .create()
    }
}
```

### 5. **Consistência Visual**
Todos os diálogos seguem o mesmo padrão de construção, garante uma UI consistente.

---

## Quando Usar Este Pattern

### Usar Factory Method Quando:

- Tens **múltiplos objetos semelhantes** sendo criados em lugares diferentes
- A **lógica de criação é complexa** ou repetitiva
- Precisas de **flexibilidade** para alterar o tipo de objeto criado
- Queres **testar** a criação de objetos facilmente
- Precisas de **consistência** na criação de objetos

### Não usar Factory Method Quando:

- Tens apenas **1 ou 2 instâncias** de um objeto
- A criação do objeto é **trivial** (ex: `new MyClass()`)
- Não há **variação** na forma como objetos são criados
- O overhead de uma factory adiciona **complexidade desnecessária**

---

## Resumo das Mudanças

### Arquitetura

```
ANTES:
MainActivity
  ├── mostrarDialogoLimpeza() [cria + mostra]
  └── mostrarDialogoRegeneracao() [cria + mostra]

DEPOIS:
MainActivity
  ├── DialogFactory [object]
  │     ├── criarDialogoLimpeza() [apenas cria]
  │     └── criarDialogoRegeneracao() [apenas cria]
  ├── mostrarDialogoLimpeza() [usa factory + mostra]
  └── mostrarDialogoRegeneracao() [usa factory + mostra]
```

### Métricas

| Métrica | Antes | Depois | Melhoria |
|---------|-------|--------|----------|
| Duplicação de código | Alta | Nenhuma |  100% |
| Linhas por método de exibição | 9 | 3 |  -67% |
| Pontos de manutenção | 2+ | 1 |  -50% |
| Testabilidade | Baixa | Alta |  +100% |

---

## Conclusão

A aplicação do **Factory Method Pattern** na `MainActivity` trouxe benefícios significativos com um custo mínimo:

-  **Código mais limpo e organizado**
-  **Fácil de testar e manter**
-  **Preparado para crescimento futuro**
-  **Sem over-engineering** - solução simples e direta


---
