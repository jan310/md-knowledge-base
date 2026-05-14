package com.janondra.mdknowledgebase.user.mapper;

import com.janondra.mdknowledgebase.user.controller.dto.UpdateUserDTO;
import com.janondra.mdknowledgebase.user.controller.dto.UserResponseDTO;
import com.janondra.mdknowledgebase.user.model.UpdateUser;
import com.janondra.mdknowledgebase.user.model.User;

import java.util.Locale;

public class UserMapper {

    public static UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
            user.email(),
            user.timeZone(),
            user.dailyMailEnabled(),
            user.dailyMailTime(),
            user.dailyMailTags()
        );
    }

    public static UpdateUser toUpdateUser(String authId, UpdateUserDTO updateUserDTO) {
        return new UpdateUser(
            authId,
            updateUserDTO.timeZone(),
            updateUserDTO.dailyMailEnabled(),
            updateUserDTO.dailyMailTime(),
            updateUserDTO.dailyMailTags().stream().map(s -> s.toLowerCase(Locale.ROOT)).toList()
        );
    }

}
