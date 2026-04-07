package com.janondra.mdknowledgebase.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAiClientConfig {

    @Value("${google-gemini.api-key}")
    private String apiKey;

    @Bean
    public Client client() {
        return Client.builder().apiKey(apiKey).build();
    }

}
