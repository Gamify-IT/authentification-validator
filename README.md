# Authentification Validator

This is a spring service which validates jwt access tokens issued by keycloak. On invalid token it throws the
corresponding ResponseStatusException. \
This service is used by all Gamify-IT backends to validate the users.

## Usage

Needs following properties from the application:

| property key    | description                                  | example                                                    |
|-----------------|----------------------------------------------|------------------------------------------------------------|
| keycloak.url    | the realm url to fetch the certificates from | keycloak.url=http://localhost/keycloak/realms/Gamify-IT    |
| keycloak.issuer | the issuer mentioned in the tokens           | keycloak.issuer=http://localhost/keycloak/realms/Gamify-IT |

### Needed Dependencies

```xml

<repositories>
    <repository>
        <id>sqa-artifactory</id>
        <name>SQA Artifactory-releases</name>
        <url>https://rss-artifactory.ddnss.org/artifactory/libs-release</url>
    </repository>
</repositories>
```

```xml

<dependency>
    <groupId>de.uni-stuttgart.gamify-it</groupId>
    <artifactId>authentification-validator</artifactId>
    <version>v0.0.1</version>
</dependency>
```

## Class diagram
