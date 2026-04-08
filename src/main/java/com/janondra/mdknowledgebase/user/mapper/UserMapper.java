package com.janondra.mdknowledgebase.user.mapper;

import com.janondra.mdknowledgebase.user.controller.dto.CreateUserDTO;
import com.janondra.mdknowledgebase.user.controller.dto.ModifyUserDTO;
import com.janondra.mdknowledgebase.user.controller.dto.UserResponseDTO;
import com.janondra.mdknowledgebase.user.model.CreateUser;
import com.janondra.mdknowledgebase.user.model.ModifyUser;
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

    public static CreateUser toCreateUser(String authId, CreateUserDTO createUserDTO) {
        return new CreateUser(
            authId,
            createUserDTO.email().toLowerCase(Locale.ROOT),
            createUserDTO.timeZone()
        );
    }

    public static ModifyUser toModifyUser(String authId, ModifyUserDTO modifyUserDTO) {
        return new ModifyUser(
            authId,
            modifyUserDTO.timeZone(),
            modifyUserDTO.dailyMailEnabled(),
            modifyUserDTO.dailyMailTime(),
            modifyUserDTO.dailyMailTags().stream().map(s -> s.toLowerCase(Locale.ROOT)).toList()
        );
    }

}
