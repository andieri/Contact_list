package com.ge.academy.contact_list.mock;

import com.ge.academy.contact_list.TestingApplication;
import com.ge.academy.contact_list.utils.User;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by 212565332 on 6/13/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestingApplication.class)
@WebAppConfiguration//Scenarios for controller: 6, 7, 8

public class ContactScenarios {
    User UserA;
    String group;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mvc;
    @Autowired
    private ContactController contacts;

    @Before
    public void setup() throws Exception {
        contacts.reset();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        UserA = new User();

//        group = GroupBuilder.builder()
//                .webApplicationContext(context)
//                .authHeader(UserA.getAuthString())
//                .name("group1")
//                .displayName("GroupName1")
//                .build();
        group = "group1";
    }


    //good scenarios

    @Test
    public void createAndDeleteContact() throws Exception {

        String contactid = "myid2";

        addContact(UserA, group, "{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}");

        String headerValue =
                addContact(UserA, group, "{\"id\":\"" + contactid + "\",\"firstName\":\"myname\"}");

        // Details
        MvcResult result = mvc.perform(get(headerValue)
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(contactid)))
                .andReturn();

        System.out.println(JsonPath.read(result.getResponse().getContentAsString(), "$").toString());

        // Is contact in group?
        mvc.perform(get("/groups/" + group + "/contacts")
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(1)));

        // Delete the contact
        mvc.perform(delete("/groups/" + group + "/contacts/" + contactid)
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isOk());

        // Details(it should fail)
        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isNotFound());

        // Group shouldn't contain it
        mvc.perform(get("/groups/" + group + "/contacts")
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(0)));

    }

    @Test
    public void createAndModify() throws Exception {


        String contactid = "myid2";

        // Add two contacts

        addContact(UserA, group, "{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}");

        String headerValue =
                addContact(UserA, group, "{\"id\":\"" + contactid + "\",\"firstName\":\"myname\"}");


        // Details

        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(contactid)))
                .andExpect(jsonPath("$.firstName", Matchers.is("myname")))
                .andReturn();

        // Is contact in group?
        mvc.perform(get("/groups/" + group + "/contacts")
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(1)));

        // Modify the contact
        mvc.perform(put("/groups/" + group + "/contacts/" + contactid)
                .header("Authorization", UserA.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"" + contactid + "\",\"firstName\":\"mynewname\"}"))
                .andExpect(status().isOk());

        // Details

        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", Matchers.is("mynewname")))
                .andReturn();

    }

    @Test
    public void search() throws Exception {


        addContact(UserA, group, "{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}");

        addContact(UserA, group, "{\"id\":\"myid\",\"firstName\":\"myname\"}");

        addContact(UserA, group, "{\"id\":\"abcabc\",\"firstName\":\"sanyi\"}");


        // Is contact in group?
        // No one has any idea about the interface so this is just a plan:
        // can you find two "myname"?
        mvc.perform(post("/find")
                .header("Authorization", UserA.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"myname\"}"))
                .andExpect(status().isOk());

        // can you find "notmyname"?
        mvc.perform(post("/find")
                .header("Authorization", UserA.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"notmyname\"}"))
                .andExpect(status().isOk());

        // can you find "bc" by ID?
        mvc.perform(post("/find")
                .header("Authorization", UserA.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"bc\"}"))
                .andExpect(status().isOk());

    }

    @Test
    public void testMultipleGroupsAndUsers() throws Exception {
        List<User> users = new ArrayList<User>();
        String groupid1 = "group1";
        String groupid2 = "group2";
        // Given
        // N users with two groups each. Every first group will have one contact, and every second group will have i * 2 contact
        for (int i = 0; i < 10; i++) {
            User user = new User();
            users.add(user);

            addContact(user, groupid1, "{\"id\":\"myid" + i + "\",\"firstName\":\"name" + i + "\"}");
            for (int d = 0; d < i; d++) {
                addContact(user, groupid2, "{\"id\":\"myid" + i + d + "\",\"firstName\":\"name" + i + "\"}");
            }

        }
        int d = 0;
        for (User user : users) {
            addContact(user, groupid2, "{\"id\":\"new" + d + "\",\"firstName\":\"name" + d + "\"}");
            d++;
        }

        d = 0;
        for (User user : users) {
            mvc.perform(get("/groups/" + groupid1 + "/contacts")
                    .header("Authorization", user.getAuthString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mvc.perform(get("/groups/" + groupid2 + "/contacts")
                    .header("Authorization", user.getAuthString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(d * 2)));
            d++;
        }
    }


    private String addContact(User user, String group, String ContactJson) throws Exception {
        MvcResult result = mvc.perform(post("/groups/" + group + "/contacts")
                .header("Authorization", user.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ContactJson))//"{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}"
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getHeader("Location");

    }
}