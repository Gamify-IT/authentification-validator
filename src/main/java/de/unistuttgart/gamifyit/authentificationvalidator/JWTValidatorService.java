package de.unistuttgart.gamifyit.authentificationvalidator;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Slf4j
public class JWTValidatorService {

    private final String keycloakIssuer;

    private JwkProvider jwkProvider;

    public JWTValidatorService(final String keycloakIssuer) throws MalformedURLException {
    this.keycloakIssuer = keycloakIssuer;
    this.getJwkProvider();
    }

    /**
     * gets the keys from the issuer (keycloak)
     *
     * @throws MalformedURLException if issuer is invalid
     */
    public void getJwkProvider() throws MalformedURLException {
        log.info("Use Issuer " + keycloakIssuer);
        final String url = keycloakIssuer + "/protocol/openid-connect/certs";
        jwkProvider = new UrlJwkProvider(new URL(url));
    }

    /**
     * select the public key matching the token encryption
     *
     * @param token the access token
     * @return the public key
     * @throws JwkException if public key is missing
     */
    private RSAPublicKey selectPublicKey(DecodedJWT token) throws JwkException {
        return (RSAPublicKey) jwkProvider.get(token.getKeyId()).getPublicKey();
    }

    /**
     * Validate a JWT token
     *
     * @param token the access token
     * @return decoded token
     * @throws ResponseStatusException Unauthorized if token is invalid
     */
    public DecodedJWT validate(String token) {
        try {
            final DecodedJWT jwt = JWT.decode(token);

            if (!keycloakIssuer.equals(jwt.getIssuer())) {
                throw new InvalidParameterException(String.format("Unknown Issuer %s", jwt.getIssuer()));
            }

            RSAPublicKey publicKey = selectPublicKey(jwt);

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(jwt.getIssuer())
                    .build();

            verifier.verify(token);
            log.info("Verified User " + jwt.getClaim("preferred_username"));
            return jwt;

        }catch (TokenExpiredException | JwkException e) {
            ResponseStatusException exception = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is invalid!");
            log.warn("Access denied {}", exception.getMessage());
            throw exception;
        }
    }

    /**
     * Checks if user is a lecturer
     *
     * @param accessToken token from the user
     * @throws ResponseStatusException Unauthorized if user is no lecturer
     */
    public void checkLecturer(String accessToken) {
        final List<String> roles = (List<String>) validate(accessToken).getClaims().get("realm_access").asMap().get("roles");
        if (!roles.contains("lecturer")) {
            ResponseStatusException exception = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is no lecturer!");
            log.warn("Access denied {}", exception.getMessage());
            throw exception;
        }
    }
}