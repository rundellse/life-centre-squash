package org.rundellse.squashleague;

import org.rundellse.squashleague.api.player.PlayerRestController;
import org.rundellse.squashleague.model.Player;
import org.rundellse.squashleague.persistence.H2DatabaseConnection;
import org.rundellse.squashleague.persistence.PlayerH2DAO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class SquashLeagueConfiguration {

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistration() {
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
    public PlayerRestController playerRestController() {
        return new PlayerRestController(playerH2DAO());
    }



}
