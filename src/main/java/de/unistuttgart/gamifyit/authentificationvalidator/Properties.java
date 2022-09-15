package de.unistuttgart.gamifyit.authentificationvalidator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix="keycloak")
@Configuration
public class Properties {
    public String issuer = "";
    public String url = "";
}
