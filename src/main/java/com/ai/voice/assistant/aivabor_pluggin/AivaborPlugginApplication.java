package com.ai.voice.assistant.aivabor_pluggin;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@OpenAPIDefinition(
        info = @Info(
                title = "AI Voice Assistant Pluggiun",
                version = "1.0",
                description = "API for managing products in the e-commerce platform by a voice assistant"
        )
)
public class AivaborPlugginApplication {

    public static void main(String[] args) {
        SpringApplication.run(AivaborPlugginApplication.class, args);
    }

}
