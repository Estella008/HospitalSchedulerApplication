package com.example.hospitalscheduler.contoller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/resultado.html") 
    public String resultado() {
        return "resultado";
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }
}