package de.unistuttgart.gamifyit.authentificationvalidator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Properties containing the keycloak url and issuer-url
 * Gets configuration with prefix "keycloak" from the application properties
 */
@ConfigurationProperties(prefix = "keycloak")
@Configuration
@ComponentScan
public class Properties {

    private String issuer = "";
    private String url = "";

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}
