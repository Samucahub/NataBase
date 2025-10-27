# Como ativar obfuscação:

> O Android Studio já vem com uma tool de obfuscação chamada R8, mas há uma extensão para o R8. O dProtect, que era a nossa initial aproach. O dProtect faz tudo o que o R8 faz mas tem uma obfuscação mais agressiva. Só que é **SUPER PESADO** e meter isto num tablet com 2GB (mínimo que esperamos trabahar) torna-se inviável. É preferível usar o R8 e ter uma compatibilidade alta, assim como a segurança do que usar o dProtect e precisar de um tablet xpto. É como a história do SHA256 e do SHA512.

## Passo 1: Ativar a Minificação no Ficheiro de Build

A ofuscação é ativada através da "minificação" no ficheiro de build da pasta `app`.

1.  **No ficheiro** `app/build.gradle.kts`

2.  **Encontrar o bloco `buildTypes`** e, dentro desse bloco encontrar o bloco `release`.

3.  **Configure o `release` da seguinte forma:**
    ```kotlin
    buildTypes {
        release {
            // Esta linha ativa a ofuscação e minificação com R8
            isMinifyEnabled = true

            // Estes são os ficheiros de regras que o R8 utiliza
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    ```

### O que estas linhas fazem?

-   `isMinifyEnabled = true`: Este é o interruptor principal. Ativa o R8 para remover código não utilizado (minificação) e para renomear as classes, métodos e campos (ofuscação).
-   `proguardFiles(...)`: Indica ao R8 quais regras seguir.
    -   `proguard-android-optimize.txt`: Ficheiro padrão do Android com regras comuns para garantir que a app não crasha(problema que tivemos antigamente).
    -   `proguard-rules.pro`: Ficheiro pessoal de regras (`-keep` rules) para classes ou métodos que não devem ser ofuscados (por exemplo, classes usadas por bibliotecas de serialização como GSON ou classes chamadas a partir de código nativo).

## Passo 2: Gerar o APK Ofuscado

A ofuscação só é aplicada quando geramos uma versão de `release` da app.

1.  Menu do Android Studio.
2.  **Build > Generate Signed Bundle / APK...**.
3.  Seguir os passos para assinar a app (obrigatório para a app não ser "flaggada" como vírus e crashar no startup)

Alternativamente, podemos usar a tarefa do Gradle:
- No terminal do Android Studio, correr `./gradlew assembleRelease`. Esta opção o GPT disse que era mais fácil mas a de cima deu-me menos trabalho da primeira vez.

## Passo 3: Encontrar o APK Gerado

O APK ofuscado:

`app/build/outputs/apk/release/`

O ficheiro chama-se, normalmente, `app-release.apk` se não defirnimos nada no código.
