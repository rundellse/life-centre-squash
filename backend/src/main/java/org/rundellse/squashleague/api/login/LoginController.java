package org.rundellse.squashleague.api.login;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class.getName());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextHolder securityContextHolder;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest.email() == null || loginRequest.email().isEmpty()
                || loginRequest.password() == null || loginRequest.password().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.email(), loginRequest.password());
        Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);

        // Set authentication in security context and session i.e. against the JSESSIONID.
        SecurityContextHolder.getContext().setAuthentication(authenticationResponse);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
