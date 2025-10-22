package es.unex.gc01.paymentsservice.backend.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.oauth2.jwt.Jwt;

@RequiredArgsConstructor
@Setter
@Getter
public class AuthenticatedUser {
    private final String username;
    private final String email;

    public AuthenticatedUser(Jwt jwt) {
        this.username = jwt.getClaimAsString("preferred_username");
        this.email = jwt.getClaimAsString("email");
    }
}