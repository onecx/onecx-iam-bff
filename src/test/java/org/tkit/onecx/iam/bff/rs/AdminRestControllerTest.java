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
import org.tkit.onecx.iam.bff.rs.controllers.AdminRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.*;
import gen.org.tkit.onecx.iam.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@LogService
@TestHTTPEndpoint(AdminRestController.class)
class AdminRestControllerTest extends AbstractTest {

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
        mockServerClient.when(request().withPath("/internal/admin/users/search")
                .withBody(JsonBody.json(new UserSearchCriteria().issuer("testIssuer").pageSize(25).pageNumber(1)))
                .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(userPageResult)));

        UserSearchCriteriaDTO userSearchCriteriaDTO = new UserSearchCriteriaDTO();
        userSearchCriteriaDTO.setIssuer("testIssuer");
        userSearchCriteriaDTO.setPageNumber(1);
        userSearchCriteriaDTO.setPageSize(25);

        //Restassured
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .body(userSearchCriteriaDTO)
                .post("/users/search")
                .then()
                .statusCode(OK.getStatusCode());

    }

    @Test
    void searchUsersByCriteriaTest_shouldReturnBadRequest_whenBodyIsRequestBodyEmpty() {

        //Restassured
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .post("/users/search")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", res.getErrorCode());

    }

    @Test
    void searchUsersByCriteriaTest_shouldReturnBadRequest_whenBadRequestResponse() {

        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        userSearchCriteria.setIssuer("testIssuer");
        userSearchCriteria.setPageNumber(1);
        userSearchCriteria.setPageSize(1);
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        //Mockserver
        mockServerClient.when(request().withPath("/internal/admin/users/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(userSearchCriteria)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        UserSearchCriteriaDTO userSearchCriteriaDTO = new UserSearchCriteriaDTO();
        userSearchCriteriaDTO.setPageNumber(1);
        userSearchCriteriaDTO.setPageSize(1);
        userSearchCriteriaDTO.setIssuer("testIssuer");

        //Restassured
        var res = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .body(userSearchCriteriaDTO)
                .post("/users/search")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(res);
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
        mockServerClient.when(request().withPath("/internal/admin/roles/search")
                .withBody(JsonBody.json(new RoleSearchCriteria().issuer("testIssuer").name("rolesSearchTest1")))
                .withMethod(HttpMethod.POST))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(rolePageResult)));

        RoleSearchCriteriaDTO roleSearchCriteriaDTO = new RoleSearchCriteriaDTO();
        roleSearchCriteriaDTO.setName("rolesSearchTest1");
        roleSearchCriteriaDTO.setIssuer("testIssuer");

        //Restassured
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .body(roleSearchCriteriaDTO)
                .post("/roles/search")
                .then()
                .statusCode(OK.getStatusCode());

    }

    @Test
    void getUserRolesByUserId() {
        UserRolesResponseDTO rolesReponse = new UserRolesResponseDTO();
        rolesReponse.roles(List.of(new RoleDTO().name("role1")));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/admin/user1/roles").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(new UserRolesSearchRequest().issuer("testIssuer"))))
                .withId(MOCK_ID)
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(rolesReponse)));

        var result = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .pathParam("userId", "user1")
                .body(new SearchUserRolesRequestDTO().issuer("testIssuer"))
                .post("/{userId}/roles")
                .then()
                .statusCode(OK.getStatusCode()).extract().as(UserRolesResponseDTO.class);
        Assertions.assertEquals("role1", result.getRoles().get(0).getName());
    }

    @Test
    void getAllProvidersAndRealms() {
        ProvidersResponse providersResponseKc = new ProvidersResponse();
        providersResponseKc.setProviders(List.of(new Provider().name("kc1").domains(List.of(new Domain().name("realm1")))));
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/admin/providers").withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(providersResponseKc)));

        var result = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .get("/providers")
                .then()
                .statusCode(OK.getStatusCode()).extract().as(ProvidersResponseDTO.class);
        Assertions.assertEquals("kc1", result.getProviders().get(0).getName());
        Assertions.assertEquals("realm1",
                result.getProviders().get(0).getDomains().get(0).getName());
    }

    @Test
    void createUserTest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@test.com");
        request.setIssuer("someIss");
        request.setUsername("userName");
        request.setLastName("lastName");
        request.setFirstName("firstName");
        request.setTemporaryPassword("pw");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/admin/users").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode()));

        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO();
        requestDTO.setEmail("test@test.com");
        requestDTO.setIssuer("someIss");
        requestDTO.setUsername("userName");
        requestDTO.setLastName("lastName");
        requestDTO.setFirstName("firstName");
        requestDTO.setTemporaryPassword("pw");
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/users")
                .then()
                .statusCode(CREATED.getStatusCode());
    }

    @Test
    void updateUsertest() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setIssuer("newIss");
        request.setEmail("test@test.com");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/admin/users/123").withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        UpdateUserRequestDTO requestDTO = new UpdateUserRequestDTO();
        requestDTO.setIssuer("newIss");
        requestDTO.setEmail("test@test.com");

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .put("/users/123")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void createRoleTest() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setIssuer("iss");
        request.setName("newRole");
        request.setDescription("desc");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/admin/roles").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode()));

        CreateRoleRequestDTO requestDTO = new CreateRoleRequestDTO();
        requestDTO.setIssuer("iss");
        requestDTO.setName("newRole");
        requestDTO.setDescription("desc");
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/roles")
                .then()
                .statusCode(CREATED.getStatusCode());
    }

    @Test
    void assignRoleTest() {
        RoleAssignmentRequest request = new RoleAssignmentRequest();
        request.setIssuer("iss");
        request.setNames(List.of("role1"));
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/admin/123/roles/assign").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        RoleAssignmentRequestDTO requestDTO = new RoleAssignmentRequestDTO();
        requestDTO.setIssuer("iss");
        requestDTO.setNames(List.of("role1"));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/123/roles/assign")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }
}
