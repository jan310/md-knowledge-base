package com.janondra.mdknowledgebase.user.controller;

import com.janondra.mdknowledgebase.user.controller.dto.CreateUserDTO;
import com.janondra.mdknowledgebase.user.controller.dto.ModifyUserDTO;
import com.janondra.mdknowledgebase.user.controller.dto.UserResponseDTO;
import com.janondra.mdknowledgebase.user.mapper.UserMapper;
import com.janondra.mdknowledgebase.user.resolver.AuthId;
import com.janondra.mdknowledgebase.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public void createUser(
        @AuthId String authId,
        @RequestBody @Valid CreateUserDTO createUserDTO
    ) {
        userService.createUser(
            UserMapper.toCreateUser(
                authId,
                createUserDTO
            )
        );
    }

    @GetMapping
    @ResponseStatus(OK)
    public UserResponseDTO getUser(@AuthId String authId) {
        return UserMapper.toUserResponseDTO(
            userService.getUser(authId)
        );
    }

    @PutMapping
    @ResponseStatus(NO_CONTENT)
    public void modifyUser(
        @AuthId String authId,
        @RequestBody @Valid ModifyUserDTO modifyUserDTO
    ) {
        userService.modifyUser(
            UserMapper.toModifyUser(
                authId,
                modifyUserDTO
            )
        );
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public void deleteUser(@AuthId String authId) {
        userService.deleteUser(authId);
    }

}
