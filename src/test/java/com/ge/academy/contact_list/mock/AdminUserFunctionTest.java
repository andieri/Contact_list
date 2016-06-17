package com.ge.academy.contact_list.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ge.academy.contact_list.utils.UserBuilder;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ge.academy.contact_list.ContactListApplication;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by 212566304 on 6/13/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ContactListApplication.class)
@WebAppConfiguration
public class AdminUserFunctionTest {
    @Autowired
    private WebApplicationContext ctx;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    private String createUserJson(String username, String password) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userName", username);
        node.put("password", password);
        node.put("role", "USER");
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node).toString();
    }

    private String createCredentialUserJson(String username, String password) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userName", username);
        node.put("password", password);
        node.put("role", "USER");
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node).toString();
    }

    private String createAUsernamePasswordNewPasswordJson(String username, String password, String newPassword) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("username", username);
        node.put("oldPassword", password);
        node.put("newPassword", newPassword);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node).toString();
    }


    /**
     * It is a helper function to create a login for a user. It given a username and password.
     * It returns a ResultActions object which can tested.
     *
     * @param username the username of a user
     * @param password the password of a user
     * @return
     * @throws Exception
     */
    private ResultActions loginUser(String username, String password) throws Exception {
        String json = createUserJson(username, password);
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .param("password", password));
    }


    /**
     * Helper function for admin user to create a new user with the given username and password.
     * It returns a ResultActions object.
     *
     * @param username   given username for the user
     * @param password   given password for the user
     * @param adminToken given token of the logged in admin user
     * @return
     * @throws Exception
     */
    private ResultActions createUser(String username, String password, String adminToken) throws Exception {

        String json = createCredentialUserJson(username, password);
        return mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", adminToken));
    }

    /**
     * @throws Exception
     */
    @Test
    public void whenLoggedInAdminCreateUserShouldReturnHTTPStatusOK() throws Exception {
        //Given
        String adminToken = new UserBuilder(ctx).createAdminUser().build().getAuthenticationString();
        //when
        ResultActions createdUser = this.createUser("user1", "pw1", adminToken);
        //then
        createdUser.andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void adminUserCreatedUserWhenLoginShouldReturnHTTPStatusOK() throws Exception {
        //Given
        String adminToken = new UserBuilder(ctx).createAdminUser().build().getAuthenticationString();
        this.createUser("user1", "pw1", adminToken)
                .andReturn().getResponse().getContentAsString();
        //when
        ResultActions loggedInUser = this.loginUser("user1", "pw1");
        //then
        loggedInUser.andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void adminUserCreatedUserWhenLoginShouldReturnTheAuthorizationTokenOfTheUser() throws Exception {
        //Given
        String adminToken = new UserBuilder(ctx).createAdminUser().build().getAuthenticationString();

        this.createUser("user1", "pw1", adminToken)
                .andReturn().getResponse().getContentAsString();
        //when
        ResultActions loggedInUser = this.loginUser("user1", "pw1");
        //then
        String userTokenJson = loggedInUser
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.tokenId").exists())
                .andExpect(jsonPath("$.tokenId").isString()).andReturn().getResponse().getContentAsString();
        String userToken = JsonPath.read(userTokenJson, "$.tokenId");
        assertEquals(adminToken.length(), userToken.length());
        assertNotEquals(adminToken, userToken);
    }

    @Test
    public void twoDifferentLoggedInUserAuthorizationTokenIsNotSame() throws Exception {
        //Given
        String adminToken = new UserBuilder(ctx).createAdminUser().build().getAuthenticationString();

        this.createUser("user1", "pw1", adminToken).andExpect(status().isCreated());

        this.createUser("user2", "pw2", adminToken).andExpect(status().isCreated());

        //when
        String user1TokenJson = this.loginUser("user1", "pw1")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.tokenId").exists())
                .andExpect(jsonPath("$.tokenId").isString())
                .andReturn().getResponse().getContentAsString();

        String user2TokenJson = this.loginUser("user2", "pw2")
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.tokenId").exists())
                .andExpect(jsonPath("$.tokenId").isString())
                .andReturn().getResponse().getContentAsString();
        String user1Token = JsonPath.read(user1TokenJson, "$.tokenId");
        String user2Token = JsonPath.read(user2TokenJson, "$.tokenId");
        // then

        assertNotEquals(user1Token, user2Token);
    }

    @Test
    public void adminCanListAllUsers() throws Exception {
        //Given
        String adminToken = new UserBuilder(ctx).createAdminUser().build().getAuthenticationString();
        System.out.println("AdminToken " + adminToken);
        String user1 = this.createUser("user1", "password", adminToken).andReturn().getResponse().getContentAsString();
        System.out.println("user1 created");
        String user2 = this.createUser("user2", "password2", adminToken).andReturn().getResponse().getContentAsString();
        System.out.println("user2 created");
        String user3 = this.createUser("user3", "password3", adminToken).andReturn().getResponse().getContentAsString();
        System.out.println("user3 created");
        String user4 = this.createUser("user4", "password4", adminToken).andReturn().getResponse().getContentAsString();
        System.out.println("user4 created");

        //when


        ResultActions getAllUsersRequest = mockMvc.perform(get("/users").header("Authorization", adminToken));

        //then

        MvcResult getAllUserResult = getAllUsersRequest.andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].userName").exists())
                .andReturn();
        String allUserJson = getAllUserResult.getResponse().getContentAsString();
//        String userIdForSelectedUser = JsonPath.read(allUserJson, "$[0].userName");
//        String json = "{ \"name\" : \"" + userIdForSelectedUser + "\", \"oldPassword\" : \"password\", \"newPassword\" : \"pass\" }";

        //then
//        mockMvc.perform(put("/" + userIdForSelectedUser + "/changePassword")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json))
//                .andExpect(status().isOk());

    }


//    @Test
//    public void LogedInUserWhenChangePasswordShouldReturnHTTPStatusOK() throws Exception {
//        //given
//        String authString = new UserBuilder(ctx).setUsername("user1").setPassword("password").getUser().getUserAuthenticationString();
//        String json = createAUsernamePasswordNewPasswordJson("user1", "password", "newpassword");
//        //when
//        ResultActions passwordUpdateRequest = mockMvc.perform(put("/users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json)
//                .header("Authorization", authString)).andDo(print());
//
//        //then
//        passwordUpdateRequest.andExpect(status().isOk())
//                .andReturn();
//        //mockMvc.perform(get("/groups")).andExpect(status().isForbidden());
//    }
//
//
//    @Test
//    public void LogedInUserWheChangePasswordShouldReloginWithNewPassword() throws Exception {
//        //given
//        String authString = new UserBuilder(ctx).setUsername("username").setPassword("pass").getUser().getUserAuthenticationString();
//        //when
//        String json = createAUsernamePasswordNewPasswordJson("username", "pass", "pass2");
//
//        ResultActions passwordUpdateRequest =
//                mockMvc.perform(put("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                        .andExpect(status().isOk())
//                        .andExpect(jsonPath("$.tokenId").exists())
//                        .andExpect(jsonPath("$.tokenId").isString())
//                        .andExpect(jsonPath("$.user.username").value("username"))
//                        .andExpect(jsonPath("$.user.password").value("pass2"));
//
//        //logout request
//
//        ResultActions relogin = this.loginUser("username", "pass2");
//
//        //then
//        relogin.andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.tokenId").exists())
//                .andExpect(jsonPath("$.tokenId").isString())
//                .andExpect(jsonPath("$.user.username").value("usename"));
//    }
//
//    @Test
//    public void LogedInUserWheChangePasswordShouldNotReloginWithOldPassword() throws Exception {
//        //given
//        String authString = new UserBuilder(ctx).setUsername("username").setPassword("passwd2").getUser().getUserAuthenticationString();
//        //when
//        String json = createAUsernamePasswordNewPasswordJson("user1", "passwd2", "password2");
//        ResultActions passwordUpdateRequest =
//                mockMvc.perform(put("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                        .andExpect(status().isOk());
//
//        //logout request
//
//        ResultActions relogin = this.loginUser("username", "passwd2");
//
//        //then
//
//        relogin.andExpect(status().is4xxClientError()).andReturn();
//    }


}
