package org.dev;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RPG extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (message.getContentRaw().equals("!rpg")) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Começando a jornada:").queue();

            try {
                String prompt = "Você é um mestre de RPG. Comece uma campanha lendária descrevendo o cenário inicial:";
                CompletableFuture<String> aiResponse = getAiResponse(prompt);
                aiResponse.thenAccept(response -> channel.sendMessage(response).queue())
                        .exceptionally(error -> {
                            error.printStackTrace();
                            channel.sendMessage("Ocorreu um erro ao gerar a história.").queue();
                            return null;
                        });
            } catch (Exception error) {
                error.printStackTrace();
                channel.sendMessage("Ocorreu um erro ao processar o comando.").queue();
            }
        }
    }

    private CompletableFuture<String> getAiResponse(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Configuração da API do Cohere
                String apiKey = Keys.COHERE_API_KEY; // Substitua pela sua chave de API do Cohere
                String apiUrl = "https://api.cohere.ai/v1/generate";

                OkHttpClient client = new OkHttpClient();

                // Corpo da requisição em JSON
                JSONObject json = new JSONObject();
                json.put("model", "command-xlarge-nightly");
                json.put("prompt", prompt);
                json.put("max_tokens", 300);
                json.put("temperature", 0.7);
                json.put("k", 0);
                json.put("stop_sequences", new String[]{"\n"});

                RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

                // Requisição HTTP
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Envio e leitura da resposta
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        return jsonResponse.getJSONArray("generations")
                                .getJSONObject(0)
                                .getString("text")
                                .trim();
                    } else {
                        return "Erro ao conectar-se à API: " + (response.body() != null ? response.body().string() : "Sem detalhes.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Erro ao gerar a resposta da IA.";
            }
        });
    }
}
