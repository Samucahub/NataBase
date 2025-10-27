## Índice

1. [O Que É o ISP](#o-que-é-o-isp)
2. [O Problema no Código](#o-problema-no-código)
3. [A Solução: Segregar Interfaces](#a-solução-segregar-interfaces)
4. [Comparação: Antes vs. Depois](#comparação-antes-vs-depois)
5. [Benefícios da Aplicação](#benefícios-da-aplicação)
6. [Conclusão](#conclusão)

---

## O Que É o ISP

O **Interface Segregation Principle** (Princípio da Segregação de Interfaces) afirma que:

> *"Nenhum cliente deve ser forçado a depender de métodos que não utiliza."*

Isto significa que interfaces grandes e "gordas" devem ser divididas em interfaces menores e mais específicas. Cada cliente deve implementar apenas o que realmente precisa, sem carregar dependências desnecessárias.

### Sintomas de Violação do ISP

- Classes forçadas a implementar métodos que não usam
- Callbacks com múltiplas responsabilidades misturadas
- Implementações vazias de métodos (`override fun metodo() { /* nada */ }`)
- Clientes acoplados a funcionalidades irrelevantes

### Por Que Acontece?

A violação do ISP nasce da tentação de criar "interfaces universais" que servem para tudo. Parece prático no início - uma única interface que resolve todos os problemas. Mas rapidamente torna-se num fardo: mudanças numa responsabilidade afectam todos os clientes, mesmo os que não usam essa funcionalidade.

### Analogia do Mundo Real

Imagina um canivete suíço com 50 ferramentas. Para apertar um parafuso, só precisas da chave de fenda, mas és obrigado a carregar também o saca-rolhas, a tesoura, a lima e outras 46 ferramentas. **ISP defende que cada ferramenta seja separada** - levas apenas o que precisas.

---

## O Problema no Código

### Contexto: ItemProducaoAdapter

Na aplicação NataBase, a classe `ItemProducaoAdapter` é um RecyclerView.Adapter responsável por mostrar a lista de produtos. No entanto, o seu construtor exige **5 dependências diferentes**:

```kotlin
class ItemProducaoAdapter(
    private val lista: MutableList<ItemProducao>,
    private val producaoIndex: Int,
    private val onQuantidadeAlterada: (ItemProducao) -> Unit,      // Callback 1
    private val salvarCallback: (List<ItemProducao>) -> Unit,      // Callback 2
    private val onCalculadoraRequest: (String, String, Int) -> Unit // Callback 3
) : RecyclerView.Adapter<ItemProducaoAdapter.ViewHolder>()
```

### Problemas Identificados

#### 1. Interface Implícita "Gorda"
Embora não exista uma interface explícita, o construtor define um contrato implícito com 3 callbacks distintos. Qualquer cliente que crie este adapter é **forçado a fornecer todas as 3 funcionalidades**, mesmo que não precise delas todas.

```kotlin
// MainActivity tem de implementar TUDO, mesmo que não use tudo
val adapter = ItemProducaoAdapter(
    lista = itens,
    producaoIndex = 1,
    onQuantidadeAlterada = { item -> 
        // Talvez não precise disto...
    },
    salvarCallback = { lista -> 
        CacheManager.salvar(this, lista)
    },
    onCalculadoraRequest = { produto, tipo, idx -> 
        abrirCalculadora(produto, tipo, idx)
    }
)
```

#### 2. Responsabilidades Misturadas
O adapter mistura 3 responsabilidades diferentes:
- **Notificação de mudanças** (`onQuantidadeAlterada`)
- **Persistência** (`salvarCallback`)
- **Navegação** (`onCalculadoraRequest`)

Estas responsabilidades não têm relação entre si. Um cliente pode querer notificações sem persistência, ou persistência sem navegação.

#### 3. Acoplamento Desnecessário
Se a MainActivity apenas quer mostrar uma lista **read-only** (sem edição), ainda assim é forçada a fornecer callbacks de edição:

```kotlin
// Lista read-only no ResumoActivity - mas preciso dos callbacks!
val adapter = ItemProducaoAdapter(
    lista = itensFinais,
    producaoIndex = 1,
    onQuantidadeAlterada = { /* não uso */ },  //  Implementação vazia!
    salvarCallback = { /* não uso */ },        //  Implementação vazia!
    onCalculadoraRequest = { _, _, _ -> }      //  Implementação vazia!
)
```

#### 4. Dificuldade de Teste
Para testar o adapter, tens de fornecer mocks de todas as dependências, mesmo que o teste só se foque numa:

```kotlin
@Test
fun testMostrarLista() {
    val mockQuantidade = mockk<(ItemProducao) -> Unit>()  // Não usado no teste
    val mockSalvar = mockk<(List<ItemProducao>) -> Unit>() // Não usado no teste
    val mockCalc = mockk<(String, String, Int) -> Unit>()  // Não usado no teste
    
    val adapter = ItemProducaoAdapter(itens, 1, mockQuantidade, mockSalvar, mockCalc)
    // Só quero testar que a lista é mostrada correctamente...
}
```

---

## A Solução: Segregar Interfaces

### Conceito

A solução é dividir as responsabilidades em **interfaces pequenas e específicas**. Cada cliente implementa apenas as interfaces que realmente precisa.

### Implementação

#### Passo 1: Criar Interfaces Segregadas

```kotlin
// Interface para notificação de mudanças
interface ItemChangeListener {
    fun onItemChanged(item: ItemProducao)
}

// Interface para persistência
interface ItemPersistenceManager {
    fun saveItems(items: List<ItemProducao>)
}

// Interface para navegação/acções
interface ItemActionHandler {
    fun onCalculatorRequest(productName: String, operationType: String, index: Int)
}
```

#### Passo 2: Refatorar o Adapter

```kotlin
class ItemProducaoAdapter(
    private val lista: MutableList<ItemProducao>,
    private val producaoIndex: Int,
    private val changeListener: ItemChangeListener? = null,     // Opcional
    private val persistenceManager: ItemPersistenceManager? = null, // Opcional
    private val actionHandler: ItemActionHandler? = null        // Opcional
) : RecyclerView.Adapter<ItemProducaoAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.nome.text = item.produto

        holder.btnProducao.setOnClickListener {
            actionHandler?.onCalculatorRequest(item.produto, "Produção", producaoIndex)
        }

        holder.btnSobras.setOnClickListener {
            actionHandler?.onCalculatorRequest(item.produto, "Sobras", producaoIndex)
        }

        holder.btnPerdas.setOnClickListener {
            actionHandler?.onCalculatorRequest(item.produto, "Perdas", producaoIndex)
        }
    }

    fun atualizarItem(produtoNome: String, tipoOperacao: String, valor: Int, producaoIndex: Int) {
        val itemIndex = lista.indexOfFirst { it.produto == produtoNome }
        if (itemIndex != -1) {
            val item = lista[itemIndex]
            val updatedItem = /* ... lógica de actualização ... */

            lista[itemIndex] = updatedItem
            
            // Notificar apenas se houver listener
            changeListener?.onItemChanged(updatedItem)
            
            // Persistir apenas se houver manager
            persistenceManager?.saveItems(lista)
            
            notifyItemChanged(itemIndex)
        }
    }
}
```

#### Passo 3: Clientes Implementam Apenas o Necessário

**MainActivity - Precisa de tudo:**
```kotlin
class MainActivity : AppCompatActivity(), 
                      ItemChangeListener, 
                      ItemPersistenceManager,
                      ItemActionHandler {
    
    private lateinit var adapter: ItemProducaoAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fornece todas as interfaces
        adapter = ItemProducaoAdapter(
            lista = itens,
            producaoIndex = 1,
            changeListener = this,
            persistenceManager = this,
            actionHandler = this
        )
    }
    
    override fun onItemChanged(item: ItemProducao) {
        atualizarUI(item)
    }
    
    override fun saveItems(items: List<ItemProducao>) {
        CacheManager.salvar(this, items)
    }
    
    override fun onCalculatorRequest(productName: String, operationType: String, index: Int) {
        abrirCalculadora(productName, operationType, index)
    }
}
```

**ResumoActivity - Lista read-only:**
```kotlin
class ResumoActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Não precisa de callbacks - fornece null!
        val adapter = ItemProducaoAdapter(
            lista = itensFechados,
            producaoIndex = 1,
            changeListener = null,
            persistenceManager = null,
            actionHandler = null
        )
    }
}
```

**TestActivity - Só quer persistência:**
```kotlin
class TestActivity : AppCompatActivity(), ItemPersistenceManager {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Só implementa persistência
        val adapter = ItemProducaoAdapter(
            lista = itens,
            producaoIndex = 1,
            changeListener = null,
            persistenceManager = this,  // Só esta!
            actionHandler = null
        )
    }
    
    override fun saveItems(items: List<ItemProducao>) {
        database.guardar(items)
    }
}
```

---

## Comparação: Antes vs. Depois

### Cenário 1: Activity com Todas as Funcionalidades

**ANTES (Sem ISP):**
```kotlin
// Forçado a usar lambdas - não há interface clara
val adapter = ItemProducaoAdapter(
    lista = itens,
    producaoIndex = 1,
    onQuantidadeAlterada = { item -> atualizarUI(item) },
    salvarCallback = { lista -> CacheManager.salvar(this, lista) },
    onCalculadoraRequest = { produto, tipo, idx -> abrirCalculadora(produto, tipo, idx) }
)
```

**DEPOIS (Com ISP):**
```kotlin
// Implementa interfaces - tipo explícito e testável
class MainActivity : AppCompatActivity(), 
                      ItemChangeListener, 
                      ItemPersistenceManager,
                      ItemActionHandler {
    
    val adapter = ItemProducaoAdapter(
        lista = itens,
        producaoIndex = 1,
        changeListener = this,
        persistenceManager = this,
        actionHandler = this
    )
    
    // Métodos explícitos com nomes claros
    override fun onItemChanged(item: ItemProducao) { ... }
    override fun saveItems(items: List<ItemProducao>) { ... }
    override fun onCalculatorRequest(...) { ... }
}
```

### Cenário 2: Activity Só com Visualização

**ANTES (Sem ISP):**
```kotlin
// Obrigado a fornecer implementações vazias
val adapter = ItemProducaoAdapter(
    lista = itens,
    producaoIndex = 1,
    onQuantidadeAlterada = { /* não uso */ },  //  Poluição
    salvarCallback = { /* não uso */ },        //  Poluição
    onCalculadoraRequest = { _, _, _ -> }      //  Poluição
)
```

**DEPOIS (Com ISP):**
```kotlin
// Não fornece nada - interfaces são opcionais
val adapter = ItemProducaoAdapter(
    lista = itens,
    producaoIndex = 1
    // Sem callbacks desnecessários! 
)
```

### Cenário 3: Testes Unitários

**ANTES (Sem ISP):**
```kotlin
@Test
fun testAtualizarItem() {
    // Mocks de tudo, mesmo o que não é usado
    val mockQuantidade = mockk<(ItemProducao) -> Unit>(relaxed = true)
    val mockSalvar = mockk<(List<ItemProducao>) -> Unit>(relaxed = true)
    val mockCalc = mockk<(String, String, Int) -> Unit>(relaxed = true)
    
    val adapter = ItemProducaoAdapter(itens, 1, mockQuantidade, mockSalvar, mockCalc)
    
    adapter.atualizarItem("Croissant", "Produção", 50, 1)
    
    verify { mockQuantidade(any()) }
    verify { mockSalvar(any()) }
}
```

**DEPOIS (Com ISP):**
```kotlin
@Test
fun testAtualizarItem() {
    // Mock apenas do que é testado
    val mockListener = mockk<ItemChangeListener>(relaxed = true)
    
    val adapter = ItemProducaoAdapter(
        itens, 
        1, 
        changeListener = mockListener
        // Sem mocks desnecessários!
    )
    
    adapter.atualizarItem("Croissant", "Produção", 50, 1)
    
    verify { mockListener.onItemChanged(any()) }
}
```

---

## Benefícios da Aplicação

### 1. **Menor Acoplamento**

Clientes dependem apenas das funcionalidades que usam. Mudanças numa interface não afectam clientes que não a implementam.

### 2. **Código Mais Limpo**

Sem implementações vazias ou callbacks não utilizados. O código expressa claramente as suas dependências.

### 3. **Facilidade de Teste**

Testes focam-se apenas nas interfaces relevantes. Menos mocks, testes mais rápidos e claros.

### 4. **Flexibilidade**

Fácil adicionar novos clientes com necessidades diferentes. Novas interfaces podem ser adicionadas sem quebrar código existente.

### 5. **Manutenibilidade**

Interfaces pequenas são mais fáceis de entender e modificar. Mudanças têm impacto localizado.

---

## Conclusão

O Interface Segregation Principle é sobre **respeito pelos clientes**. Não os forces a depender do que não precisam. Interfaces grandes e monolíticas criam acoplamento desnecessário, dificultam testes e tornam o código rígido.

No caso do `ItemProducaoAdapter` da aplicação NataBase, dividir as responsabilidades em `ItemChangeListener`, `ItemPersistenceManager` e `ItemActionHandler` transformou um contrato rígido num sistema flexível. Clientes escolhem o que implementar, testes tornam-se mais simples, e o código expressa claramente as suas intenções.

**Regra prática:** Se estás a criar implementações vazias de métodos de interface, ou se mudanças numa funcionalidade afectam clientes que não a usam - estás a violar o ISP. Divide a interface. Os teus clientes agradecem.

---
