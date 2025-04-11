package com.kimngan.ComesticAdmin.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer")
public class CustomerChatViewController {

    @GetMapping("/chatbot")
    public String showChatPage() {
        return "customer/chatbot"; 
    }
}
