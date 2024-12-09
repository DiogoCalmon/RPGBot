package org.dev;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.json.JSONObject;
import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

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

    private String getAiResponse(String prompt) {
        String key = Configuration.getGlobalConfiguration().get("GITHUB_TOKEN");
        String endpoint = "https://models.inference.ai.azure.com";
        String model = "gpt-4o";

        ChatCompletionsClient client = new ChatCompletionsClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();

        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(""),
                new ChatRequestUserMessage("Can you explain the basics of machine learning?")
        );

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setModel(model);

        ChatCompletions completions = client.complete(chatCompletionsOptions);

        return completions.getChoice().getMessage().getContent();
    }
}
