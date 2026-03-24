package com.janondra.mdknowledgebase.user.service;

import com.janondra.mdknowledgebase.user.model.CreateUser;
import com.janondra.mdknowledgebase.user.model.ModifyUser;
import com.janondra.mdknowledgebase.user.model.User;
import com.janondra.mdknowledgebase.user.repository.UserRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final Cache userIdCache;
    private final UserRepository userRepository;

    public UserService(CacheManager cacheManager, UserRepository userRepository) {
        this.userIdCache = cacheManager.getCache("userIdCache");
        this.userRepository = userRepository;
    }

    public void createUser(CreateUser createUser) {
        var userId = userRepository.createUser(createUser);
        userIdCache.put(createUser.authId(), userId);
    }

    public User getUser(String authId) {
        var user = userRepository.getUser(authId);
        userIdCache.put(authId, user.id());
        return user;
    }

    public UUID getUserIdByAuthId(String authId) {
        return userIdCache.get(authId, () -> userRepository.getUserIdByAuthId(authId));
    }

    public void modifyUser(ModifyUser modifyUser) {
        userRepository.modifyUser(modifyUser);
    }

    public void deleteUser(String authId) {
        userRepository.deleteUser(authId);
        userIdCache.evict(authId);
    }

}
