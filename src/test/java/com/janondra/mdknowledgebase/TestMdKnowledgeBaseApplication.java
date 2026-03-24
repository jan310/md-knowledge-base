package com.janondra.mdknowledgebase;

import org.springframework.boot.SpringApplication;

public class TestMdKnowledgeBaseApplication {

    public static void main(String[] args) {
        SpringApplication.from(MdKnowledgeBaseApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
