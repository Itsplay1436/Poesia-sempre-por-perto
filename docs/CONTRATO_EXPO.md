# Contrato para alinhamento Android / Expo

| Tela | Comportamento |
| --- | --- |
| Início | Buscar título; três atalhos; um poema aleatório com início dos versos |
| Autores | GET /author; filtro local por substring ignorando caixa |
| Autor | GET /author/NOME:abs/info; GET /author/NOME:abs/title,author,linecount |
| Poemas | GET /title para catálogo; campos título, autor, trecho, mínimo e máximo |
| Resultados | Título, autor e versos quando fornecidos; toque abre texto completo |
| Poema | GET /title/TITULO:abs; selecionar pelo autor em títulos ambíguos; compartilhar texto |
| Aleatórios | Quantidade inicial 3; intervalo 1–100; botões − e + |
| Resultado aleatório | GET /random/N/title,author,linecount; voltar da leitura mantém lista |

Filtros: /title/TERMO/title,author,linecount; /author/TERMO/title,author,linecount; /lines/TERMO/title,author,linecount; /linecount/MIN-MAX/title,author,linecount. Limites abertos permitidos. Campos simultâneos fazem interseção pelo par (título, autor). Termos são codificados como segmentos de URL. Não inventar biografias nem incluir poemas inexistentes só porque aparecem no protótipo.

Cores e medidas constam no README. Preservar título/poema em inglês, rótulos em português. O layout é adaptado à largura real do telefone; imagens do protótipo não representam capturas do aplicativo executado. Conferir as duas versões lado a lado antes da entrega.
