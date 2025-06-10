package org.tkit.onecx.iam.bff.rs;

import static org.keycloak.common.util.Encode.urlEncode;

import java.util.List;

import org.eclipse.microprofile.config.ConfigProvider;
import org.keycloak.representations.AccessTokenResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;

@QuarkusTestResource(MockServerTestResource.class)
public abstract class AbstractTest {
    protected static final String ADMIN = "alice";

    protected static final String USER = "bob";

    protected static final String APM_HEADER_PARAM = ConfigProvider.getConfig()
            .getValue("%test.tkit.rs.context.token.header-param", String.class);

    protected AccessTokenResponse getTokens(KeycloakTestClient ktc, String userName) {
        return getTokens(ktc, userName, userName);
    }

    protected AccessTokenResponse getTokens(KeycloakTestClient ktc, String userName, String password) {

        String clientId = "quarkus-app";
        String clientSecret = "secret";
        List<String> scopes = List.of("openid");

        String authServerUrl = ktc.getAuthServerUrl();

        RequestSpecification requestSpec = RestAssured.given()
                .param("grant_type", "password")
                .param("username", userName)
                .param("password", password)
                .param("client_id", clientId)
                .param("client_secret", clientSecret)
                .param("scope", urlEncode(String.join(" ", scopes)));

        return requestSpec.when().post(authServerUrl + "/protocol/openid-connect/token").then()
                .extract()
                .as(AccessTokenResponse.class);
    }

    static {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> {
                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper.registerModule(new JavaTimeModule());
                            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                            return objectMapper;
                        }));
    }
}
