package com.kimngan.ComesticAdmin.controller.customer;

import com.kimngan.ComesticAdmin.services.ChatBotProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {

    @Autowired
    private ChatBotProductService chatBotProductService;

    @GetMapping("/ask")
    @ResponseBody
    public String ask(@RequestParam("prompt") String prompt) {
        return chatBotProductService.askProductBot(prompt);
    }

    @PostMapping("/ask")
    public String askBot(@RequestBody String prompt) {
        return chatBotProductService.askProductBot(prompt);
    }
}
