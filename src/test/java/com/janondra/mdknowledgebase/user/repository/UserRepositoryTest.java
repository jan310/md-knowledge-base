package com.janondra.mdknowledgebase.user.repository;

import com.janondra.mdknowledgebase.helper.DatabaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(UserRepository.class)
class UserRepositoryTest extends DatabaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

}