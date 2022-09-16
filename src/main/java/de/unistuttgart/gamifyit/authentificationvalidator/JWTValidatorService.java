package de.unistuttgart.gamifyit.authentificationvalidator;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * A very nice Service Class to validate JWT access tokens
 */
@Slf4j
@Service
public class JWTValidatorService {

    private JwkProvider jwkProvider;

    @Autowired
    private Properties properties;

    /**
     * gets the keys from the keycloak
     *
     * @throws MalformedURLException if issuer URI is invalid
     * @throws URISyntaxException    if issuer URI is invalid
     */
    @Autowired
    public void initializeJwkProvider() {
        if (properties == null || properties.getUrl() == null || properties.getUrl().isBlank()) {
            throw new IllegalArgumentException(
                String.format("keycloak URL is null or empty: \"%s\"", properties.getUrl())
            );
        }
        log.info("Use keycloak URL " + properties.getUrl());
        final String url = properties.getUrl() + "/protocol/openid-connect/certs";
        try {
            jwkProvider = new UrlJwkProvider(new URI(url).toURL());
        } catch (final URISyntaxException | MalformedURLException linkException) {
            throw new IllegalArgumentException("Illegal keycloak URL", linkException);
        }
    }

    /**
     * Returns the userId extracted from the token
     *
     * @param token the access token
     * @return UserId
     * @throws ResponseStatusException BadRequest if token cannot be decoded
     */
    public String extractUserId(final String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(String.format("Token is null or empty: \"%s\"", token));
        }
        return decodeToken(token).getSubject();
    }

    /**
     * Checks if user has the given roles
     *
     * @param token the access token
     * @throws ResponseStatusException Unauthorized if user has not all required roles <br>
     *                                 BadRequest if token cannot be decoded
     */
    public void hasRolesOrThrow(final String token, final List<String> roles) {
        @SuppressWarnings("unchecked")
        final List<String> userRoles = (List<String>) decodeToken(token)
            .getClaims()
            .get("realm_access")
            .asMap()
            .get("roles");
        if (!new HashSet<>(userRoles).containsAll(roles)) {
            final ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User has not all required roles!"
            );
            log.info("Access denied {}", exception.getMessage());
            throw exception;
        }
    }

    /**
     * Validate a JWT token
     *
     * @param token the access token
     * @throws ResponseStatusException Unauthorized if user has not all required roles <br>
     *                                 BadRequest if token cannot be decoded
     */
    public void validateTokenOrThrow(final String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(String.format("Token is null or empty: \"%s\"", token));
        }
        final DecodedJWT jwtToken = decodeToken(token);
        validateIssuerOrThrow(jwtToken);
        validateSignatureOrThrow(jwtToken);
    }

    /**
     * Decode a JWT token
     *
     * @param token the access token
     * @throws ResponseStatusException BadRequest if token cannot be decoded
     */
    private DecodedJWT decodeToken(final String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(String.format("Token is null or empty: \"%s\"", token));
        }
        final DecodedJWT jwtToken;
        try {
            jwtToken = JWT.decode(token);
        } catch (final JWTDecodeException validationException) {
            final ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Not able to decode Token!"
            );
            log.info("{} - {}", exception.getMessage(), validationException.getMessage());
            throw exception;
        }
        return jwtToken;
    }

    /**
     * select the public key matching the token signature
     *
     * @param token the access token
     * @return the public key
     * @throws JwkException if public key is missing
     */
    private RSAPublicKey selectPublicKey(final DecodedJWT token) throws JwkException {
        return (RSAPublicKey) jwkProvider.get(token.getKeyId()).getPublicKey();
    }

    /**
     * Validate issuer matches JWT token
     *
     * @param jwtToken the access token
     * @throws ResponseStatusException Unauthorized if token issuer is unknown
     */
    private void validateIssuerOrThrow(final DecodedJWT jwtToken) {
        if (!properties.getIssuer().equals(jwtToken.getIssuer())) {
            final ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                String.format("Unknown Issuer: %s", jwtToken.getIssuer())
            );
            log.info("Access denied {}", exception.getMessage());
            throw exception;
        }
    }

    /**
     * Validate signature of JWT token
     *
     * @param jwtToken the access token
     * @throws ResponseStatusException Unauthorized if token has an invalid signature
     */
    private void validateSignatureOrThrow(final DecodedJWT jwtToken) {
        try {
            final RSAPublicKey publicKey = selectPublicKey(jwtToken);
            final Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            final JWTVerifier verifier = JWT.require(algorithm).withIssuer(jwtToken.getIssuer()).build();
            verifier.verify(jwtToken);
        } catch (final JwkException validationException) {
            final ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Token is invalid!"
            );
            log.info("Access denied {} - {}", exception.getMessage(), validationException.getMessage());
            throw exception;
        }
    }
}
