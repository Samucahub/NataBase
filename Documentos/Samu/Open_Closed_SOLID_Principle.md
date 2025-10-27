# Open/Closed Principle (OCP)

## Índice

1. [Introdução](#introdução)
2. [O Problema Original](#o-problema-original)
3. [Aplicação do Princípio](#aplicação-do-princípio)
4. [Refatoração Proposta](#refatoração-proposta)
5. [Benefícios](#benefícios)

---

## Introdução

O **Open/Closed Principle** (Princípio Aberto/Fechado) afirma que **as classes devem estar abertas para extensão, mas fechadas para modificação**.
Ou seja, deve ser possível adicionar novas funcionalidades sem alterar o código existente.

No caso do `ExcelService.kt`, o objetivo seria permitir **novos tipos de exportação ou formatação** de mapas de produção **sem editar diretamente a classe**.

---

## O Problema Original

Atualmente, o `ExcelService` é uma **classe monolítica**, com responsabilidades múltiplas:

* Criação de ficheiros Excel
* Leitura e escrita de dados
* Validação de entrada
* Geração de estilos e estruturas de planilhas

Como consequência:

1. Qualquer nova funcionalidade (ex: novo tipo de mapa, nova cor ou estrutura de célula) obriga a **modificar o código dentro da classe**.
2. O ficheiro é longo, difícil de testar e com baixo nível de extensibilidade.
3. Viola o OCP — está **fechado à extensão**, pois cada nova variação implica editar funções existentes.

---

## Aplicação do Princípio

Para cumprir o **Open/Closed Principle**, podemos **separar as responsabilidades** e criar **interfaces extensíveis**:

```kotlin
interface ExcelGenerator {
    fun criarEstrutura(workbook: XSSFWorkbook, sheet: Sheet)
}
```

Cada tipo de mapa (ex: “Mapa Pastelaria”, “Mapa Padaria”) pode implementar esta interface com a sua própria estrutura.

```kotlin
class MapaPastelariaGenerator : ExcelGenerator {
    override fun criarEstrutura(workbook: XSSFWorkbook, sheet: Sheet) {
        // Estrutura específica para pastelaria
    }
}

class MapaPadariaGenerator : ExcelGenerator {
    override fun criarEstrutura(workbook: XSSFWorkbook, sheet: Sheet) {
        // Estrutura específica para padaria
    }
}
```

O `ExcelService` passaria a **utilizar estas implementações** sem precisar ser modificado:

```kotlin
object ExcelService {
    fun criarNovoMapa(context: Context, generator: ExcelGenerator): MapaProducao {
        val fileName = getFileName(context)
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Planilha1")

        generator.criarEstrutura(workbook, sheet)

        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { fos -> workbook.write(fos) }
        workbook.close()

        return carregarMapaProducao(context)
    }
}
```

Assim, para criar um novo tipo de mapa, basta adicionar uma nova classe `ExcelGenerator` — **sem tocar em `ExcelService`**.

---

## Refatoração Proposta (Resumo)

| **Antes**                                                    | **Depois**                                              |
| ------------------------------------------------------------ | ------------------------------------------------------- |
| `ExcelService` contém toda a lógica de estrutura             | `ExcelService` delega a criação para `ExcelGenerator`   |
| Adicionar novo tipo de mapa exige modificar código existente | Basta criar nova classe que implementa `ExcelGenerator` |
| Viola OCP                                                    | Cumpre OCP                                              |

---

## Benefícios

✅ Código modular e fácil de manter
✅ Novos tipos de mapas ou estruturas podem ser adicionados sem alterar o núcleo
✅ Facilita testes e reutilização
✅ Cumpre o **Open/Closed Principle** do SOLID
