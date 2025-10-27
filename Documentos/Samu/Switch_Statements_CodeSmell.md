# Code Smell: Switch Statements

## Índice

1. [Introdução](#introdução)
2. [O Problema Original](#o-problema-original)
3. [Refatoração: Eliminar o Switch Statement](#refatoração-eliminar-o-switch-statement)
4. [Comparação Lado a Lado](#comparação-lado-a-lado)
5. [Benefícios da Refatoração](#benefícios-da-refatoração)
6. [Quando Aplicar Esta Refatoração](#quando-aplicar-esta-refatoração)

---

## Introdução

Este documento explica como foi identificado e corrigido o *code smell* **Switch Statements** (ou *“Excesso de Condicionais”*) no ficheiro **`ResumoActivity.kt`**.

A refatoração teve como objetivo **reduzir o acoplamento**, **melhorar a legibilidade** e **facilitar a adição de novas opções** no menu lateral da aplicação.

---

## O Problema Original

### Onde Ocorre

O *code smell* aparece dentro do método `setupDrawer()`, onde é feito o tratamento de cliques nos itens do menu de navegação (`NavigationView`):

```kotlin
navigationView.setNavigationItemSelectedListener { menuItem ->
    when (menuItem.itemId) {
        R.id.nav_producao -> {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("open_production", true)
            startActivity(intent)
            finish()
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        R.id.nav_resumo -> {
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        R.id.nav_inventario -> {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("open_inventory", true)
            startActivity(intent)
            finish()
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        R.id.nav_configuracoes -> {
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        R.id.nav_logout -> {
            signOut()
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        else -> false
    }
}
```

---

### Problemas Identificados

1. **Várias Condições (Switch/When)** — o método cresce sempre que um novo item de menu é adicionado.
2. **Baixa Coesão** — a lógica de navegação está toda dentro da `Activity`.
3. **Dificuldade de Manutenção** — adicionar ou alterar um comportamento obriga a editar este bloco.
4. **Violação do Open/Closed Principle (OCP)** — o código precisa ser modificado para se estender.
5. **Baixa Reutilização** — o comportamento não pode ser reaproveitado noutras atividades.

---

## Refatoração: Eliminar o Switch Statement

Para eliminar o *code smell*, aplicou-se uma variação do **Strategy Pattern**:
cada item do menu passa a ser tratado por um **handler** específico, que sabe executar a ação correspondente.

---

### Passo 1 — Criar a Interface Comum

```kotlin
interface MenuAction {
    fun execute(context: Context, drawerLayout: DrawerLayout)
}
```

---

### Passo 2 — Criar Estratégias Concretas

Cada item do menu tem sua própria classe que implementa `MenuAction`.

```kotlin
class ProducaoAction : MenuAction {
    override fun execute(context: Context, drawerLayout: DrawerLayout) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("open_production", true)
        context.startActivity(intent)
        if (context is AppCompatActivity) context.finish()
        drawerLayout.closeDrawer(GravityCompat.START)
    }
}

class InventarioAction : MenuAction {
    override fun execute(context: Context, drawerLayout: DrawerLayout) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("open_inventory", true)
        context.startActivity(intent)
        if (context is AppCompatActivity) context.finish()
        drawerLayout.closeDrawer(GravityCompat.START)
    }
}

class ConfiguracoesAction : MenuAction {
    override fun execute(context: Context, drawerLayout: DrawerLayout) {
        val intent = Intent(context, ConfigActivity::class.java)
        context.startActivity(intent)
        drawerLayout.closeDrawer(GravityCompat.START)
    }
}

class LogoutAction : MenuAction {
    override fun execute(context: Context, drawerLayout: DrawerLayout) {
        if (context is ResumoActivity) {
            context.signOut()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
}
```

---

### Passo 3 — Criar um Mapeamento de IDs para Estratégias

Em vez de `when (menuItem.itemId)`, usamos um `Map` que associa IDs a ações.

```kotlin
object MenuActionRegistry {
    private val actions = mapOf(
        R.id.nav_producao to ProducaoAction(),
        R.id.nav_inventario to InventarioAction(),
        R.id.nav_configuracoes to ConfiguracoesAction(),
        R.id.nav_logout to LogoutAction()
    )

    fun getActionFor(itemId: Int): MenuAction? = actions[itemId]
}
```

---

### Passo 4 — Substituir o `when` no `setupDrawer()`

```kotlin
navigationView.setNavigationItemSelectedListener { menuItem ->
    val action = MenuActionRegistry.getActionFor(menuItem.itemId)
    if (action != null) {
        action.execute(this, drawerLayout)
        true
    } else {
        drawerLayout.closeDrawer(GravityCompat.START)
        false
    }
}
```

---

## Comparação Lado a Lado

| **Aspeto**          | **Antes (Switch Statement)**                 | **Depois (Handlers Estratégicos)**         |
| ------------------- | -------------------------------------------- | ------------------------------------------ |
| **Extensibilidade** | Baixa — cada novo item exige editar o `when` | Alta — basta adicionar uma nova estratégia |
| **Coesão**          | Lógica de navegação misturada com UI         | Cada ação isolada numa classe              |
| **Testabilidade**   | Difícil de testar individualmente            | Cada ação pode ser testada em isolamento   |
| **Manutenção**      | Arriscada — editar blocos grandes            | Segura — código distribuído e modular      |
| **Cumpre OCP**      | ❌ Não                                        | ✅ Sim                                      |

---

## Benefícios da Refatoração

1. **Código mais modular** — cada ação é independente.
2. **Abertura para extensão** — novos menus podem ser adicionados sem tocar na `Activity`.
3. **Maior clareza** — a `ResumoActivity` foca-se apenas em UI, não em lógica de navegação.
4. **Facilita testes unitários** — é possível testar individualmente cada `MenuAction`.
5. **Eliminação do code smell “Switch Statements”** — o comportamento é agora polimórfico.

---

## Resumo da Nova Estrutura

```
ResumoActivity
  └── MenuActionRegistry [mapa de ações]
        ├── ProducaoAction
        ├── InventarioAction
        ├── ConfiguracoesAction
        └── LogoutAction
```

---

## Conclusão

A refatoração removeu o *code smell* **Switch Statements**, tornando o código:

* ✅ **Mais limpo e extensível**
* ✅ **Mais fácil de manter**
* ✅ **Aberto a novas funcionalidades**
* ✅ **Aderente aos princípios SOLID**

O menu lateral agora é modular, e cada comportamento é encapsulado numa classe própria, garantindo **baixa dependência** e **alta coesão** em toda a arquitetura.

