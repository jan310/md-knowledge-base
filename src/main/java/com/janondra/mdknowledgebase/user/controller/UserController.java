package com.janondra.mdknowledgebase.user.controller;

import com.janondra.mdknowledgebase.user.controller.dto.UpdateEmailDTO;
import com.janondra.mdknowledgebase.user.controller.dto.UpdateUserDTO;
import com.janondra.mdknowledgebase.user.controller.dto.UserResponseDTO;
import com.janondra.mdknowledgebase.user.mapper.UserMapper;
import com.janondra.mdknowledgebase.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me/sync")
    @ResponseStatus(OK)
    public UserResponseDTO syncUser(@AuthenticationPrincipal Jwt jwt) {
        return UserMapper.toUserResponseDTO(
            userService.syncUser(
                jwt.getSubject(),
                jwt.getClaimAsString("https://md-know-base.com/email")
            )
        );
    }

    @PatchMapping("/me/email")
    @ResponseStatus(NO_CONTENT)
    public void updateUserEmail(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody UpdateEmailDTO updateEmailDTO
    ) {
        userService.updateUserEmail(jwt.getSubject(), updateEmailDTO.email());
    }

    @PatchMapping("/me")
    @ResponseStatus(NO_CONTENT)
    public void updateUser(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody @Valid UpdateUserDTO updateUserDTO
    ) {
        userService.updateUser(
            UserMapper.toUpdateUser(
                jwt.getSubject(),
                updateUserDTO
            )
        );
    }

    @DeleteMapping("/me")
    @ResponseStatus(NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(jwt.getSubject());
    }

}
