package com.example.atiperarekrutacja;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class AtiperaRekrutacjaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtiperaRekrutacjaApplication.class, args);
    }

    @Bean
    RestClient githubRestClient(@Value("${github.api.url:https://api.github.com}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

}
