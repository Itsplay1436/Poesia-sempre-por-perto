# Validação realizada — 24/11/2026

## Concluído neste ambiente

- Leitura dos arquivos de requisitos e da referência visual fornecida.
- Consulta da documentação oficial PoetryDB: confirmação dos endpoints `/info` e de intervalos de versos.
- Compilação de todos os arquivos Kotlin contra o SDK Android 35, utilizando o compilador Kotlin 1.9.23 disponível no Gradle 8.9 (o projeto Android Studio está configurado com Kotlin 2.0.21).
- Compilação e vinculação dos recursos com AAPT2 35.
- Cinco testes JUnit 4.13.2 de validação de filtros: todos aprovados.
- Conversão do código para DEX com D8, mínimo Android 26.
- Empacotamento, alinhamento e assinatura de um APK debug; assinatura v2/v3 verificada.
- Inspeção dos metadados do APK: pacote br.com.pam, versão 1.0, Activity exportada, permissão INTERNET, minSdk 26 e targetSdk 35.

## Limitações e pendências

- Gradle completo (AGP, lint e assembleDebug) não concluiu: os processos Java não conseguiram resolver dependências por restrição de rede/proxy do ambiente. O APK fornecido foi construído com a cadeia local alternativa descrita acima, não pelo workflow Gradle.
- As chamadas diretas a poetrydb.org receberam bloqueio HTTP 403 neste ambiente. A implementação foi conferida contra a documentação oficial; não houve validação ponta a ponta da API em aparelho.
- Não foi executado em emulador ou celular. Aparência, acessibilidade, navegação e comportamento com servidor real ainda precisam do roteiro TESTES.md. Compilar não comprova esses comportamentos.
- A equivalência visual com Expo depende da implementação do outro integrante e da comparação conjunta.
- Repositório GitHub não publicado: é necessário conectar a conta e informar o destino. O projeto inclui .gitignore e workflow para publicação.

O APK de teste está em `apk/PAM-debug.apk`. A chave temporária de teste não acompanha o pacote. Se uma compilação posterior usar outra chave e o Android recusar a atualização, desinstale a versão de teste antes de instalar a nova.
