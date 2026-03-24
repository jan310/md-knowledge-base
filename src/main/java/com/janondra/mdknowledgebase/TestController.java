package com.janondra.mdknowledgebase;

import com.janondra.mdknowledgebase.document.model.CreateDocument;
import com.janondra.mdknowledgebase.document.repository.DocumentRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
public class TestController {

    @Autowired private DocumentRepository documentRepository;

    @GetMapping("/test")
    public Object test() {
//        var d = new CreateDocument(
//            UUID.fromString("019d15fb-031e-7ab9-bf95-f42afc3b81c3"),
//            "asdf",
//            List.of(),
//            "asdf",
//            List.of("A", "B", "C")
//        );
//        documentRepository.saveDocument(d);

        return null;
    }

}
