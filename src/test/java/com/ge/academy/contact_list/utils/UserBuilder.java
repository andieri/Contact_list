package com.ge.academy.contact_list.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.academy.contact_list.TestingApplication;
import com.ge.academy.contact_list.mock.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by 212566304 on 6/13/2016.
 */

public class UserBuilder {

    private MockMvc mockMvc;

    public UserBuilder(WebApplicationContext ctx) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    private MockHttpServletResponse createUserRequestSendingHelper(Token admin, String newUsername, String password) throws Exception {
        return mockMvc.perform(post("/users")
                .header("Authorization", "Baerer " + admin.getTokenID())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", newUsername)
                .param("password", password))
                .andReturn().getResponse();
    }

    private MockHttpServletResponse userLoginRequestSendingHelper(String username, String password) throws Exception {
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", username)
                .param("password", password)).andReturn().getResponse();
    }

    public Token getAdminUser() {
        return null;
    }

    public Token getUser() throws Exception {
        String adminJson = this.userLoginRequestSendingHelper("root", "password").getContentAsString();
        Token adminToken = new ObjectMapper().readValue(adminJson, Token.class);
        MockHttpServletResponse response = null;
        int randomID = Integer.MIN_VALUE;
        do {
            randomID = (int) Math.floor(Math.random() * 1000.0);
            response = this.createUserRequestSendingHelper(adminToken, "user" + randomID, "passwd" + randomID);
        } while (response.getStatus() == 201);

        MockHttpServletResponse userResponse = this.userLoginRequestSendingHelper("user" + randomID, "passwd" + randomID);
        String userJson = response.getContentAsString();
        return new ObjectMapper().readValue(userJson, Token.class);

    }

}
