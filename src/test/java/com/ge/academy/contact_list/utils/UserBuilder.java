package com.ge.academy.contact_list.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.academy.contact_list.mock.Token;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by 212566304 on 6/13/2016.
 */

public class UserBuilder {

    private MockMvc mockMvc;

    private static long counter = 1L;

    private Token adminToken = null;
    private Token userToken = null;
    private String username = null;
    private String password = null;


    public UserBuilder(WebApplicationContext ctx) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    private MockHttpServletResponse createUser(Token admin, String newUsername, String password) throws Exception {
        return mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + admin.getTokenID())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", newUsername)
                .param("password", password))
                .andReturn().getResponse();
    }

    private MockHttpServletResponse userLogin(String username, String password) throws Exception {
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("password", password)).andReturn().getResponse();
    }

    public UserBuilder getAdminUser() throws Exception {
        String adminJson = this.userLogin("root", "Almafa123").getContentAsString();
        this.adminToken = new ObjectMapper().readValue(adminJson, Token.class);
        return this;
    }

    public UserBuilder getUser() throws Exception {
        this.getAdminUser();
        MockHttpServletResponse userResponse = null;
        int randomID = Integer.MIN_VALUE;



        if (this.username == null & this.password == null) {
            do {
                randomID = (int) Math.floor(Math.random() * 1000.0);
                this.username = "user"+UserBuilder.counter;
                this.password = "passwd"+UserBuilder.counter;
                UserBuilder.counter++;
            } while (this.createUser(adminToken, username, password).getStatus() == 201);
        } else {
            this.createUser(adminToken, username, password);
        }
        userResponse = this.userLogin("user" + username, password);
        this.userToken = new ObjectMapper().readValue(userResponse.getContentAsString(), Token.class);
        return this;
    }

    public UserBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public Token getAdminToken() {
        return adminToken;
    }

    public Token getUserToken() {
        return userToken;
    }

    public String getUserAuthenticationString() {
        return "Bearer " + userToken.getTokenID();
    }

    public String getAdminUserAuthenticationString() {
        return "Bearer " + adminToken.getTokenID();
    }
}
