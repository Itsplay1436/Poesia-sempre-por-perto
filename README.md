# PAM — Poesia, sempre por perto

Aplicativo Android desenvolvido em Kotlin para explorar autores, descobrir poemas e compartilhar poesias. O aplicativo utiliza a API pública PoetryDB para consultar o acervo e apresentar os conteúdos em uma interface com cores suaves e tipografia literária.

## Funcionalidades

- Listagem de autores com pesquisa por nome.
- Perfil do autor com informações biográficas, temas recorrentes e poemas disponíveis.
- Consulta de todos os títulos do acervo.
- Pesquisa de poemas por título, autor ou trecho.
- Filtro por quantidade mínima e máxima de versos.
- Combinação de filtros para refinar os resultados.
- Leitura completa dos poemas, preservando versos e estrofes.
- Geração de 1 a 100 poemas aleatórios.
- Compartilhamento de poemas por aplicativos instalados no dispositivo.
- Paginação das listas de poemas.
- Indicadores de carregamento e opção de tentar novamente em caso de erro.

A interface é apresentada em português. Os títulos, nomes e textos dos poemas são preservados conforme retornados pela API.

## Tecnologias

- Kotlin.
- Android SDK.
- Interface nativa com Android Views.
- Gradle com Kotlin DSL.
- HttpURLConnection para requisições HTTPS.
- JSON para leitura das respostas da API.
- JUnit para testes unitários.
- GitHub Actions para testes, análise e compilação automática.

## Como funciona

### Início

Apresenta uma pesquisa por título, atalhos para as principais áreas do aplicativo e um poema aleatório em destaque.

### Autores

Exibe os autores disponíveis na PoetryDB. A pesquisa filtra a lista pelo nome, sem diferenciar letras maiúsculas e minúsculas.

Ao selecionar um autor, o aplicativo consulta suas informações e os poemas disponíveis no acervo.

### Poemas

Permite consultar os títulos e pesquisar por:

- Título ou parte do título.
- Nome ou parte do nome do autor.
- Trecho de um poema.
- Quantidade de versos.

Quando vários filtros são preenchidos, os resultados precisam atender a todos eles. Os limites mínimo e máximo de versos também podem ser utilizados separadamente.

A consulta geral de títulos não retorna autor e quantidade de versos. Essas informações são exibidas nas pesquisas filtradas e na leitura do poema.

### Leitura

Apresenta o título, o autor, a quantidade de versos e o texto completo. O conteúdo pode ser selecionado e compartilhado pelo menu de compartilhamento do Android.

### Aleatórios

Permite escolher quantos poemas serão sorteados. A quantidade inicial é três, com limites de um a cem poemas.

Ao abrir um poema e voltar, a lista sorteada é preservada.

## API

O aplicativo consome a [PoetryDB](https://poetrydb.org), uma API pública que não exige chave de acesso.

Principais endpoints utilizados:

| Recurso | Endpoint |
| --- | --- |
| Lista de autores | `/author` |
| Informações de um autor | `/author/{nome}:abs/info` |
| Poemas de um autor | `/author/{nome}:abs/title,author,linecount` |
| Lista de títulos | `/title` |
| Pesquisa por título | `/title/{termo}/title,author,linecount` |
| Pesquisa por autor | `/author/{termo}/title,author,linecount` |
| Pesquisa por trecho | `/lines/{trecho}/title,author,linecount` |
| Filtro por versos | `/linecount/{min}-{max}/title,author,linecount` |
| Poema completo | `/title/{titulo}:abs` |
| Poemas aleatórios | `/random/{quantidade}/title,author,linecount` |

As consultas são executadas fora da thread principal para manter a interface responsiva. Algumas respostas são armazenadas temporariamente em memória durante a sessão.

É necessária conexão com a internet para consultar novos conteúdos. O aplicativo não oferece um acervo offline permanente.

Documentação: [PoetryDB no GitHub](https://github.com/thundercomb/poetrydb).

## Identidade visual

O aplicativo utiliza as fontes **Playfair Display** e **Lora**, incluídas no projeto.

| Cor | Código | Aplicação |
| --- | --- | --- |
| Creme | `#FFF8EA` | Fundo das telas |
| Areia | `#F7EBD3` | Campos e botões |
| Papel | `#D9B882` | Elementos em destaque |
| Linha | `#E6D3B3` | Bordas e divisores |
| Marrom pena | `#7B4A26` | Ações e identidade visual |
| Marrom tinta | `#4A2A10` | Texto principal |
| Marrom suave | `#8A6A4C` | Texto secundário |

As licenças das fontes estão disponíveis na pasta `docs`.

## Requisitos

- Android Studio compatível com Android Gradle Plugin 8.7.3.
- JDK 17.
- Android SDK 35.
- Dispositivo ou emulador com Android 8.0, API 26, ou superior.
- Conexão com a internet.

## Executando o projeto

1. Clone o repositório:

   ```bash
   git clone https://github.com/Itsplay1436/Poesia-sempre-por-perto.git
   ```

2. Abra o Android Studio e selecione **Open**.

3. Escolha a pasta clonada, que contém o arquivo `settings.gradle.kts`.

4. Aguarde a sincronização do Gradle e instale os componentes do SDK solicitados.

5. Inicie um emulador ou conecte um dispositivo Android com depuração USB ativada.

6. Execute a configuração **app**.

O arquivo `local.properties`, que registra o caminho do SDK no computador, é configurado localmente e não faz parte do repositório.

## Organização do código

| Arquivo ou pasta | Responsabilidade |
| --- | --- |
| `app/src/main/java/br/com/pam/MainActivity.kt` | Telas, navegação e interação com o usuário |
| `app/src/main/java/br/com/pam/PoetryApi.kt` | Comunicação com a API, leitura do JSON e cache de sessão |
| `app/src/main/java/br/com/pam/Filters.kt` | Modelo e validação dos filtros |
| `app/src/main/java/br/com/pam/PamIcon.kt` | Desenho dos ícones vetoriais |
| `app/src/main/res/font/` | Fontes utilizadas na interface |
| `app/src/main/res/drawable/` | Recursos gráficos |
| `app/src/main/res/values/` | Tema base |
| `app/src/main/res/values-v27/` | Ajustes do tema para Android 8.1 ou superior |
| `app/src/test/` | Testes unitários |
| `.github/workflows/android.yml` | Automação de testes, análise e compilação |
| `docs/` | Documentação, referências e licenças das fontes |

## Testes e compilação

No Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

No Linux ou macOS:

```bash
chmod +x gradlew
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Esses comandos executam os testes unitários, verificam o projeto com Android Lint e geram o APK de depuração.

O APK será criado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Os testes unitários verificam a validação dos filtros, incluindo intervalos invertidos, limites individuais e entradas inválidas.

## GitHub Actions

O repositório possui um workflow que executa automaticamente:

1. Configuração do Java, Android SDK e Gradle.
2. Testes unitários.
3. Análise com Android Lint.
4. Compilação do APK de depuração.
5. Disponibilização do artefato `PAM-debug`.

Para baixar um APK gerado, acesse a aba **Actions**, abra uma execução concluída com sucesso e procure **Artifacts → PAM-debug**.

O APK de depuração é destinado a testes e instalação direta. Ele não corresponde a uma versão publicada na Google Play.
