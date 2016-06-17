package com.ge.academy.contact_list;

import com.ge.academy.contact_list.utils.UserBuilder;
import com.ge.academy.contact_list.utils.UserJsonCreator;
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


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by 212566304 on 6/14/2016.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ContactListApplication.class)
@WebAppConfiguration
public class SimpleUserFunctionTest {

    @Autowired
    private WebApplicationContext ctx;
    private MockMvc mockMvc;
    private UserJsonCreator creator;
    private String userToken1;

    @Before
    public void setup() throws Exception{
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        this.userToken1 = new UserBuilder(ctx).setUsername("cheatUser").setPassword("cheater").createUser().build().getAuthenticationString();
        this.creator = new UserJsonCreator();
    }
    private ResultActions loginUser(String username, String password) throws Exception {
        String json = creator.getJsonForLogin(username, password);
        return mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .param("password", password));
    }

    @Test
    public void logedInUserWhenChangePasswordShouldReturnHTTPStatusOK() throws Exception {
        //given
        String json = creator.getJSonForChangePassword("password", "newpassword");
        //when
        ResultActions passwordUpdateRequest = mockMvc.perform(put("/users/changepassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", userToken1));

        //then
        passwordUpdateRequest.andExpect(status().isOk())
                .andReturn();
        //mockMvc.perform(get("/groups")).andExpect(status().isForbidden());
    }


    @Test
    public void LogedInUserWheChangePasswordShouldReloginWithNewPassword() throws Exception {
        //given
        //when
        String json = creator.getJSonForChangePassword("pass", "pass2");

        ResultActions passwordUpdateRequest =
                mockMvc.perform(put("/users/changepassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", userToken1))
                        .andExpect(status().isOk());

        //logout request

        ResultActions relogin = this.loginUser("username", "pass2");

        //then
        relogin.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenId").exists())
                .andExpect(jsonPath("$.tokenId").isString());
    }

    @Test
    public void LogedInUserWhenChangePasswordShouldNotReloginWithOldPassword() throws Exception {
        //given
        String json = creator.getJSonForChangePassword("passwd2", "password2");
        ResultActions passwordUpdateRequest =
                mockMvc.perform(put("/users/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", userToken1))
                        .andExpect(status().isOk());

        //when

        ResultActions relogin = this.loginUser("username", "passwd2");

        //then

        relogin.andExpect(status().is4xxClientError()).andReturn();
    }



    @Test
    public void UserShouldNotCreateUser() throws Exception {
        //Given
        String json = creator.getJSonForCreateUser("proba", "probapw");
        System.out.println("Felhasználó bearer string: "+ this.userToken1);
        //when
        ResultActions createUserByUserAction = mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", this.userToken1));
//                .andDo(print());

        //then
//        System.out.println(createUserByUserAction.andReturn().getResponse().getContentAsString());
        MvcResult createUserByUserResult = createUserByUserAction.andExpect(status().isForbidden()).andReturn();
//        String responseJson = createUserByUserResult.getResponse().getContentAsString();
//        System.out.println(responseJson);
//        JsonPath.re
//        System.out.println("status "+createUserByUserAction.andReturn().getResponse().getStatus());
//        assertEquals(createUserByUserAction.andReturn().getResponse().getStatus(), 403);

    }





}
