package com.janondra.mdknowledgebase.user.service;

import com.auth0.client.mgmt.ManagementApi;
import com.auth0.client.mgmt.core.ManagementApiException;
import com.auth0.client.mgmt.types.UpdateUserRequestContent;
import com.janondra.mdknowledgebase.user.exception.EmailAlreadyInUseException;
import org.springframework.stereotype.Service;

@Service
public class AuthManagementService {

    private final ManagementApi managementApi;

    public AuthManagementService(ManagementApi managementApi) {
        this.managementApi = managementApi;
    }

    public void updateUserEmail(String authId, String email) {
        try {
            managementApi.users().update(
                authId,
                UpdateUserRequestContent.builder()
                    .name(email)
                    .email(email)
                    .verifyEmail(true)
                    .build()
            );
        } catch (ManagementApiException e) {
            if (e.statusCode() == 400) {
                throw new EmailAlreadyInUseException("Email '%s' is already in use.".formatted(email), e);
            }
            throw e;
        }
    }

    public void deleteUser(String authId) {
        managementApi.users().delete(authId);
    }

}
