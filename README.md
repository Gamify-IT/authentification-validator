# Authentification Validator

This is a spring service which validates jwt access tokens issued by keycloak. \
When it encounters an invalid token, it throws a `ResponseStatusException` with the corresponding HTTP status. \
This service is used by all Gamify-IT backends to validate the users and to require authentication for specific routes.

<!-- TOC -->
* [Links](#links)
* [Usage](#usage)
    * [Properties](#properties)
    * [Needed Dependencies](#needed-dependencies)
* [Class diagram](#class-diagram)
<!-- TOC -->

## Links

- The developer docs can be found [here](https://gamifyit-docs.readthedocs.io/en/latest/dev-manuals/authentication/authentication-validator.html).
- For more info about the authentication cookie, see the [keycloak manual](https://gamifyit-docs.readthedocs.io/en/latest/dev-manuals/authentication/keycloak.html).

## Usage

### Properties

Needs following properties from the application:

| Property key    | Environment variable | Description                                  | Example                                                    |
|-----------------|----------------------|----------------------------------------------|------------------------------------------------------------|
| keycloak.url    | KEYCLOAK_URL         | the realm url to fetch the certificates from | keycloak.url=http://localhost/keycloak/realms/Gamify-IT    |
| keycloak.issuer | KEYCLOAK_ISSUER      | the issuer mentioned in the tokens           | keycloak.issuer=http://localhost/keycloak/realms/Gamify-IT |

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
    <version>v1.0.0</version>
</dependency>
```

## Class diagram

![class diagram](https://raw.githubusercontent.com/Gamify-IT/docs/main/dev-manuals/authentication/assets/authentication_validator_class_diagram.webp)
