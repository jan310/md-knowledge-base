package com.janondra.mdknowledgebase.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAiClientConfig {

    @Bean
    public Client client(@Value("${google-gemini.api-key}") String apiKey) {
        return Client.builder().apiKey(apiKey).build();
    }

}
