package com.ge.academy.contact_list.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by 212566304 on 6/13/2016.
 */

public class UserBuilder {

    private MockMvc mockMvc;
    private WebApplicationContext ctx;
    private static long counter = 1L;

    private String userToken = null;
    private User user;


    public UserBuilder(WebApplicationContext ctx) {
        this.ctx = ctx;
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        this.user = new User(null, null);
    }

    private String createUserJson(String username, String password, String role) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userName", username);
        node.put("password", password);
        node.put("role", role);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node).toString();
    }

    private MockHttpServletResponse createUser(String adminToken) throws Exception {
        String adminUserJson = createUserJson(this.user.getUsername(), this.user.getPassword(), this.user.getRole());
        return mockMvc.perform(post("/users/create")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminUserJson))
                .andReturn().getResponse();
    }

    private MockHttpServletResponse userLogin() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userName", this.user.getUsername());
        node.put("password", this.user.getPassword());

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn().getResponse();
    }

    public UserBuilder createAdminUser() throws Exception {
        this.user = new AdminUser("Admin", "Alma1234");
        return this;
    }

    public UserBuilder build() throws Exception {
        if (this.user instanceof AdminUser) {
            String adminJson = this.userLogin().getContentAsString();
            this.userToken = JsonPath.read(adminJson, "$.tokenId");
            return this;
        }
        if (this.user instanceof User) {
            MockHttpServletResponse userResponse = userLogin();
            this.userToken = JsonPath.read(userResponse.getContentAsString(), "$.tokenId");
            return this;
        }
        return null;
    }

    public UserBuilder createUser() throws Exception {
        String adminToken = new UserBuilder(ctx).createAdminUser().build().getAuthenticationString();

        if (this.user.getUsername() == null & this.user.getPassword() == null) {
            do {
                this.user.setUsername("user" + UserBuilder.counter);
                this.user.setPassword("passwd" + UserBuilder.counter);
                UserBuilder.counter++;
            } while (this.createUser(adminToken).getStatus() != 201);
        } else {
            this.createUser(adminToken);
        }
        return this;
    }

    public UserBuilder setUsername(String username) {
        this.user.setUsername(username);
        return this;
    }

    public UserBuilder setPassword(String password) {
        this.user.setPassword(password);
        return this;
    }

    public String getUsername() {
        return this.user.getUsername();
    }

    public String getPassword(){
        return this.user.getPassword();
    }

    public String getRole(){
        return this.user.getRole();
    }

    public String getAuthenticationString() {
        return "Bearer " + userToken;
    }
}
