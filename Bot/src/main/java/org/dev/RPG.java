package org.dev;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class RPG extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (message.getContentRaw().equals("!rpg")) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Começando a jornada:").queue();

            String prompt = "Você é um mestre de RPG. Comece uma campanha lendária descrevendo o cenário inicial:";
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
        // Criando a requisição para o Hugging Face
        OkHttpClient client = new OkHttpClient();

        // Montando o corpo da requisição com o texto de entrada
        JSONObject json = new JSONObject();
        json.put("inputs", prompt);

        // Construindo a requisição
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(Keys.API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + Keys.HUGGING_FACE_API_KEY)
                .build();

        // Enviando a requisição e recebendo a resposta
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody.substring(1, responseBody.length() - 1)); // Remove os colchetes []
                return jsonResponse.getString("generated_text");
            } else {
                return "Erro na comunicação com o modelo.";
            }
        }
    }
}
