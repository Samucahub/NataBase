# Strategy Pattern

## Índice

1. [Introdução](#introdução)
2. [O Problema Original](#o-problema-original)
3. [A Solução: Strategy Pattern](#a-solução-strategy-pattern)
4. [Comparação Lado a Lado](#comparação-lado-a-lado)
5. [Benefícios da Implementação](#benefícios-da-implementação)
6. [Quando Usar Este Pattern](#quando-usar-este-pattern)

---

## Introdução

Este documento explica a aplicação do **Strategy Pattern** no contexto da **seleção e gestão de diretórios de exportação** dentro da `ConfigActivity`.

O objetivo foi **eliminar condicionais repetitivas** e **tornar a escolha do diretório mais flexível**, isolando a lógica de cada tipo de diretório numa “estratégia” própria.

---

## O Problema Original

### Código Antes (condicionais repetitivas)

Na versão atual, o método `setExportDirectory()` faz várias verificações de diretório e mostra mensagens com base em constantes do `SettingsManager`.

```kotlin
private fun setExportDirectory(directory: String) {
    SettingsManager.setExportDirectory(this, directory)

    val directoryFile = SettingsManager.getExportDirectoryFile(this)
    if (!directoryFile.exists()) {
        directoryFile.mkdirs()
    }

    updateDirectoryDisplay()

    val message = when (directory) {
        SettingsManager.KEY_DEFAULT_EXPORT_DIR -> "Diretório alterado para o diretório da aplicação"
        SettingsManager.KEY_DOCUMENTS_DIR -> {
            if (SettingsManager.isDirectoryAvailable(this)) {
                "Diretório alterado para Documentos públicos"
            } else {
                "Diretório de Documentos pode não estar disponível. A usar diretório alternativo."
            }
        }
        SettingsManager.KEY_DOWNLOADS_DIR -> {
            if (SettingsManager.isDirectoryAvailable(this)) {
                "Diretório alterado para Downloads públicos"
            } else {
                "Diretório de Downloads pode não estar disponível. A usar diretório alternativo."
            }
        }
        else -> "Diretório alterado"
    }

    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
```

### Problemas

1. **Condicionais repetidas (`when` e `if`)** — cada diretório tem comportamento próprio.
2. **Baixa escalabilidade** — adicionar novo tipo de diretório (ex: “Cloud”) exige alterar este método.
3. **Violação do Open/Closed Principle (OCP)** — o método deve ser fechado para modificação, aberto para extensão.
4. **Dificuldade em testar** — a lógica está misturada com UI (`Toast`, `SettingsManager`, etc).

---

## A Solução: Strategy Pattern

### O Que É Strategy?

O **Strategy Pattern** permite definir **vários comportamentos intercambiáveis**, encapsulando cada um numa classe separada.

No nosso caso, cada tipo de diretório (`AppDir`, `DocumentsDir`, `DownloadsDir`) implementa a sua própria lógica de **configuração e mensagem de feedback**.

---

### Implementação

#### 1. Interface `ExportDirectoryStrategy`

Define o contrato comum para todas as estratégias de diretório.

```kotlin
interface ExportDirectoryStrategy {
    fun configurar(context: Context)
    fun getMensagem(context: Context): String
}
```

#### 2. Estratégias concretas

Cada tipo de diretório implementa a interface com a sua lógica específica.

```kotlin
class AppDirectoryStrategy : ExportDirectoryStrategy {
    override fun configurar(context: Context) {
        SettingsManager.setExportDirectory(context, SettingsManager.KEY_DEFAULT_EXPORT_DIR)
    }

    override fun getMensagem(context: Context): String {
        return "Diretório alterado para o diretório da aplicação"
    }
}

class DocumentsDirectoryStrategy : ExportDirectoryStrategy {
    override fun configurar(context: Context) {
        SettingsManager.setExportDirectory(context, SettingsManager.KEY_DOCUMENTS_DIR)
    }

    override fun getMensagem(context: Context): String {
        return if (SettingsManager.isDirectoryAvailable(context)) {
            "Diretório alterado para Documentos públicos"
        } else {
            "Diretório de Documentos pode não estar disponível. A usar diretório alternativo."
        }
    }
}

class DownloadsDirectoryStrategy : ExportDirectoryStrategy {
    override fun configurar(context: Context) {
        SettingsManager.setExportDirectory(context, SettingsManager.KEY_DOWNLOADS_DIR)
    }

    override fun getMensagem(context: Context): String {
        return if (SettingsManager.isDirectoryAvailable(context)) {
            "Diretório alterado para Downloads públicos"
        } else {
            "Diretório de Downloads pode não estar disponível. A usar diretório alternativo."
        }
    }
}
```

#### 3. Contexto (`DirectoryManager`)

O contexto usa a estratégia atual para aplicar o comportamento desejado.

```kotlin
class DirectoryManager(private var strategy: ExportDirectoryStrategy) {

    fun definirStrategy(novaStrategy: ExportDirectoryStrategy) {
        strategy = novaStrategy
    }

    fun aplicar(context: Context) {
        strategy.configurar(context)
        val message = strategy.getMensagem(context)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
```

#### 4. Utilização em `ConfigActivity`

Substituímos o código cheio de condicionais por uma chamada simples ao `DirectoryManager`.

```kotlin
private fun showDirectorySelectionDialog() {
    val directories = arrayOf(
        "Diretório da aplicação (Recomendado)",
        "Documentos públicos",
        "Downloads públicos"
    )

    AlertDialog.Builder(this)
        .setTitle("Selecionar Diretório de Exportação")
        .setItems(directories) { _, which ->
            val manager = DirectoryManager(AppDirectoryStrategy())

            when (which) {
                0 -> manager.definirStrategy(AppDirectoryStrategy())
                1 -> manager.definirStrategy(DocumentsDirectoryStrategy())
                2 -> manager.definirStrategy(DownloadsDirectoryStrategy())
            }

            manager.aplicar(this)
            updateDirectoryDisplay()
        }
        .setNegativeButton("Cancelar", null)
        .show()
}
```

---

## Comparação Lado a Lado

| **Aspecto**                  | **Código Original**                   | **Com Strategy Pattern**                      |
| ---------------------------- | ------------------------------------- | --------------------------------------------- |
| **Lógica centralizada**      | Tudo dentro de `setExportDirectory()` | Distribuída em estratégias claras             |
| **Condicionais repetitivas** | Sim (`when` e `if`)                   | Nenhuma                                       |
| **Escalabilidade**           | Baixa                                 | Alta — basta criar nova `Strategy`            |
| **Testabilidade**            | Difícil                               | Cada estratégia pode ser testada isoladamente |
| **Cumpre OCP**               | ❌ Não                                 | ✅ Sim                                         |

---

## Benefícios da Implementação

### 1. **Isolamento da lógica**

Cada tipo de diretório trata da sua própria configuração.

### 2. **Extensibilidade**

Adicionar um novo tipo de diretório (ex: “Cloud”) é tão simples quanto:

```kotlin
class CloudDirectoryStrategy : ExportDirectoryStrategy {
    override fun configurar(context: Context) {
        // Implementação específica (ex: acesso à nuvem)
    }

    override fun getMensagem(context: Context) = "Diretório alterado para Cloud"
}
```

### 3. **Melhor organização e legibilidade**

O código da `ConfigActivity` fica mais limpo, focado na interface e não nas regras.

### 4. **Cumpre princípios SOLID**

Especialmente o **Open/Closed Principle** e **Single Responsibility Principle**.

### 5. **Testabilidade**

Podemos testar cada comportamento separadamente:

```kotlin
@Test
fun `DocumentsDirectoryStrategy mostra mensagem correta quando disponível`() {
    val context = mock(Context::class.java)
    val strategy = DocumentsDirectoryStrategy()
    assertEquals("Diretório alterado para Documentos públicos", strategy.getMensagem(context))
}
```

---

## Quando Usar Este Pattern

### Usar Strategy Quando:

* Tens **vários comportamentos alternativos** (ex: diferentes tipos de exportação, autenticação, cálculo, etc).
* Queres **eliminar condicionais** (`when`, `if/else`) que se repetem.
* Precisas **mudar o comportamento em tempo de execução**.
* Desejas **testar comportamentos individualmente**.

### Evitar Strategy Quando:

* Só existe um único comportamento.
* O comportamento nunca muda.
* Criar várias classes adicionaria complexidade desnecessária.

---

## Resumo das Mudanças

```
ANTES:
ConfigActivity
  └── setExportDirectory() [if/when interno]

DEPOIS:
ExportDirectoryStrategy [interface]
  ├── AppDirectoryStrategy
  ├── DocumentsDirectoryStrategy
  ├── DownloadsDirectoryStrategy
DirectoryManager [usa a Strategy]
ConfigActivity [define a estratégia e aplica]
```

---

## Conclusão

* ✅ Código mais modular e limpo
* ✅ Sem condicionais complexas
* ✅ Fácil de estender e manter
* ✅ Cumpre princípios SOLID
* ✅ Ideal para comportamentos alternáveis

O **Strategy Pattern** permite que a `ConfigActivity` trate diferentes tipos de diretório **sem duplicação de lógica**, mantendo o código organizado e preparado para crescimento futuro.
