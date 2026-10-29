# Revisão do projeto e correção do painel do estudante

Revisão de código realizada em 29/10/2026, considerando as alterações locais já existentes no projeto.

## Estrutura e rotas

O frontend usa Angular 19, componentes standalone carregados por rota, signals, RxJS, Tailwind/DaisyUI e Leaflet. Os modelos e a autenticação ficam em `frontend/src/app/core`; componentes e serviços HTTP ficam organizados por domínio em `features`.

| Rotas | Acesso e responsabilidade |
| --- | --- |
| `/login`, `/register` | Entrada e cadastro de estudante. |
| `/student`, `/student/tracking` | Rastreamento; a segunda rota redireciona à primeira. Exige perfil de estudante. |
| `/student/polls`, `/student/profile` | Enquetes e perfil do estudante. |
| `/driver`, `/driver/polls`, `/driver/prepare-travel`, `/driver/travel-in-progress` | Seleção de enquete, preparação e acompanhamento da viagem; exigem motorista. |
| `/admin` | Área placeholder para administrador/prefeitura. |
| `/demo` | Demonstração pública de mapa. |

O backend usa Spring Boot (4.1.1 no `pom.xml` atual), Spring Security, JPA, DTOs, MapStruct, PostgreSQL/Flyway e STOMP. Os controllers recebem requisições e a identidade autenticada; os serviços concentram regras de negócio e consultas aos repositórios. O README principal ainda cita uma versão anterior do Spring Boot.

## Fluxo da viagem e regras de negócio

No `/student`, `BusTrackingComponent` consulta `/api/travels/active/student`. O backend resolve o estudante pelo ID extraído do principal autenticado e busca viagens em andamento do município/universidade. O frontend seleciona a primeira viagem, carrega sua enquete e itinerário, consulta a última posição do ônibus por REST e acompanha atualizações por WebSocket. Sem viagem, tenta as enquetes abertas e aguarda localização.

Na criação de viagem, `TravelService` verifica o vínculo municipal do motorista, da enquete e do ônibus, além de impedir viagens simultâneas para o mesmo motorista ou ônibus. Alterações de retorno/conclusão exigem o motorista responsável; cancelamento permite o motorista responsável ou a prefeitura proprietária. Enquetes e votos possuem verificações próprias de município, universidade, prazo, duplicidade e propriedade.

## Autenticação e usuário autenticado

O fluxo efetivamente exposto está no `AuthController`: login valida senha com `PasswordEncoder`, retorna access token e usuário e grava refresh token em cookie HttpOnly. O refresh rotaciona o refresh token. O Angular mantém o access token em memória, restaura a sessão no inicializador e usa interceptor para adicionar Bearer e renovar após 401.

`roleGuard` controla a navegação por perfil. No backend, `JwtAuthenticationFilter` valida assinatura/emissor/expiração, carrega o usuário e monta o principal com as autoridades atuais. Controllers usam `@AuthenticationPrincipal` e `@PreAuthorize`; serviços complementam com verificações de propriedade e município. No STOMP, `WebSocketAuthInterceptor` autentica o CONNECT com Bearer e propaga o principal ao processamento das mensagens.

## Exceções

`GlobalHandlerException` padroniza erros em `ApiError`: validação/regras de negócio retornam 400; credenciais inválidas, 401; falta de permissão/propriedade ou acesso entre municípios, 403; recurso inexistente, 404; duplicidade, 409; erro inesperado, 500. Login/refresh também produzem respostas próprias com `message`.

## Causa e correção do mapa

O painel mantinha o Leaflet criado dentro de um ancestral oculto enquanto carregava os dados. O cálculo inicial recebia dimensões zero, e os atrasos fixos de 150/250 ms não garantiam nova medição após a resposta HTTP. Na reprodução, havia apenas um tile carregado e o popup tinha largura inline de 51 px.

A correção cria o mapa somente quando o painel pode ser exibido. Um `ResizeObserver` invalida suas dimensões e atualiza o popup quando o contêiner muda de tamanho. A recriação do mapa também reinicializa as referências aos marcadores, e o observer é desconectado ao sair da página.

Os popups do ônibus e das paradas compartilham largura adaptável e quebra de texto, com estilos globais específicos para o conteúdo criado pelo Leaflet. Nomes passam por `textContent`, evitando interpretação como HTML. Os controles inferiores do mapa foram elevados no celular para ficarem acima da navegação e do itinerário recolhido.

## Pontos adicionais encontrados

Estes pontos foram identificados por leitura do código e não foram alterados nesta correção:

1. **Autorização das localizações:** `BusLocationController.handleLocationUpdate` exige motorista, mas aceita o `busId` do payload sem conferir se pertence à viagem do motorista autenticado. A leitura REST por `busId` exige autenticação geral, sem conferir o vínculo municipal. O interceptor STOMP autentica a conexão, mas não restringe os destinos de SUBSCRIBE por município/ônibus; há um tópico global de localizações.
2. **Viagens e universidades:** a consulta do estudante tem fallback para todas as viagens ativas do município. Já a leitura da enquete valida a universidade do estudante. A primeira viagem selecionada pode, portanto, levar a uma enquete cujo itinerário ele não pode consultar.
3. **Falha de refresh concorrente:** requisições que aguardam `refreshTokenSubject` filtram valores nulos. Se a renovação falha, esse fluxo não recebe erro nem novo token e pode permanecer aguardando.
4. **Resposta de erro inesperado:** o handler genérico devolve `Exception.getMessage()` ao cliente, o que pode expor detalhes internos. Convém retornar mensagem pública fixa e manter detalhes no log.

## Validação

Compilação de produção concluída com os avisos de seletores CSS emitidos pelo build, e tipos dos testes verificados. O navegador confirmou tiles renderizados e popup legível nas larguras de 1366, 375 e 320 px, incluindo redimensionamento entre desktop e celular e abertura/recolhimento do itinerário. Os seis testes do componente passaram no Karma/Jasmine, cobrindo dados atrasados, primeira localização após estado vazio, conteúdo textual/largura do popup, redimensionamento, descarte do observer e recriação do mapa. O ambiente não possui Chrome executável configurado para o launcher padrão; a suíte foi executada pelo navegador integrado com configuração temporária do Karma, sem alterar a configuração de testes do projeto.

Esta revisão não equivale a uma auditoria completa de segurança. A validação da correção se concentrou no frontend; não houve alteração das regras de negócio ou autenticação do backend.
