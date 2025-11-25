package org.rundellse.squashleague.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.rundellse.squashleague.api.user.dto.UserDetailsDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.user.Role;
import org.rundellse.squashleague.model.user.User;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public UserDetailsDTO getUserDetails(HttpServletRequest request, long userId) {
        User user = getUserForId(userId);
        if (!validateUserAgainstRequest(request, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // There is (almost) no requirement in the model for a User to have a player, but for now everyone will, users are/will be
        // created one-to-one with players, and both admins are players. If there is a legitimate User in future with no
        // corresponding Player this will need to be updated.
        Player player = user.getPlayer();

        return createUserDetailsDTO(user, player);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void saveUserDetails(HttpServletRequest request, Long userId, UserDetailsDTO userDetailsDTO) {
        User user = getUserForId(userId);
        if (!validateUserAgainstRequest(request, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Player player = user.getPlayer();

        user.setName(userDetailsDTO.name());
        user.setEmail((userDetailsDTO.email()));

        player.setName(userDetailsDTO.name());
        player.setEmail(userDetailsDTO.email());
        player.setPhoneNumber(userDetailsDTO.phoneNumber());
        player.setAvailabilityNotes(userDetailsDTO.availabilityNotes());
        player.setAnonymised(userDetailsDTO.anonymise());

        userRepository.save(user);
        playerRepository.save(player);
    }

    private User getUserForId(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            LOG.error("No User found for ID: {}", userId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return userOptional.get();
    }

    public User getSessionUser(HttpServletRequest request) {
        User sessionUser = userRepository.findUserByEmail(request.getRemoteUser());
        if (sessionUser == null) {
            LOG.error("Session User not found by email in Database. Session ID: {}", request.getSession().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return sessionUser;
    }

    private boolean validateUserAgainstRequest(HttpServletRequest request, User userToUpdate) {
        User sessionUser = getSessionUser(request);

        if (request.isUserInRole(Role.ROLE_ADMIN.name())) {
            LOG.info("User details update for User (ID: {}) performed by administrator (ID: {}), not validating session User against User to update.", userToUpdate, sessionUser.getId());
            return true;
        }

        if (sessionUser != userToUpdate) {
            LOG.error("Session User (ID: {}) and User to update (ID: {}) do not match, blocking due to potential indirect access attack.", sessionUser.getId(), userToUpdate.getId());
            return false;
        }

        LOG.trace("Session User (ID: {}) and User to update (ID: {}) match. Approving.", sessionUser.getId(), userToUpdate.getId());
        return true;
    }

    public static UserDetailsDTO createUserDetailsDTO(User user, Player player) {
        return new UserDetailsDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                player.getPhoneNumber(),
                player.getAvailabilityNotes(),
                player.isAnonymised()
        );
    }

    public boolean doesPasswordMatchUserPassword(String currentPassword, User user) {
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    public boolean validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return false;
        }

        // TODO Password policy
        if (newPassword.length() < 6) {
            return false;
        }

        return true;
    }

    public void saveNewPasswordForUser(String newPassword, User user) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
