package org.tkit.onecx.iam.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.iam.bff.rs.controllers.UsersRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserSearchCriteriaDTO;
import gen.org.tkit.onecx.iam.kc.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@LogService
@TestHTTPEndpoint(UsersRestController.class)
class UsersRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    static final String mockId = "MOCK_ID_USER";

    @BeforeEach
    void resetExpectation() {

        try {
            mockServerClient.clear(mockId);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void searchUsersByCriteriaTest() {

        User user1 = new User();
        user1.setId("userId1");
        user1.setFirstName("firstname1");
        user1.setLastName("lastname1");
        user1.setUsername("username1");
        user1.setEmail("user1@mail.com");

        User user2 = new User();
        user2.setId("userId2");
        user2.setFirstName("firstname2");
        user2.setLastName("lastname2");
        user2.setUsername("username2");
        user2.setEmail("user2@mail.com");

        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        UserPageResult userPageResult = new UserPageResult();
        userPageResult.setNumber(20);
        userPageResult.setTotalPages(5L);
        userPageResult.setTotalElements(200L);
        userPageResult.setStream(userList);

        //Mockserver
        mockServerClient.when(request().withPath("/internal/users/search")
                .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(userPageResult)));

        UserSearchCriteriaDTO userSearchCriteriaDTO = new UserSearchCriteriaDTO();
        userSearchCriteriaDTO.setPageNumber(3);
        userSearchCriteriaDTO.setPageSize(25);

        //Restassured
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(userSearchCriteriaDTO)
                .post()
                .then()
                .statusCode(OK.getStatusCode());

    }

    @Test
    void searchRolesByCriteriaTest_shouldReturnBadRequest_whenBodyIsRequestBodyEmpty() {

        //Restassured
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", res.getErrorCode());

    }

    @Test
    void searchRolesByCriteriaTest_shouldReturnBadRequest_whenBadRequestResponse() {

        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setPageNumber(3);
        userSearchCriteria.setPageSize(25);

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        //Mockserver
        mockServerClient.when(request().withPath("/internal/users/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(userSearchCriteria)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        UserSearchCriteriaDTO userSearchCriteriaDTO = new UserSearchCriteriaDTO();
        userSearchCriteriaDTO.setPageNumber(3);
        userSearchCriteriaDTO.setPageSize(25);

        //Restassured
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(userSearchCriteriaDTO)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(res);
    }

}
