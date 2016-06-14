package com.ge.academy.contact_list.adminuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.academy.contact_list.TestingApplication;
import com.ge.academy.contact_list.mock.Token;
import com.ge.academy.contact_list.utils.UserBuilder;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
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
public class CreateUserByAdmin {
    @Autowired
    private WebApplicationContext ctx;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    private ResultActions loginUser(String username, String password) throws Exception {
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("password", password));
    }

    private ResultActions createUser(String username, String password, String adminToken) throws Exception {
        return mockMvc.perform(post("/users/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("password", password)
                .header("Authorization", "Baerer " + adminToken));
    }

    /**
     * Adott egy admin user amelynek a felhasználói neve <strong>root</strong>
     * jelszava <strong>Almafa123</strong>. A felhasználóval való belépés után
     * lehet egy új felhasználót visszaadni.
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
        ResultActions passwordUpdateRequest =  mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user1")
                .param("password", "password")
                .param("newpassword", "newpassword")
                .header("Authorization", authString)).andDo(print());

        //then
                passwordUpdateRequest.andExpect(status().isOk())
                .andReturn();
        mockMvc.perform(get("/groups")).andExpect(status().isForbidden());
    }

    public void LogedInUserwheChangePasswordShouldReLoginTheNewPassword() throws Exception{
        //given
        String authString = new UserBuilder(ctx).getUser().getUserAuthenticationString();
        //when

        ResultActions passwordUpdateRequest =
                mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        //then


    }



}
