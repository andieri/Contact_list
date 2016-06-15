package com.ge.academy.contact_list;

import com.ge.academy.contact_list.entity.User;
import com.ge.academy.contact_list.utils.ContactGroup;
import com.ge.academy.contact_list.utils.UserBuilder;
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
@SpringApplicationConfiguration(classes = ContactListApplication.class)
@WebAppConfiguration//Scenarios for controller: 6, 7, 8

public class ContactScenarios {
    UserBuilder UserA;
    ContactGroup groupA;
    
    @Autowired
    private WebApplicationContext context;
    private MockMvc mvc;


    @Before
    public void setup() throws Exception {

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        UserA = new UserBuilder(context).getUser();

        ContactGroup contactGroup = ContactGroup.creator()
                .authHeader(UserA.getUserAuthenticationString())
                .userName(UserA.getUsername())
                .name("name")
                .displayName("displayName")
                .webApplicationContext(context)
                .create();


    }


    //good scenarios

    @Test
    public void createAndDeleteContact() throws Exception {

        String contactid = "myid2";

        addContact(UserA.getUserAuthenticationString(), groupA.getName(), "{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}");

        String headerValue =
                addContact(UserA.getUserAuthenticationString(), groupA.getName(), "{\"id\":\"" + contactid + "\",\"firstName\":\"myname\"}");

        // Details
        MvcResult result = mvc.perform(get(headerValue)
                .header("Authorization", UserA.getUserAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(contactid)))
                .andReturn();

        System.out.println(JsonPath.read(result.getResponse().getContentAsString(), "$").toString());

        // Is contact in group?
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getUserAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(1)));

        // Delete the contact
        mvc.perform(delete("/groups/" + groupA.getName() + "/contacts/" + contactid)
                .header("Authorization", UserA.getUserAuthenticationString()))
                .andExpect(status().isOk());

        // Details(it should fail)
        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getUserAuthenticationString()))
                .andExpect(status().isNotFound());

        // Group shouldn't contain it
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getUserAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(0)));

    }

    @Test
    public void createAndModify() throws Exception {


        String contactid = "myid2";

        // Add two contacts

        addContact(UserA.getUserAuthenticationString(), groupA.getName(), "{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}");

        String headerValue =
                addContact(UserA.getUserAuthenticationString(), groupA.getName(), "{\"id\":\"" + contactid + "\",\"firstName\":\"myname\"}");


        // Details

        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getUserAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(contactid)))
                .andExpect(jsonPath("$.firstName", Matchers.is("myname")))
                .andReturn();

        // Is contact in group?
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getUserAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(1)));

        // Modify the contact
        mvc.perform(put("/groups/" + groupA.getName() + "/contacts/" + contactid)
                .header("Authorization", UserA.getUserAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"" + contactid + "\",\"firstName\":\"mynewname\"}"))
                .andExpect(status().isOk());

        // Details

        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getUserAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", Matchers.is("mynewname")))
                .andReturn();

    }

    @Test
    public void search() throws Exception {


        addContact(UserA.getUserAuthenticationString(), groupA.getName(), "{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}");

        addContact(UserA.getUserAuthenticationString(), groupA.getName(), "{\"id\":\"myid\",\"firstName\":\"myname\"}");

        addContact(UserA.getUserAuthenticationString(), groupA.getName(), "{\"id\":\"abcabc\",\"firstName\":\"sanyi\"}");


        // Is contact in group?
        // No one has any idea about the interface so this is just a plan:
        // can you find two "myname"?
        mvc.perform(post("/find")
                .header("Authorization", UserA.getUserAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"myname\"}"))
                .andExpect(status().isOk());

        // can you find "notmyname"?
        mvc.perform(post("/find")
                .header("Authorization", UserA.getUserAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"notmyname\"}"))
                .andExpect(status().isOk());

        // can you find "bc" by ID?
        mvc.perform(post("/find")
                .header("Authorization", UserA.getUserAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"bc\"}"))
                .andExpect(status().isOk());

    }

    @Test
    public void testMultipleGroupsAndUsers() throws Exception {
        List<String> users = new ArrayList<String>();
        String groupid1 = "group1";
        String groupid2 = "group2";
        // Given
        // N users with two groups each. Every first group will have one contact, and every second group will have i * 2 contact
        for (int i = 0; i < 10; i++) {
            String user = "erzsi";//new UserBuilder(context).getUser().getUserAuthenticationString();
            users.add(user);

            addContact(user, groupid1, "{\"id\":\"myid" + i + "\",\"firstName\":\"name" + i + "\"}");
            for (int d = 0; d < i; d++) {
                addContact(user, groupid2, "{\"id\":\"myid" + i + d + "\",\"firstName\":\"name" + i + "\"}");
            }

        }
        int d = 0;
        for (String user : users) {
            addContact(user, groupid2, "{\"id\":\"new" + d + "\",\"firstName\":\"name" + d + "\"}");
            d++;
        }

        d = 0;
        for (String user : users) {
            mvc.perform(get("/groups/" + groupid1 + "/contacts")
                    .header("Authorization", user))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mvc.perform(get("/groups/" + groupid2 + "/contacts")
                    .header("Authorization", user))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(d * 2)));
            d++;
        }
    }


    private String addContact(String user, String group, String ContactJson) throws Exception {
        MvcResult result = mvc.perform(post("/groups/" + group + "/contacts")
                .header("Authorization", user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ContactJson))//"{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}"
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getHeader("Location");

    }
}