package es.unex.gc01.paymentsservice.backend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class JwtToAuthenticatedUserConverter implements Converter<Jwt, AuthenticatedUserToken> {
    @Override
    public AuthenticatedUserToken convert(Jwt jwt) {
        AuthenticatedUser user = new AuthenticatedUser(jwt);
        return new AuthenticatedUserToken(jwt, user, List.of());
    }
}
