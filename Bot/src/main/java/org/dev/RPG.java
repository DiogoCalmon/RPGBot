package org.dev;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RPG extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (message.getContentRaw().equals("!rpg")) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Começando a jornada:").queue();

            String prompt = "Crie um cenário inicial de RPG em português, com menos de 2000 caracteres, ambientado em uma cidade sombria e vitoriana. O cenário deve ser misterioso e sobrenatural, lembrando as ruas de Londres durante os tempos de Jack, o Estripador, com uma atmosfera de terror. As ruas são estreitas e sujas, e há assassinatos misteriosos. A população sussurra sobre criaturas das sombras e cultos secretos. O jogador, que pode ser um detetive ou alguém fora da lei, é atraído para investigar as mortes. O clima deve ser tenso e sombrio.";
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

        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("inputs", prompt);  // Agora é apenas uma string, e não um array

        RequestBody body = RequestBody.create(requestBodyJson.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://api-inference.huggingface.co/models/distilgpt2")
                .post(body)
                .addHeader("Authorization", "Bearer " + Keys.HUGGING_FACE_API_KEY)
                .build();


        // Enviando a requisição e recebendo a resposta
        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseBody);
                    JSONObject jsonResponse = jsonArray.getJSONObject(0); // Pega o primeiro item do array
                    if (jsonResponse.has("generated_text")) {
                        return jsonResponse.getString("generated_text");
                    } else {
                        return "Chave 'generated_text' não encontrada na resposta.";
                    }
                }catch (JSONException e) {
                    return "Erro ao analisar a resposta JSON: " + e.getMessage();
                }

            } else {
                String errorResponse = response.body().string();
                System.out.println("Erro na comunicação com o modelo: " + errorResponse);
                return "Erro na comunicação com o modelo: " + response.code() + " - " + response.message();
            }
        }
    }

}
