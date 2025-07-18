package org.tkit.onecx.iam.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.iam.bff.rs.controllers.UserRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProvidersResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserResetPasswordRequestDTO;
import gen.org.tkit.onecx.iam.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@LogService
@TestHTTPEndpoint(UserRestController.class)
class UserRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    static final String MOCK_ID = "MOCK_ID";
    static final String MOCK_KEYCLOAK_CLIENT = "MOCK_KC";
    static final String MOCK_AWS_CLIENT = "MOCK_AWS";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(MOCK_ID);
            mockServerClient.clear(MOCK_KEYCLOAK_CLIENT);
            mockServerClient.clear(MOCK_AWS_CLIENT);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void resetPasswordTest() {

        var tokens = getTokens(keycloakClient, ADMIN);
        var aliceToken = tokens.getIdToken();

        UserResetPasswordRequest passwortReset = new UserResetPasswordRequest();
        passwortReset.setPassword("new_password");
        //Mockserver
        mockServerClient.when(request().withPath("/internal/me/password")
                .withMethod(HttpMethod.PUT).withBody(JsonBody.json(passwortReset)))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode((Response.Status.NO_CONTENT.getStatusCode()))
                        .withContentType(MediaType.APPLICATION_JSON));

        UserResetPasswordRequestDTO userResetPasswordRequestDTO = new UserResetPasswordRequestDTO();
        userResetPasswordRequestDTO.setPassword("new_password");
        //Restassured
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, aliceToken)
                .contentType(APPLICATION_JSON)
                .body(userResetPasswordRequestDTO)
                .put("/password")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void resetPasswordTest_shouldReturnBadRequest_whenBodyIsRequestBodyEmpty() {
        var tokens = getTokens(keycloakClient, ADMIN);
        var aliceToken = tokens.getIdToken();

        //Restassured
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, aliceToken)
                .contentType(APPLICATION_JSON)
                .put("/password")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", res.getErrorCode());

    }

    @Test
    void resetPasswordTest_shouldReturnBadRequest_whenBadRequestResponse() {
        UserResetPasswordRequest userResetPasswordRequest = new UserResetPasswordRequest();
        userResetPasswordRequest.setPassword("new_password");

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        var tokens = getTokens(keycloakClient, ADMIN);
        var aliceToken = tokens.getIdToken();

        //Mockserver
        mockServerClient.when(request().withPath("/internal/me/password").withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(userResetPasswordRequest)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        UserResetPasswordRequestDTO userResetPasswordRequestDTO = new UserResetPasswordRequestDTO();
        userResetPasswordRequestDTO.setPassword("new_password");

        //Restassured
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, aliceToken)
                .contentType(APPLICATION_JSON)
                .body(userResetPasswordRequestDTO)
                .put("/password")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(res);
    }

    @Test
    void resetPasswordTest_shouldReturnNotAuthorized_whenNoBearerToken() {

        UserResetPasswordRequestDTO userResetPasswordRequestDTO = new UserResetPasswordRequestDTO();
        userResetPasswordRequestDTO.setPassword("new_password");

        //Restassured
        given()
                .when()
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(USER))
                .contentType(APPLICATION_JSON)
                .body(userResetPasswordRequestDTO)
                .put("/password")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getUserProviderAndRealm() {
        var tokens = getTokens(keycloakClient, ADMIN);
        var aliceToken = tokens.getIdToken();

        ProvidersResponse providersResponse = new ProvidersResponse();
        providersResponse.setProviders(List.of(new Provider().name("kc1").domains(List.of(new Domain().name("realm1")))));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/me/provider").withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(providersResponse)));

        var result = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, aliceToken)
                .contentType(APPLICATION_JSON)
                .get("/provider")
                .then()
                .statusCode(OK.getStatusCode()).extract().as(ProvidersResponseDTO.class);
        Assertions.assertEquals("kc1", result.getProviders().get(0).getName());
        Assertions.assertEquals("realm1",
                result.getProviders().get(0).getDomains().get(0).getName());
    }
}
