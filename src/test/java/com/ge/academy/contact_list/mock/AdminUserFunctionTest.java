package com.ge.academy.contact_list.mock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.academy.contact_list.TestingApplication;
import com.ge.academy.contact_list.mock.Token;
import com.ge.academy.contact_list.utils.UserBuilder;
import com.jayway.jsonpath.JsonPath;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by 212566304 on 6/13/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestingApplication.class)
@WebAppConfiguration
public class AdminUserFunctionTest {
    @Autowired
    private WebApplicationContext ctx;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    /**
     * It is a helper function to create a login for a user. It given a username and password.
     * It returns a ResultActions object which can tested.
     * @param username  the username of a user
     * @param password  the password of a user
     * @return
     * @throws Exception
     */

    private ResultActions loginUser(String username, String password) throws Exception {
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("password", password)).andDo(print());
    }

    /**
     * Helper function for admin user to create a new user with the given username and password.
     * It returns a ResultActions object.
     *
     *
     * @param username  given username for the user
     * @param password  given password for the user
     * @param adminToken  given token of the logged in admin user
     * @return
     * @throws Exception
     */

    private ResultActions createUser(String username, String password, String adminToken) throws Exception {
        return mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("password", password)
                .header("Authorization", "Baerer " + adminToken));
    }

    /**
     *
     *
     * @throws Exception
     */
    @Test
    public void createUserShouldReturnHTTPStatusOK() throws Exception {
        //Given
        //when
        String adminJson = this.loginUser("root", "Almafa123")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        String token = objectMapper.readValue(adminJson, Token.class).getTokenID();
        //then
        this.createUser("user1", "pw1", token)
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void createdUserWhenLoginShouldReturnHTTPStatusOK() throws Exception {
        //Given
        //when
        String adminJson = this.loginUser("root", "Almafa123")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper().readValue(adminJson, Token.class).getTokenID();

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
        String adminJson = this.loginUser("root", "Almafa123")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = new ObjectMapper().readValue(adminJson, Token.class).getTokenID();

        this.createUser("user1", "pw1", token)
                .andReturn().getResponse().getContentAsString();
        //then

        String userJson = this.loginUser("user1", "pw1")
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenID").exists())
                .andExpect(jsonPath("$.tokenID").isString())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void LogedInUserWhenChangePasswordShouldReturnHTTPStatusOK() throws Exception {
        //given
        String authString = new UserBuilder(ctx).setUsername("user1").setPassword("password").getUser().getUserAuthenticationString();
        //when
        ResultActions passwordUpdateRequest = mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user1")
                .param("password", "password")
                .param("newpassword", "newpassword")
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

        ResultActions passwordUpdateRequest =
                mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "username")
                        .param("password", "pass")
                        .param("newpassword", "pass2"))
                        .andExpect(status().isOk());

        //logout request

        ResultActions relogin = this.loginUser("username", "pass2");

        //then

        relogin.andExpect(status().isOk()).andReturn();
    }

    @Test
    public void LogedInUserWheChangePasswordShouldNotReloginWithOldPassword() throws Exception {
        //given
        String authString = new UserBuilder(ctx).setUsername("username").setPassword("pass").getUser().getUserAuthenticationString();
        //when

        ResultActions passwordUpdateRequest =
                mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "username")
                        .param("password", "pass")
                        .param("newpassword", "pass2"))
                        .andExpect(status().isOk());

        //logout request

        ResultActions relogin = this.loginUser("username", "pass1");

        //then

        relogin.andExpect(status().is4xxClientError()).andReturn();
    }


}
