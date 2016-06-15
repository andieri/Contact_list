package com.ge.academy.contact_list.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ge.academy.contact_list.entity.Token;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by 212566304 on 6/13/2016.
 */

public class UserBuilder {

    private MockMvc mockMvc;

    private static long counter = 1L;

    private String adminToken = null;
    private String userToken = null;
    private String username = null;
    private String password = null;


    public UserBuilder(WebApplicationContext ctx) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    private MockHttpServletResponse createUser(String admin, String newUsername, String password) throws Exception {
        return mockMvc.perform(post("/users/create")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", newUsername)
                .param("password", password)).andDo(print())
                .andReturn().getResponse();
    }

    private MockHttpServletResponse userLogin(String username, String password) throws Exception {

        String json = "{ \"username\" : "+'"'+ username +"\", \"password\" : \""+ password +"\" }";
        System.out.println(json);
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn().getResponse();
    }

    public UserBuilder getAdminUser() throws Exception {
        String adminJson = this.userLogin("Admin", "Alma1234").getContentAsString();
        this.adminToken = JsonPath.read(adminJson, "$.tokenId");
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
        userResponse = this.userLogin(username, password);
        this.userToken =  JsonPath.read(userResponse, "$.tokenId");;
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

    public String getAdminToken() {
        return adminToken;
    }

    public String getUserToken() {
        return userToken;
    }

    public String getUserAuthenticationString() {
        return "Bearer " + userToken;
    }

    public String getAdminUserAuthenticationString() {
        return "Bearer " + adminToken;
    }
}
