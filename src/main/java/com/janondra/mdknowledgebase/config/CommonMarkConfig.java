package com.janondra.mdknowledgebase.config;

import org.commonmark.Extension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommonMarkConfig {

    private final List<Extension> markdownExtensions = List.of(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        FootnotesExtension.create()
    );

    @Bean
    public Parser mdParser() {
        return Parser.builder()
            .extensions(markdownExtensions)
            .build();
    }

    @Bean
    public HtmlRenderer htmlRenderer() {
        return HtmlRenderer.builder()
            .extensions(markdownExtensions)
            .build();
    }

}
