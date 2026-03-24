package com.janondra.mdknowledgebase.user.mapper;

import com.janondra.mdknowledgebase.user.controller.dto.CreateUserDTO;
import com.janondra.mdknowledgebase.user.controller.dto.ModifyUserDTO;
import com.janondra.mdknowledgebase.user.controller.dto.UserResponseDTO;
import com.janondra.mdknowledgebase.user.model.CreateUser;
import com.janondra.mdknowledgebase.user.model.ModifyUser;
import com.janondra.mdknowledgebase.user.model.User;

public class UserMapper {

    public static UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
            user.email(),
            user.timeZone(),
            user.dailyMailEnabled(),
            user.dailyMailTime()
        );
    }

    public static CreateUser toCreateUser(String authId, CreateUserDTO createUserDTO) {
        return new CreateUser(
            authId,
            createUserDTO.email(),
            createUserDTO.timeZone()
        );
    }

    public static ModifyUser toModifyUser(String authId, ModifyUserDTO modifyUserDTO) {
        return new ModifyUser(
            authId,
            modifyUserDTO.email(),
            modifyUserDTO.timeZone(),
            modifyUserDTO.dailyMailEnabled(),
            modifyUserDTO.dailyMailTime()
        );
    }

}
