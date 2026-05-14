package com.janondra.mdknowledgebase.user.service;

import com.janondra.mdknowledgebase.user.model.UpdateUser;
import com.janondra.mdknowledgebase.user.model.User;
import com.janondra.mdknowledgebase.user.repository.UserRepository;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final Cache userIdCache;
    private final UserRepository userRepository;
    private final AuthManagementService authManagementService;

    public UserService(
        Cache userIdCache,
        UserRepository userRepository,
        AuthManagementService authManagementService
    ) {
        this.userIdCache = userIdCache;
        this.userRepository = userRepository;
        this.authManagementService = authManagementService;
    }

    public UUID getUserIdByAuthId(String authId) {
        return userIdCache.get(
            authId,
            () -> userRepository.getUserIdByAuthId(authId)
        );
    }

    public User syncUser(String authId, String email) {
        Optional<User> optionalUser = userRepository.getUser(authId);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            if (!user.email().equals(email)) {
                userRepository.updateUserEmail(authId, email);
                user = user.withEmail(email);
            }
        } else {
            user = userRepository.createUser(authId, email);
        }

        userIdCache.put(authId, user.id());
        return user;
    }

    public void updateUserEmail(String authId, String email) {
        authManagementService.updateUserEmail(authId, email);
        userRepository.updateUserEmail(authId, email);
    }

    public void updateUser(UpdateUser updateUser) {
        userRepository.updateUser(updateUser);
    }

    public void deleteUser(String authId) {
        authManagementService.deleteUser(authId);
        userRepository.deleteUser(authId);
        userIdCache.evict(authId);
    }

}
