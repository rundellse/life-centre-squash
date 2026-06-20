package org.rundellse.squashleague;

import jakarta.servlet.http.HttpServletResponse;
import org.rundellse.squashleague.api.login.custom.CustomUserDetailsService;
import org.rundellse.squashleague.model.user.Role;
import org.rundellse.squashleague.persistence.PlayerRepository;
import org.rundellse.squashleague.persistence.RoleRepository;
import org.rundellse.squashleague.persistence.SeasonRepository;
import org.rundellse.squashleague.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableJpaRepositories
public class SquashLeagueConfiguration implements WebMvcConfigurer {

    @Value("${squash.league.host.address:http://localhost:8081/}")
    private String hostAddress;

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistration() {
        // Prepend all api paths with '/api'. Done simply for clarity.
        return new DispatcherServletRegistrationBean(dispatcherServlet(), "/api");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public SecurityContextHolder securityContextHolder() {
        return new SecurityContextHolder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(hostAddress, "www." + hostAddress)
                        .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name())
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(14400);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        //TODO Implement csrf, disabled for now.
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(requestMatcherRegistry ->
                        requestMatcherRegistry
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/season/end-date").permitAll()
                                .requestMatchers(HttpMethod.GET, "/players/**").hasAnyAuthority(Role.ROLE_ADMIN.toString(), Role.ROLE_USER.toString())
                                .requestMatchers(HttpMethod.POST, "/players/**").hasAuthority(Role.ROLE_ADMIN.toString())
                                .requestMatchers(HttpMethod.GET, "/table/**").hasAuthority(Role.ROLE_ADMIN.toString())
                                .requestMatchers(HttpMethod.POST, "/table/**").hasAuthority(Role.ROLE_ADMIN.toString())
                                .requestMatchers(HttpMethod.GET, "/user/**").hasAnyAuthority(Role.ROLE_ADMIN.toString(), Role.ROLE_USER.toString())
                                .requestMatchers(HttpMethod.POST, "/user/**").hasAnyAuthority(Role.ROLE_ADMIN.toString(), Role.ROLE_USER.toString())
                                .requestMatchers(HttpMethod.POST, "/match/**").hasAnyAuthority(Role.ROLE_ADMIN.toString(), Role.ROLE_USER.toString())
                                .requestMatchers(HttpMethod.POST, "/admin/**").hasAuthority(Role.ROLE_ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE).hasAuthority(Role.ROLE_ADMIN.toString())
                )
                .logout(logoutConfigurer ->
                        logoutConfigurer
                                .logoutUrl("/api/logout")
                                .logoutSuccessHandler((request, response, authentication) -> {
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    response.setContentType("application/json");

                                    // CORS mappings are added for Controller Endpoints only by addCorsMappings above. Needs
                                    // to be added manually for the logout response. According to Spring this is still preferred,
                                    // rather than creating a custom logout, to ensure logout is done fully and properly. I'll take their word for it.
                                    response.setHeader("Access-Control-Allow-Origin", hostAddress);
                                    response.setHeader("Access-Control-Allow-Credentials", "true");
                                    response.getWriter().write("{\"message\": \"Logged out\"}");
                                })
                                .permitAll()
                );
        return http.build();
    }


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SeasonRepository seasonRepository;
}
