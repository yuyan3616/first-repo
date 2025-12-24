package com.myai.spring_ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatModelService {
    private final ChatModel chatModel;
    public String generateText(String prompt) {
        return chatModel.call(prompt);
    }
}
