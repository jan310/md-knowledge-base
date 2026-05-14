package com.janondra.mdknowledgebase.config;

import com.auth0.client.mgmt.ManagementApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthManagementApiConfig {

    @Bean
    public ManagementApi managementApi(
        @Value("${authorization-server.domain}") String authServerDomain,
        @Value("${authorization-server.client-id}") String authServerClientId,
        @Value("${authorization-server.client-secret}") String authServerClientSecret
    ) {
        return ManagementApi.builder()
            .domain(authServerDomain)
            .clientCredentials(authServerClientId, authServerClientSecret)
            .build();
    }

}
