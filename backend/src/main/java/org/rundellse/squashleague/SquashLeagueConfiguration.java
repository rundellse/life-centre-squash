package org.rundellse.squashleague;

import jakarta.servlet.http.HttpServletResponse;
import org.rundellse.squashleague.api.login.custom.CustomUserDetailsService;
import org.rundellse.squashleague.api.player.PlayerRestController;
import org.rundellse.squashleague.model.user.Roles;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableJpaRepositories
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
    public PlayerRestController playerRestController() {
        return new PlayerRestController();
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
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
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
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8081/")
                        .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name())
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                                .requestMatchers(HttpMethod.GET, "/players/**").hasAnyAuthority(Roles.ROLE_USER.toString(), Roles.ROLE_ADMIN.toString())
                                .requestMatchers(HttpMethod.POST, "/players/**").hasAuthority(Roles.ROLE_ADMIN.toString())
                                .requestMatchers(HttpMethod.POST, "/table/**").hasAuthority(Roles.ROLE_ADMIN.toString())
                                .requestMatchers(HttpMethod.GET, "/user/**").hasAuthority(Roles.ROLE_USER.toString())
                                .requestMatchers(HttpMethod.POST, "/user/**").hasAnyAuthority(Roles.ROLE_USER.toString(), Roles.ROLE_ADMIN.toString())
                )
                .logout(logoutConfigurer ->
                        logoutConfigurer
                                .logoutRequestMatcher(new MvcRequestMatcher(new HandlerMappingIntrospector(), "/logout"))
                                .logoutSuccessHandler((request, response, authentication) -> {
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    response.setContentType("application/json");

                                    // CORS mappings are added for Controller Endpoints only by addCorsMappings above. Needs
                                    // to be added manually for the logout response. According to Spring this is still preferred,
                                    // rather than creating a custom logout to ensure logout is done fully and properly. I'll take their word for it.
                                    response.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
                                    response.setHeader("Access-Control-Allow-Credentials", "true");
                                    response.getWriter().write("{\"message\": \"Logged out\"}");
                                })
                                .permitAll()
                );
        return http.build();
    }


//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private PlayerRepository playerRepository;
//
//    @Bean
//    public CommandLineRunner init() {
//        userRepository.deleteAll();
//        User user;
//        Optional<User> userOptional = userRepository.findById(1L);
//        if (userOptional.isPresent()) {
//            user = userOptional.get();
//        } else {
//            user = new User();
//            user.setName("user");
//            user.setEmail("user@email.com");
//            user.setPassword(passwordEncoder().encode("password"));
//        }
//        User admin = new User();
//        admin.setName("admin");
//        admin.setEmail("admin@email.com");
//        admin.setPassword(passwordEncoder().encode("password"));
//
//        Role userRole = roleRepository.findByName(Roles.ROLE_USER.toString());
//        if (userRole == null) {
//            roleRepository.save(new Role(Roles.ROLE_USER.toString()));
//        }
//
//        Role adminRole = roleRepository.findByName(Roles.ROLE_ADMIN.toString());
//        if (adminRole == null) {
//            roleRepository.save(new Role(Roles.ROLE_ADMIN.toString()));
//        }
//
//        user.getUserRoles().add(userRole);
//        admin.getUserRoles().add(adminRole);
//
//        return args -> {
//            userRepository.save(user);
//            userRepository.save(admin);
//            for (int i = 0; i < 24; i++) {
//                playerRepository.save(new Player(
//                        "Player " + i,
//                        "email"+i+"@email.com",
//                        "07777777"+i,
//                        "Availibility"+i,
//                        i%4,
//                        false,
//                        false
//                ));
//            }
//        };
//    }
}
