package co.todotech.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "TodoTech Backend is running");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Bienvenido a TodoTech API");
        response.put("version", "1.0");
        response.put("status", "active");
        return response;
    }
}