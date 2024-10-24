package com.project.unifiedauthenticationservice.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class TestController {
    @GetMapping("/home")
    public String home() {
        return "PUBLIC HELP";
    }

    @GetMapping("/readme")
    public String readme() {
        return "README";
    }

    @GetMapping("/help")
    public String help() {
        return "HELP";
    }
}
