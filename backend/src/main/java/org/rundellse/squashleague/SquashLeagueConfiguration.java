package org.rundellse.squashleague;

import org.rundellse.squashleague.api.player.PlayerRestController;
import org.rundellse.squashleague.persistence.*;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SquashLeagueConfiguration implements WebMvcConfigurer {

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistration() {
        // Prepend all api paths with '/api/'. Done simply for clarity.
        return new DispatcherServletRegistrationBean(dispatcherServlet(), "/api/");
    }

    @Bean
    public H2DatabaseConnection h2DatabaseConnection() {
        return new H2DatabaseConnection();
    }

    @Bean
    public PlayerH2DAO playerH2DAO() {
        return new PlayerH2DAO();
    }

    @Bean
    public SeasonH2DAO seasonH2DAO() {
        return new SeasonH2DAO();
    }

    @Bean
    public SquashMatchH2DAO squashMatchH2DAO() {
        return new SquashMatchH2DAO();
    }

    @Bean
    public UserH2Dao userH2Dao() {
        return new UserH2Dao();
    }

    @Bean
    public PlayerRestController playerRestController() {
        return new PlayerRestController(playerH2DAO());
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(HttpMethod.OPTIONS);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        // Example only, to go before any real deployment, of course.
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password1")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //TODO Implement csrf, disabled for now.
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

//    @Bean
//    public CommandLineRunner initDatabaseTables() {
//        PlayerH2DAO playerH2DAO = playerH2DAO();
//        SeasonH2DAO seasonH2DAO = seasonH2DAO();
//        SquashMatchH2DAO squashMatchH2DAO = squashMatchH2DAO();
//        UserH2Dao userH2Dao = userH2Dao();
//
//        return args -> {
//            playerH2DAO.createTable();
//            seasonH2DAO.createTable();
//            squashMatchH2DAO.createTable();
//            userH2Dao.createTables();
//        };
//
//    }

}
