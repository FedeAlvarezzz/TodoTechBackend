package co.todotech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication
@EnableRetry  // ← AÑADE ESTA ANOTACIÓN
public class TodoTechApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoTechApplication.class, args);
    }
}