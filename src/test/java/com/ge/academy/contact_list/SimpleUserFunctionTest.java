package com.ge.academy.contact_list;

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

import com.ge.academy.contact_list.entity.Token;
import com.ge.academy.contact_list.ContactListApplication;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    private String userToken1;

    private String createUserJson(String username, String password) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userName", username);
        node.put("password", password);
        node.put("role", "USER");
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node).toString();
    }

    @Before
    public void setup() throws Exception{
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        this.userToken1 = new UserBuilder(ctx).setUsername("cheatUser").setPassword("cheater").createUser().build().getAuthenticationString();
    }

    @Test
    public void UserShouldNotCreateUser() throws Exception {
        //Given
        String json = createUserJson("proba", "probapw");
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
