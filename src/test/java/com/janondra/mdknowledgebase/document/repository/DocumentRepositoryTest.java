package com.janondra.mdknowledgebase.document.repository;

import com.janondra.mdknowledgebase.helper.DatabaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(DocumentRepository.class)
class DocumentRepositoryTest extends DatabaseIntegrationTest {

    @Autowired
    private DocumentRepository documentRepository;

}