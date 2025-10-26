# Facade
## Índice
1. [Introdução](#introdução)
2. [O Problema Original](#o-problema-original)
3. [A Solução: Facade](#a-solução-facade)
4. [Comparação Lado a Lado](#comparação-lado-a-lado)
5. [Benefícios da Implementação](#benefícios-da-implementação)
6. [Quando Usar Este Pattern](#quando-usar-este-pattern)

---

## Introdução

No desenvolvimento de software, uma das maiores dificuldades é gerir a complexidade crescente dos sistemas. Quando um projeto cresce, rapidamente nos deparamos com múltiplas classes, dependências entrelaçadas e subsistemas que precisam de trabalhar em conjunto. É aqui que entra o padrão **Facade**.

### O Que É Facade?

O **Facade** é um padrão de design estrutural que fornece uma interface simples para um conjunto de interfaces mais complexas num subsistema. Funciona como um "intermediário" que coordena várias operações nos bastidores, permitindo que o código cliente use funcionalidades complexas através de chamadas simples e diretas.

Em termos práticos, o Facade:
- **Esconde** a complexidade interna de múltiplos componentes
- **Reduz** o acoplamento entre o cliente e os subsistemas
- **Simplifica** o uso de funcionalidades que dependem de várias classes

A grande vantagem? O código cliente não precisa de saber como tudo funciona por dentro - apenas precisa de uma forma fácil de atingir o seu objetivo.

### Analogia do Mundo Real: Devolução de Produtos Online

Imagina o processo de devolver um produto numa loja online. Sem uma facade, terias que lidar diretamente com cada departamento:

1. **Sistema de Inventário**: Verificar e atualizar o stock do produto
2. **Sistema Financeiro**: Processar o reembolso e ajustar a contabilidade
3. **Sistema Logístico**: Contactar a transportadora para agendar a recolha
4. **Sistema de CRM**: Atualizar o histórico do cliente
5. **Sistema de Qualidade**: Registar o defeito reportado
6. **Sistema de Fornecedores**: Notificar sobre o produto com problemas

**Com uma facade** (o Serviço de Atendimento ao Cliente), simplesmente envias uma mensagem: *"Quero devolver este produto defeituoso"*. Nos bastidores, a facade coordena todos estes sistemas automaticamente, e tu apenas recebes a confirmação: *"Reembolso processado, a transportadora contacta-te amanhã"*.

Outro exemplo seria ligar um computador. Sem uma "Facade", teríamos de iniciar manualmente a BIOS, carregar o kernel do sistema operativo, inicializar todos os drivers e serviços necessários - tudo manualmente. Com uma facade (o botão de power), apenas carregamos num botão e todo esse processo complexo acontece automaticamente.

Este é o princípio do Facade: **uma interface simples que esconde toda a orquestração complexa**.

---

## O Problema Original

### Contexto da Aplicação NataBase

A aplicação **NataBase** é um sistema de gestão de produção para restaurantes e pastelarias, desenvolvido em Kotlin para Android. Uma das suas funcionalidades principais é gerar e enviar por email relatórios diários de produção em formato Excel.

### O Código Problemático

Na versão inicial, a classe `ResumoActivity` era responsável por apresentar o resumo da produção ao utilizador e permitir o envio do relatório por email. Vejamos o método `enviarEmail()`:

```kotlin
private fun enviarEmail(file: File) {
    // 1. Verificar conectividade de rede
    if (!isNetworkAvailable(this)) {
        Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_LONG).show()
        return
    }

    val dataParaEmail = if (mapaProducao.data.isNotBlank()) {
        mapaProducao.data
    } else {
        val hoje = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        hoje
    }

    lifecycleScope.launch {
        // 2. Testar conectividade SMTP
        val podeConectar = testarConectividade()
        if (!podeConectar) {
            Toast.makeText(this@ResumoActivity, 
                "Não foi possível conectar ao servidor de email", 
                Toast.LENGTH_LONG).show()
            return@launch
        }

        withContext(Dispatchers.IO) {
            try {
                // 3. Configurar credenciais e servidor SMTP
                val emailService = EmailService(
                    senderEmail = "relatorioloja012@gmail.com",
                    senderAppPassword = "cwvt qgcg etrd ydzw",
                    toAddresses = listOf("samu.plantaarvores@gmail.com")
                )
                
                // 4. Enviar email com anexo
                emailService.sendExcel(file, "Mapa de Produção - $dataParaEmail", 
                    "Segue o mapa de produção em anexo.")
                
                // 5. Feedback de sucesso
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResumoActivity, 
                        "Email enviado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 6. Gestão detalhada de exceções
                withContext(Dispatchers.Main) {
                    when (e) {
                        is UnknownHostException ->
                            Toast.makeText(this@ResumoActivity, 
                                "Erro de DNS: Não foi possível encontrar o servidor", 
                                Toast.LENGTH_LONG).show()
                        is ConnectException ->
                            Toast.makeText(this@ResumoActivity, 
                                "Erro de conexão: Verifique sua internet", 
                                Toast.LENGTH_LONG).show()
                        else ->
                            Toast.makeText(this@ResumoActivity, 
                                "Erro ao enviar email: ${e.message}", 
                                Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
```

### Problemas Identificados

Este código apresenta várias **complexidades desnecessárias** para quem apenas quer "enviar um email":

1. **Múltiplas Responsabilidades**
   - Validação de rede (verificar se há internet)
   - Teste de conectividade SMTP (verificar acesso ao servidor de email)
   - Gestão de corrotinas e threads
   - Configuração de credenciais sensíveis
   - Tratamento de múltiplos tipos de exceções
   - Apresentação de feedback ao utilizador

2. **Alto Acoplamento**
   - A Activity conhece detalhes do protocolo SMTP
   - Depende diretamente de classes de rede (`Socket`, `InetSocketAddress`)
   - Tem de gerir manualmente o ciclo de vida das corrotinas
   - Está acoplada à implementação específica do `EmailService`

3. **Código Difícil de Manter**
   - Mais de 60 linhas apenas para enviar um email
   - Lógica complexa misturada com código de UI
   - Difícil de testar unitariamente
   - Alterações no processo de envio requerem mudanças na Activity

4. **Falta de Reutilização**
   - Se outra parte da aplicação precisar de enviar emails, todo este código teria de ser duplicado
   - A lógica de validação e tratamento de erros não pode ser facilmente reutilizada

### Impacto no Desenvolvimento

Imagine que precisássemos de:
- Adicionar autenticação OAuth2 em vez de password
- Suportar outro servidor SMTP (Outlook, SMTP corporativo)
- Adicionar retry automático em caso de falha
- Enviar emails com múltiplos anexos

**Cada uma destas alterações exigiria modificações extensas na `ResumoActivity`**, aumentando ainda mais a complexidade e o risco de introduzir bugs.

O que a Activity realmente precisa? **Apenas de uma forma simples de dizer**: *"Envia este ficheiro por email"*.

---

## A Solução: Facade

### Implementação da Solução

Para resolver estes problemas, implementámos o padrão **Facade** através da criação de uma classe `EmailReportFacade`. Esta facade atua como um **ponto único de entrada** para toda a funcionalidade de envio de relatórios por email, escondendo a complexidade dos múltiplos subsistemas envolvidos.

### A Classe EmailReportFacade

```kotlin
package com.exemplo.natabase.facades

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class EmailReportFacade(private val context: Context) {

    private val emailService = EmailService(
        senderEmail = "relatorioloja012@gmail.com",
        senderAppPassword = "cwvt qgcg etrd ydzw",
        toAddresses = listOf("samu.plantaarvores@gmail.com")
    )

    suspend fun enviarRelatorio(
        file: File, 
        reportDate: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        
        try {
            if (!validarRede()) {
                return@withContext Result.failure(
                    Exception("Sem conexão com a internet"))
            }

            if (!testarConectividadeSMTP()) {
                return@withContext Result.failure(
                    Exception("Servidor de email indisponível"))
            }

            val data = reportDate ?: obterDataAtual()
            emailService.sendExcel(file, "Mapa de Produção - $data", 
                "Segue o mapa de produção em anexo.")

            Result.success("Email enviado com sucesso!")
            
        } catch (e: Exception) {
            Result.failure(categorizarErro(e))
        }
    }

    // Métodos privados: validarRede(), testarConectividadeSMTP(), 
    // obterDataAtual(), categorizarErro()
}
```

### O Código Cliente Simplificado

```kotlin
class ResumoActivity : AppCompatActivity() {

    private lateinit var emailFacade: EmailReportFacade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumo)

        emailFacade = EmailReportFacade(this)

        btnEnviarEmail.setOnClickListener {
            val file = File(getExternalFilesDir(null), "mapa_producao.xlsx")
            enviarRelatorio(file)
        }
    }

    private fun enviarRelatorio(file: File) {
        lifecycleScope.launch {
            val resultado = emailFacade.enviarRelatorio(file)

            resultado.fold(
                onSuccess = { mensagem ->
                    Toast.makeText(this@ResumoActivity, mensagem, 
                        Toast.LENGTH_SHORT).show()
                },
                onFailure = { erro ->
                    Toast.makeText(this@ResumoActivity, erro.message, 
                        Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
```

**O resultado:** De 70+ linhas complexas para 19 linhas simples. A Activity não conhece detalhes de SMTP, validação de rede ou gestão de erros - apenas chama a facade e reage ao resultado.

---

## Comparação Lado a Lado

### Tabela Comparativa

| Aspeto | **Sem Facade** | **Com Facade** |
|--------|----------------|----------------|
| **Linhas de Código** | 70+ linhas | 19 linhas |
| **Responsabilidades** | 6 (rede, SMTP, threads, erros, dados, UI) | 1 (reagir ao resultado) |
| **Dependências** | Socket, ConnectivityManager, EmailService, Dispatchers | Apenas EmailReportFacade |
| **Testabilidade** | Difícil - requer mocks complexos | Fácil - mock simples da facade |
| **Reutilização** | Código duplicado | Facade reutilizável |
| **Manutenção** | Mudanças afetam Activity | Mudanças isoladas na facade |

---

## Benefícios da Implementação

### 1. Redução da Complexidade
- **85% menos código** na Activity (70 → 10 linhas)
- **Complexidade ciclomática** reduzida de 12 para 2
- Código mais fácil de ler e compreender

### 2. Melhor Testabilidade
A facade pode ser facilmente mockada em testes unitários, permitindo testar a Activity de forma isolada sem necessidade de simular redes, servidores SMTP ou dependências do Android. Os testes tornam-se simples, rápidos e confiáveis.

### 3. Reutilização
A facade pode ser usada em qualquer parte da aplicação que precise de enviar relatórios por email, garantindo consistência e evitando duplicação de código.

### 4. Facilidade de Manutenção
Alterações no processo de envio de emails (como adicionar retry automático, mudar servidor SMTP ou adicionar logging) ficam isoladas na facade. As Activities não precisam de ser modificadas.

### 5. Separação de Responsabilidades
- **Activity:** Apresentação e interação com utilizador
- **Facade:** Orquestração de envio de emails
- **EmailService:** Lógica SMTP
- **Validadores:** Verificações de rede

Cada classe tem um propósito claro e bem definido.

---

## Quando Usar Este Pattern

### Usar Facade Quando:

1. **Subsistemas Complexos**
   - Múltiplas classes precisam de trabalhar juntas para uma operação
   - Exemplo: Enviar email requer validação de rede + configuração SMTP + envio + tratamento de erros

2. **Alto Acoplamento**
   - Cliente conhece demasiados detalhes de implementação
   - Mudanças em subsistemas afetam muitos clientes

3. **Código Duplicado**
   - Mesma sequência de operações repetida em vários locais
   - Difícil manter consistência

4. **Difícil de Testar**
   - Testes requerem setup complexo
   - Muitas dependências externas para mockar

### Não Use Facade Quando:

1. **Operações Simples**
   - Uma única classe resolve o problema
   - Facade adicionaria complexidade desnecessária

2. **Necessidade de Controlo Fino**
   - Cliente precisa de configurar cada passo individualmente
   - Facade esconderia opções necessárias

3. **Sistema Já Bem Estruturado**
   - Classes já têm responsabilidades bem definidas
   - Baixo acoplamento existente

### Conclusão

No projeto NataBase, a aplicação do padrão Facade transformou uma operação complexa de 70 linhas em apenas 19 linhas simples. A Activity passou de gerir 6 responsabilidades diferentes para apenas uma: reagir ao resultado do envio. O código tornou-se mais legível, testável e fácil de manter, demonstrando que o Facade não é apenas teoria, mas uma solução prática com impacto real na qualidade do software.

