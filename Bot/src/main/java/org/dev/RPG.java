package org.dev;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;

import com.google.gson.JsonParser;

public class RPG extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (message.getContentRaw().equals("!rpg")) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Começando a jornada:").queue();

            String prompt = "Crie um cenário inicial de RPG em português com menos de 2000 caracteres e sem criar espaços brancos entre parágrafos, ambientado em uma cidade sombria e vitoriana. O cenário deve ser misterioso e sobrenatural, lembrando as ruas de Londres durante os tempos de Jack, o Estripador, com uma atmosfera de terror. As ruas são estreitas e sujas, e há assassinatos misteriosos. A população sussurra sobre criaturas das sombras e cultos secretos. O jogador, que pode ser um detetive ou alguém fora da lei, é atraído para investigar as mortes. O clima deve ser tenso e sombrio.";
            try {
                String aiResponse = getAiResponse(prompt);
                channel.sendMessage(aiResponse).queue();
            } catch (IOException e) {
                e.printStackTrace();
                channel.sendMessage("Ocorreu um erro ao gerar a história.").queue();
            }
        }
    }


    private String getAiResponse(String prompt) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Tempo de conexão
                .readTimeout(60, TimeUnit.SECONDS)    // Tempo de leitura
                .writeTimeout(60, TimeUnit.SECONDS)   // Tempo de escrita
                .build();

        // Corpo da requisição JSON
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "llama-3.3-70b-versatile");
        requestBody.addProperty("temperature", 0.9);
        requestBody.addProperty("max_tokens", 500);

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);

        // Construir a requisição HTTP
        Request request = new Request.Builder()
                .url(Keys.API_URL)
                .header("Authorization", "Bearer " + Keys.LLAMA_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        // Enviando a requisição e recebendo a resposta
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                // Usando Gson para processar a resposta JSON
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    return jsonResponse
                            .getAsJsonArray("choices")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content")
                            .getAsString();
            } else {
                String errorResponse = response.body().string();
                System.out.println("Erro na comunicação com o modelo: " + errorResponse);
                return "Erro na comunicação com o modelo: " + response.code() + " - " + response.message();
            }
        }
    }
}
