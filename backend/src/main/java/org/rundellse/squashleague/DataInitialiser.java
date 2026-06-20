package org.rundellse.squashleague;

import jakarta.annotation.PostConstruct;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitialiser {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitialiser.class.getName());


    private final RoleRepository roleRepository;

    private final PlayerRepository playerRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    @Autowired
    public DataInitialiser(RoleRepository roleRepository, PlayerRepository playerRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostConstruct
    public void init() {
        if (userRepository.count() != 0) {
            LOG.trace("User repository already populated. No data init.");
            return;
        }

        LOG.info("Starting Data init.");
        UserRole userRole = roleRepository.save(new UserRole(Role.ROLE_USER));
        UserRole adminRole = roleRepository.save(new UserRole(Role.ROLE_ADMIN));

        Player adminPlayer = playerRepository.save(new Player(
                "Admin Guy",
                "admin@email.com",
                "017777777",
                "Availability",
                0,
                false,
                false
        ));

        User admin = new User();
        admin.setName("admin");
        admin.setEmail("admin@email.com");
        admin.setPassword(passwordEncoder.encode("adminr41"));
        admin.setPlayer(adminPlayer);
        adminPlayer.setUser(admin);
        admin.getUserRoles().add(adminRole);
        userRepository.save(admin);
        playerRepository.save(adminPlayer);

        Player userPlayer = playerRepository.save(new Player(
                "User Gal",
                "user@email.com",
                "017777776",
                "Availability",
                0,
                false,
                false
        ));

        User user = new User();
        user.setName("user");
        user.setEmail("user@email.com");
        user.setPassword(passwordEncoder.encode("userr41"));
        user.setPlayer(userPlayer);
        userPlayer.setUser(user);
        user.getUserRoles().add(userRole);
        userRepository.save(user);
        playerRepository.save(userPlayer);


        for (int i = 1; i < 12; i++) {
            Player player = playerRepository.save(new Player(
                    "Player " + i,
                    "email" + i + "@email.com",
                    "07777777" + i,
                    "Availability" + i,
                    i % 3,
                    false,
                    false
            ));

            User testUser = new User();
            testUser.setName("Player " + i);
            testUser.setEmail("email" + i + "@email.com");
            testUser.setPassword(passwordEncoder.encode("password" + i));
            testUser.getUserRoles().add(userRole);
            testUser.setPlayer(player);
            userRepository.save(testUser);

        }

        LOG.info("Data init complete.");
    }


}
