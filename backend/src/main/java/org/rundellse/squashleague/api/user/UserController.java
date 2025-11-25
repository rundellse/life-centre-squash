package org.rundellse.squashleague.api.user;

import jakarta.servlet.http.HttpServletRequest;
import org.rundellse.squashleague.api.user.dto.PasswordUpdateDTO;
import org.rundellse.squashleague.api.user.dto.UserDetailsDTO;
import org.rundellse.squashleague.model.user.User;
import org.rundellse.squashleague.model.user.UserRole;
import org.rundellse.squashleague.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

// TODO User deactivation and deletion, timed deletion on red-flag removal.
@RestController
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;


    @GetMapping("/user")
    public UserDetailsDTO getUserForSession(HttpServletRequest request) {
        User sessionUser = userService.getSessionUser(request);
        return UserService.createUserDetailsDTO(sessionUser, sessionUser.getPlayer());
    }

    @GetMapping("/user/roles")
    public Iterable<String> getUserForSessionRoles(HttpServletRequest request) {
        User sessionUser = userService.getSessionUser(request);
        return sessionUser.getUserRoles().stream().map(UserRole::getRole).map(Objects::toString).toList();
    }

    @GetMapping("/user/player")
    public Long getPlayerForUser(HttpServletRequest request) {
        User sessionUser = userService.getSessionUser(request);
        return sessionUser.getPlayer() != null ? sessionUser.getPlayer().getId() : -1;
    }

    @GetMapping("/user/{id}")
    public UserDetailsDTO getUser(HttpServletRequest request, @PathVariable Long userId) {
        LOG.trace("Getting User details for API request, id: {}", userId);
        return userService.getUserDetails(request, userId);
    }

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateUser(HttpServletRequest request, @RequestBody UserDetailsDTO userDetailsDTO) {
        LOG.trace("Attempting to update User (and corresponding Player). User ID: {}", userDetailsDTO.id());
        userService.saveUserDetails(request, userDetailsDTO.id(), userDetailsDTO);
    }

    @PostMapping("/user/password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updatePassword(HttpServletRequest request, @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        User user = userService.getSessionUser(request);
        if (!userService.doesPasswordMatchUserPassword(passwordUpdateDTO.currentPassword(), user)) {
            LOG.debug("Incorrect current password for attempted password update. User ID: {}", user.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String newPassword = passwordUpdateDTO.newPassword();
        if (!userService.validateNewPassword(newPassword)) {
            LOG.debug("New password violates password policy for attempted password update. User ID: {}", user.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        userService.saveNewPasswordForUser(newPassword, user);
    }

}
