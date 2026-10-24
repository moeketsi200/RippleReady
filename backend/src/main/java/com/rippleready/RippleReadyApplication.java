package com.rippleready;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "RippleReady API",
                version = "1.0.0",
                description = "Community Disaster Interdependency & Mutual Aid Engine — FNB App of the Year Hackathon 2026 (Topic 6)",
                contact = @Contact(
                        name = "RippleReady Engineering Team",
                        email = "team@rippleready.africa"
                )
        )
)
public class RippleReadyApplication {

    public static void main(String[] args) {
        SpringApplication.run(RippleReadyApplication.class, args);
    }
}

