package org.rundellse.squashleague.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.rundellse.squashleague.api.player.dto.NewPlayerDetailsDTO;
import org.rundellse.squashleague.api.player.dto.PlayerDetailsDTO;
import org.rundellse.squashleague.api.user.dto.PasswordUpdateAdminDTO;
import org.rundellse.squashleague.api.user.dto.UserDetailsDTO;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.model.user.Role;
import org.rundellse.squashleague.model.user.User;
import org.rundellse.squashleague.model.user.UserRole;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.RoleRepository;
import org.rundellse.squashleague.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public UserDetailsDTO getUserDetails(HttpServletRequest request, long userId) {
        User user = getUserForId(userId);
        validateUserAgainstRequest(request, user);

        // There is (almost) no requirement in the model for a User to have a player, but for now everyone will, users are/will be
        // created one-to-one with players, and both admins are players. If there is a legitimate User in future with no
        // corresponding Player this will need to be updated.
        Player player = user.getPlayer();

        return createUserDetailsDTO(user, player);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void saveUserDetails(HttpServletRequest request, Long userId, UserDetailsDTO userDetailsDTO) {
        User user = getUserForId(userId);
        validateUserAgainstRequest(request, user);
        validateUserDetailsDTO(userDetailsDTO);

        Player player = user.getPlayer();

        user.setName(userDetailsDTO.name());
        user.setEmail(userDetailsDTO.email());

        player.setName(userDetailsDTO.name());
        player.setEmail(userDetailsDTO.email());
        player.setPhoneNumber(userDetailsDTO.phoneNumber());
        player.setAvailabilityNotes(userDetailsDTO.availabilityNotes());
        player.setAnonymised(userDetailsDTO.anonymise());

        userRepository.save(user);
        playerRepository.save(player);
    }

    private static void validateUserDetailsDTO(UserDetailsDTO userDetailsDTO) {
        if (userDetailsDTO.name() == null || userDetailsDTO.name().isBlank() || userDetailsDTO.name().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must be at least 2 characters long");
        }

        if (userDetailsDTO.email() == null || userDetailsDTO.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
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

    private void validateUserAgainstRequest(HttpServletRequest request, User userToUpdate) {
        User sessionUser = getSessionUser(request);

        if (request.isUserInRole(Role.ROLE_ADMIN.name())) {
            LOG.info("User details update for User (ID: {}) performed by administrator (ID: {}), not validating session User against User to update.", userToUpdate, sessionUser.getId());
            return;
        }

        if (sessionUser != userToUpdate) {
            String message = "Session User (ID: {}) and User to update (ID: {}) do not match, blocking due to potential indirect access attack.";
            LOG.error(message, sessionUser.getId(), userToUpdate.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }

        LOG.trace("Session User (ID: {}) and User to update (ID: {}) match. Approving.", sessionUser.getId(), userToUpdate.getId());
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

    public static void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            LOG.debug("New password violates password policy for attempted password update: No password defined.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // TODO Password policy
        if (newPassword.length() < 8) {
            LOG.debug("New password violates password policy for attempted password update: Must be 8 characters or longer.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public void saveNewPasswordForUser(String newPassword, User user) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User createUserFromNewPlayerDetailsDTO(NewPlayerDetailsDTO newPlayerDetailsDTO, Player player) {
        HashSet<UserRole> userRoles = assembleUserRoles(newPlayerDetailsDTO);
        validateNewPassword(newPlayerDetailsDTO.password());

        return new User(
                newPlayerDetailsDTO.name(),
                newPlayerDetailsDTO.email(),
                newPlayerDetailsDTO.password(),
                userRoles,
                player
        );
    }

    private HashSet<UserRole> assembleUserRoles(NewPlayerDetailsDTO newPlayerDetailsDTO) {
        HashSet<UserRole> userRoles = new HashSet<>(1);
        if (newPlayerDetailsDTO.adminUser()) {
            userRoles.add(roleRepository.findByRole(Role.ROLE_ADMIN));
        } else {
            userRoles.add(roleRepository.findByRole(Role.ROLE_USER));
        }
        return userRoles;
    }

    private HashSet<UserRole> assembleUserRoles(PlayerDetailsDTO newPlayerDetailsDTO) {
        HashSet<UserRole> userRoles = new HashSet<>(1);
        if (newPlayerDetailsDTO.adminUser()) {
            userRoles.add(roleRepository.findByRole(Role.ROLE_ADMIN));
        } else {
            userRoles.add(roleRepository.findByRole(Role.ROLE_USER));
        }
        return userRoles;
    }

    public boolean isUserAdmin(Player player) {
        User user = player.getUser();
        for (UserRole userRole : user.getUserRoles()) {
            if (userRole.getRole().equals(Role.ROLE_ADMIN)) {
                return true;
            }
        }
        return false;
    }

    public void updateUserFromPlayerDetailsDTO(User updatedUser, PlayerDetailsDTO playerDetailsDTO) {
        updatedUser.setName(playerDetailsDTO.name());
        updatedUser.setEmail(playerDetailsDTO.email());

        updatedUser.setUserRoles(assembleUserRoles(playerDetailsDTO));
    }

    public void updatePasswordAdmin(PasswordUpdateAdminDTO passwordUpdateAdminDTO) {
        validateNewPassword(passwordUpdateAdminDTO.newPassword());

        Optional<Player> playerOptional = playerRepository.findById(passwordUpdateAdminDTO.playerId());
        if (playerOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No player found for ID: " + passwordUpdateAdminDTO.playerId());
        }

        User userToUpdate = playerOptional.get().getUser();
        userToUpdate.setPassword(passwordUpdateAdminDTO.newPassword());
    }
}
