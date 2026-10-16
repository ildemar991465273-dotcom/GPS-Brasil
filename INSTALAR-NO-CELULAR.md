# Como instalar o GPS Brasil usando somente um celular

O projeto é Android nativo (Kotlin e Jetpack Compose). Não instale o ZIP: o arquivo instalável será um APK produzido por GitHub Actions.

## 1. Gerar o APK de teste
1. No Chrome, abra https://github.com/ildemar991465273-dotcom/GPS-Brasil/actions
2. Selecione **Compilar GPS Brasil APK**.
3. Toque em **Run workflow**, selecione main e confirme.
4. Aguarde uma execução verde. Se estiver vermelha, abra os logs da etapa **Compilar APK de teste**.
5. Dentro da execução concluída, vá a **Artifacts** > **GPS-Brasil-debug-APK**.
6. Baixe e extraia o ZIP do artefato: dentro estará **app-debug.apk**. Abra o APK para instalar no Android (o celular poderá pedir autorização para esta origem).

## 2. Ativar o mapa Google
O aplicativo NÃO exibirá mapa real sem uma chave de API do Google Maps. Essa chave não deve ser publicada no GitHub.

1. Acesse https://console.cloud.google.com/ e crie ou escolha um projeto seu.
2. Configure faturamento se a Google Cloud exigir. Habilite **Maps SDK for Android**.
3. Em **APIs e serviços > Credenciais**, crie uma chave de API. Restrinja a chave por aplicativo Android e ao serviço Maps SDK for Android.
4. Para restringir ao Android, use o nome do pacote **br.com.gpsbrasil** e a impressão digital **SHA-1 do certificado que assina o APK**. Para testes via GitHub Actions, essa impressão digital deve corresponder ao certificado da compilação; builds gerados em máquinas diferentes podem usar certificados de depuração diferentes.
5. No GitHub, abra **Settings > Secrets and variables > Actions > New repository secret**. Nome: **MAPS_API_KEY**. Valor: a chave criada.
6. Execute novamente o workflow. A chave deve ser usada somente nas compilações, nunca adicionada diretamente a arquivos públicos.

> Para distribuição real, preparar um certificado de assinatura estável antes de restringir a chave por pacote+SHA-1. A distribuição via Play Store pode ter um certificado diferente do de debug.

## Situação real do produto
- Base do mapa e rota: código inicial incluído.
- Pesquisa: Geocoder Android; precisa do serviço disponível no aparelho.
- Rotas: servidor público de demonstração OSRM. **Não é adequado para uso de produção ou navegação comercial sem provedor licenciado e infraestrutura apropriada**.
- Trânsito ao vivo, alertas compartilhados, navegação por voz contínua, caminhões e offline: ainda não implementados.
- Nunca utilize esta versão como única orientação para dirigir.
- A conta Google Cloud e eventual faturamento devem ser configurados pelo proprietário. O assistente não precisa receber senha, token ou chave em mensagem.
