package com.ge.academy.contact_list;

import com.ge.academy.contact_list.utils.*;
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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
    private UserJsonCreator creator;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        creator = new UserJsonCreator();
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
        String json = creator.getJsonForLogin(username, password);
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
        String json = creator.getJSonForCreateUser(username, password);
        return mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", adminToken));
    }

    private ResultActions createAdmin(String username, String password, String adminToken) throws Exception {
        String json = creator.getJSonForCreateAdmin(username, password);
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
       loggedInUser
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.tokenId").exists())
                .andExpect(jsonPath("$.tokenId").isString()).andReturn();
    }

//    @Test
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
        String user1 = this.createUser("user1", "password", adminToken).andReturn().getResponse().getContentAsString();
        String user2 = this.createUser("user2", "password2", adminToken).andReturn().getResponse().getContentAsString();
        String user3 = this.createUser("user3", "password3", adminToken).andReturn().getResponse().getContentAsString();
        String user4 = this.createUser("user4", "password4", adminToken).andReturn().getResponse().getContentAsString();

        //when

        ResultActions getAllUsersRequest = mockMvc.perform(get("/users").header("Authorization", adminToken));

        //then

        MvcResult getAllUserResult = getAllUsersRequest.andExpect(status().isOk())
				.andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].userName").exists()).andExpect(jsonPath("$[0].userName").isString())
                .andExpect(jsonPath("$[1].userName").exists()).andExpect(jsonPath("$[1].userName").isString())
                .andExpect(jsonPath("$[2].userName").exists()).andExpect(jsonPath("$[2].userName").isString())
                .andExpect(jsonPath("$[3].userName").exists()).andExpect(jsonPath("$[3].userName").isString())
                .andExpect(jsonPath("$[4].userName").exists()).andExpect(jsonPath("$[4].userName").isString())
                .andExpect(jsonPath("$[0].password").doesNotExist()).andExpect(jsonPath("$[0].password", nullValue()))
                .andExpect(jsonPath("$[1].password").doesNotExist()).andExpect(jsonPath("$[1].password", nullValue()))
                .andExpect(jsonPath("$[2].password").doesNotExist()).andExpect(jsonPath("$[2].password", nullValue()))
                .andExpect(jsonPath("$[3].password").doesNotExist()).andExpect(jsonPath("$[3].password", nullValue()))
                .andExpect(jsonPath("$[4].password").doesNotExist()).andExpect(jsonPath("$[4].password", nullValue()))
                .andExpect(jsonPath("$[0].links").exists()).andExpect(jsonPath("$[1].links").exists())
                .andExpect(jsonPath("$[2].links").exists()).andExpect(jsonPath("$[3].links").exists())
                .andExpect(jsonPath("$[4].links").exists())
                .andReturn();
       // System.out.println(getAllUserResult.getResponse().getContentAsString());
    }


}
