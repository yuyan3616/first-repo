package com.myai.spring_ai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import java.util.*;

@Slf4j
public class SimpleMessageChatMemoryAdvisor implements BaseAdvisor {
    // 静态Map存储对话记忆：key=会话ID，value=该会话的历史消息列表
    private static final Map<String, List<Message>> chatMemory = new HashMap<>();


    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        Assert.notNull(chatClientRequest, "ChatClientRequest 不能为null");
        Assert.notNull(chatClientRequest.context(), "请求上下文不能为null");
        // 1. 从会话ID获取会话记忆
        String conversationId = chatClientRequest.context().get("conversationId").toString();
        List<Message> messages = chatMemory.get(conversationId);
        if (messages == null) {
            messages = new ArrayList<>();
        }
        // 把这次请求的消息添加到会话记忆中
        messages.addAll(chatClientRequest.prompt().getInstructions());
        chatMemory.put(conversationId, messages);
        // 把添加记录后的ListMessage添加到请求上下文中
        Prompt oldPrompt = chatClientRequest.prompt();
        Prompt newPrompt = oldPrompt.mutate().messages(messages).build();
        ChatClientRequest request = chatClientRequest.mutate()
                .prompt(newPrompt)
                .build();
        return request;


    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // 通过会话id 查询会话记忆
        String conversationId = chatClientResponse.context().get("conversationId").toString();
        List<Message> hisMessages = chatMemory.get(conversationId);
        if (Objects.isNull(chatClientResponse)) {
            return chatClientResponse;

        }
        // 获取ai返回的消息
        AssistantMessage assistantMessage = chatClientResponse.chatResponse().getResult().getOutput();
        // 把这次请求的消息添加到会话记忆中
        hisMessages.add(assistantMessage);
        // 把添加记录后的ListMessage添加到会话记忆中
        chatMemory.put(conversationId, hisMessages);

        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
