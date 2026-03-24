package com.janondra.mdknowledgebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MdKnowledgeBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdKnowledgeBaseApplication.class, args);
    }

}
