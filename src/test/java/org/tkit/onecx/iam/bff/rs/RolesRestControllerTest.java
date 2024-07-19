package org.tkit.onecx.iam.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
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
import org.tkit.onecx.iam.bff.rs.controllers.RolesRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.RoleSearchCriteriaDTO;
import gen.org.tkit.onecx.iam.kc.client.model.ProblemDetailResponse;
import gen.org.tkit.onecx.iam.kc.client.model.Role;
import gen.org.tkit.onecx.iam.kc.client.model.RolePageResult;
import gen.org.tkit.onecx.iam.kc.client.model.RoleSearchCriteria;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@LogService
@TestHTTPEndpoint(RolesRestController.class)
class RolesRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    static final String MOCK_ID = "MOCK_ID";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(MOCK_ID);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void searchRolesByCriteriaTest() {

        Role role1 = new Role();
        role1.setName("role1");

        Role role2 = new Role();
        role1.setName("role2");

        List<Role> rolesList = new ArrayList<>();
        rolesList.add(role1);
        rolesList.add(role2);

        RolePageResult rolePageResult = new RolePageResult();
        rolePageResult.setNumber(20);
        rolePageResult.setTotalPages(5L);
        rolePageResult.setTotalElements(200L);
        rolePageResult.setStream(rolesList);

        //Mockserver
        mockServerClient.when(request().withPath("/internal/roles/search")
                .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(rolePageResult)));

        RoleSearchCriteriaDTO roleSearchCriteriaDTO = new RoleSearchCriteriaDTO();
        roleSearchCriteriaDTO.setName("rolesSearchTest1");
        roleSearchCriteriaDTO.setPageNumber(3);
        roleSearchCriteriaDTO.setPageSize(25);

        //Restassured
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(roleSearchCriteriaDTO)
                .post()
                .then()
                .statusCode(OK.getStatusCode());

    }

    @Test
    void searchRolesByCriteriaTest_shouldReturnBadRequest_whenBodyIsRequestBodyEmpty() {

        RoleSearchCriteriaDTO roleSearchCriteriaDTO = new RoleSearchCriteriaDTO();
        roleSearchCriteriaDTO.setName("rolesSearchTest1");
        roleSearchCriteriaDTO.setPageNumber(3);
        roleSearchCriteriaDTO.setPageSize(25);

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

        RoleSearchCriteria roleSearchCriteria = new RoleSearchCriteria();
        roleSearchCriteria.setName("rolesSearchTest1");
        roleSearchCriteria.setPageNumber(3);
        roleSearchCriteria.setPageSize(25);

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        //Mockserver
        mockServerClient.when(request().withPath("/internal/roles/search")
                .withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(roleSearchCriteria)))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        RoleSearchCriteriaDTO roleSearchCriteriaDTO = new RoleSearchCriteriaDTO();
        roleSearchCriteriaDTO.setName("rolesSearchTest1");
        roleSearchCriteriaDTO.setPageNumber(3);
        roleSearchCriteriaDTO.setPageSize(25);

        //Restassured
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(roleSearchCriteriaDTO)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

    }

}
