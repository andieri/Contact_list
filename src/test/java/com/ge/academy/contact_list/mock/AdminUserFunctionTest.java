package com.ge.academy.contact_list.mock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ge.academy.contact_list.entity.Token;
import com.ge.academy.contact_list.utils.UserBuilder;
import com.jayway.jsonpath.JsonPath;
import com.owlike.genson.Genson;
import net.minidev.json.JSONObject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ge.academy.contact_list.ContactListApplication;

import static org.hamcrest.Matchers.*;
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

    private String createAUsernamePasswordJson(String username, String password) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userName", username);
        node.put("password", password);
        node.put("role", "USER");
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    private String createAUsernamePasswordNewPasswordJson(String username, String password, String newPassword) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("username", username);
        node.put("oldPassword", password);
        node.put("newPassword", newPassword);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
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
        String json = createAUsernamePasswordJson(username, password);
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .param("password", password)).andDo(print());
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
        String json = createAUsernamePasswordJson(username, password);
        return mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + adminToken));
    }

    /**
     * @throws Exception
     */
    @Test
    public void createUserShouldReturnHTTPStatusOK() throws Exception {
        //Given
        //when
        String adminJson = this.loginUser("Admin", "Alma1234")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        String token = JsonPath.read(adminJson, "$.tokenId");
        //then
        this.createUser("user1", "pw1", token)
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void createdUserWhenLoginShouldReturnHTTPStatusOK() throws Exception {
        //Given
        //when
        String adminJson = this.loginUser("Admin", "Alma1234")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(adminJson, "$.tokenId");

        this.createUser("user1", "pw1", token)
                .andReturn().getResponse().getContentAsString();
        //then

        String userJson = this.loginUser("user1", "pw1")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void createdUserWhenLoginShouldReturnAnauthorizationToken() throws Exception {
        //Given
        //when
        String adminJson = this.loginUser("Admin", "Alma1234")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(adminJson, "$.tokenId");

        this.createUser("user1", "pw1", token)
                .andReturn().getResponse().getContentAsString();
        //then

        String userJson = this.loginUser("user1", "pw1")
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenId").exists())
                .andExpect(jsonPath("$.tokenId").isString())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void AdminChangeASelectedUserPasswordShouldReturnHTTPSatusOK() throws Exception{
        //Given
        String adminToken = new UserBuilder(ctx).getAdminUser().getAdminUserAuthenticationString();
        System.out.println(adminToken);
        String user1 = this.createUser("user1", "password", adminToken).andReturn().getResponse().getContentAsString();
        String user2 = this.createUser("user2", "password2", adminToken).andReturn().getResponse().getContentAsString();
        String user3 = this.createUser("user3", "password3", adminToken).andReturn().getResponse().getContentAsString();
        String user4 = this.createUser("user4", "password4", adminToken).andReturn().getResponse().getContentAsString();

        String allUserJson = mockMvc.perform(get("/users").header("Authorization", adminToken))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].username").exists())
                .andReturn().getResponse().getContentAsString();

        String userIdFor= "";

    }


    @Test
    public void LogedInUserWhenChangePasswordShouldReturnHTTPStatusOK() throws Exception {
        //given
        String authString = new UserBuilder(ctx).setUsername("user1").setPassword("password").getUser().getUserAuthenticationString();
        String json = createAUsernamePasswordNewPasswordJson("user1", "password", "newpassword");
        //when
        ResultActions passwordUpdateRequest = mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", authString)).andDo(print());

        //then
        passwordUpdateRequest.andExpect(status().isOk())
                .andReturn();
        //mockMvc.perform(get("/groups")).andExpect(status().isForbidden());
    }


    @Test
    public void LogedInUserWheChangePasswordShouldReloginWithNewPassword() throws Exception {
        //given
        String authString = new UserBuilder(ctx).setUsername("username").setPassword("pass").getUser().getUserAuthenticationString();
        //when
        String json = createAUsernamePasswordNewPasswordJson("username", "pass", "pass2");

        ResultActions passwordUpdateRequest =
                mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.tokenId").exists())
                        .andExpect(jsonPath("$.tokenId").isString())
                        .andExpect(jsonPath("$.user.username").value("username"))
                        .andExpect(jsonPath("$.user.password").value("pass2"));

        //logout request

        ResultActions relogin = this.loginUser("username", "pass2");

        //then
        relogin.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenId").exists())
                .andExpect(jsonPath("$.tokenId").isString())
                .andExpect(jsonPath("$.user.username").value("usename"));
    }

    @Test
    public void LogedInUserWheChangePasswordShouldNotReloginWithOldPassword() throws Exception {
        //given
        String authString = new UserBuilder(ctx).setUsername("username").setPassword("passwd2").getUser().getUserAuthenticationString();
        //when
        String json = createAUsernamePasswordNewPasswordJson("user1", "passwd2", "password2");
        ResultActions passwordUpdateRequest =
                mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isOk());

        //logout request

        ResultActions relogin = this.loginUser("username", "passwd2");

        //then

        relogin.andExpect(status().is4xxClientError()).andReturn();
    }


}
