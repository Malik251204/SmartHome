package com.tw.medtech.pfa.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // ChatClient.Builder is auto-configured by spring-ai-starter-model-
    // ollama, pre-wired against spring.ai.ollama.* — we just need to
    // actually build it once into a ChatClient bean to inject elsewhere
    // (AgentServiceImpl).
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
