package com.myai.spring_ai.controller;

import com.myai.spring_ai.advisor.SimpleMessageChatMemoryAdvisor;
import com.myai.spring_ai.service.ChatModelService;
import com.myai.spring_ai.tool.TimeTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api")

public class AiModelController {

    @Autowired
    private ChatModelService chatModelService;
    private final ChatClient chatClient;
    @Autowired
    private ChatMemory chatMemory;

    //构造函数注入
    public AiModelController(ChatClient.Builder builder) {
        // 创建chatMemory对象
        MessageWindowChatMemory windowChatMemory = MessageWindowChatMemory.builder().build();
        //  创建MessageChatMemoryAdvisor
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(windowChatMemory) // 需要传入chatMemoryd对象
                .build();
        this.chatClient = builder
                .defaultAdvisors(messageChatMemoryAdvisor)
                .defaultTools(new TimeTools())
                .build();
    }

    @GetMapping("generate-text-with-zhipuai")
    public String generateTextWithZhipuai(@RequestParam String prompt) {
        String content = chatModelService.generateText(prompt);
        System.out.println(content);
        return content;
    }

    @GetMapping("SimpleMessageChatMemoryAdvisor")
    public String SimpleMessageChatMemoryAdvisor(@RequestParam String query,@RequestParam String conversationId) {

        return chatClient.prompt()
                .user(query)
                // 把会话ID作为参数传递给SimpleMessageChatMemoryAdvisor
                .advisors(advisorSpec -> advisorSpec.param("conversationId", conversationId))
                .advisors(new SimpleMessageChatMemoryAdvisor())
                .call()
                .content();
    }

    @GetMapping("zhiPuChatMemoryAdvisor")
    public String zhiPuChatMemoryAdvisor(@RequestParam String query,
                                         @RequestParam String conversationId) {

        return chatClient.prompt()
                .user(query)
                // 把会话ID作为参数传递给SimpleMessageChatMemoryAdvisor
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @GetMapping("testPromptTemplate")
    public String testPromptTemplate(
            @RequestParam String query,          // 用户输入的待翻译内容
            @RequestParam String conversationId, // 会话ID（用于对话记忆）
            @RequestParam(defaultValue = "英语") String targetLang // 目标语言（默认英语）
    ) {
        // 1. 定义提示词模板（固定规则 + 动态占位符）
        String templateStr = """
                你是一个专业的翻译助手，规则如下：
                - 把用户输入翻译成{target_lang}
                - 输出只保留翻译结果，不要额外内容
                用户输入：{user_query}
                """;

        // 2. 创建PromptTemplate对象，绑定占位符参数
        PromptTemplate promptTemplate = new PromptTemplate(templateStr);
        Map<String, Object> params = new HashMap<>();
        params.put("target_lang", targetLang); // 目标语言（如：英语/法语/日语）
        params.put("user_query", query);       // 用户输入的待翻译内容

        // 3. 生成标准化的Prompt（替换占位符后）
        String finalPromptText = promptTemplate.render(params); // 渲染模板为完整文本
        System.out.println("finalPromptText如下");
        System.out.println(finalPromptText); // 打印最终生成的Prompt文本

        // 4. 调用ChatClient，结合对话记忆+提示词模板
        return chatClient.prompt()
                .user(finalPromptText) // 替换原有的.user(query)，使用模板生成的Prompt
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId)) // 传递会话ID（记忆）
                .call()
                .content();
    }
    @GetMapping("testTimeTools")
    public String testTimeTools(@RequestParam String query,
                                         @RequestParam String conversationId) {

        return chatClient.prompt()
                .user(query)
                // 把会话ID作为参数传递给SimpleMessageChatMemoryAdvisor
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
