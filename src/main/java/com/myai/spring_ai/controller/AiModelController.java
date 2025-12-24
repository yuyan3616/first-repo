package com.myai.spring_ai.controller;

import com.myai.spring_ai.service.ChatModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiModelController {
    private final ChatModelService chatModelService;

    @GetMapping("generate-text-with-zhipuai")
    public String generateTextWithZhipuai(@RequestParam String prompt) {
        String content = chatModelService.generateText(prompt);
        System.out.println(content);
        return content;
    }
}
