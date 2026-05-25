package com.example.webapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MerchController {

    @GetMapping("/merch")
    public String showMerchPage() {
        return "merch"; // Trả về file merch.html trong thư mục templates
    }
}