package com.kimngan.ComesticAdmin.services;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIService {

    private final OpenAiService openAiService;

    public OpenAIService(@Value("${openai.api.key}") String apiKey) {
    	
    	this.openAiService = new OpenAiService(apiKey);
    }

    public String askChatbot(String prompt) {
        ChatMessage userMessage = new ChatMessage("user", prompt);

        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(userMessage))
                .maxTokens(200)
                .temperature(0.7)
                .build();

        return openAiService.createChatCompletion(chatRequest)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent()
                .trim();
    }
}
