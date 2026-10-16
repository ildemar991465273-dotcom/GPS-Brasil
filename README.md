# GPS Brasil — Android

Aplicativo Android em desenvolvimento com Kotlin + Jetpack Compose.

## Primeira etapa
- Mapa Google Maps (exige API key do Google Cloud)
- Localização atual via GPS do Android
- Pesquisa de destinos e rotas reais via serviços externos
- Linha azul de percurso e instruções iniciais

**Situação:** código inicial preparado, ainda sem compilação e testes comprovados em aparelho. Não utilizar como GPS principal para dirigir.

## Configuração
É preciso adicionar o projeto Android ao repositório, configurar `MAPS_API_KEY` como segredo do GitHub Actions, habilitar Maps SDK for Android na Google Cloud e então compilar `assembleDebug`. O provedor público de rotas usado para prototipagem não deve ser adotado em produção sem verificar os limites e termos de uso.

## Evolução planejada
Recálculo e navegação por voz confiáveis, alertas compartilhados, trânsito ao vivo com API licenciada, rotas especializadas de caminhão, autenticação Firebase, moderação, mapas offline e monetização Play Billing/AdMob. Recursos pendentes não são anunciados como prontos.
