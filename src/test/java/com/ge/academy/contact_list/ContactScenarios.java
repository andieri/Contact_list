package com.ge.academy.contact_list;

import com.ge.academy.contact_list.utils.Contact;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

        UserA = new UserBuilder(context).createUser().build();

        groupA = ContactGroup.creator()
                .authHeader(UserA.getAuthenticationString())
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
        System.out.println(UserA.getAuthenticationString());
        System.out.println(groupA.getName());
        addContact(UserA.getAuthenticationString(), groupA.getName(), "firstName", "notmyname");

        String ID =
                addContact(UserA.getAuthenticationString(), groupA.getName(), contactid, "myname");

        // Details
        MvcResult result = mvc.perform(get("/groups/" + groupA.getName() + "/contacts/" + ID)
                .header("Authorization", UserA.getAuthenticationString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", Matchers.is("myname")))
                .andReturn();

        System.out.println(JsonPath.read(result.getResponse().getContentAsString(), "$").toString());

        // Is contact in group?
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
//                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(1)));

        // Delete the contact
        mvc.perform(delete("/groups/" + groupA.getName() + "/contacts/" + ID)
                .header("Authorization", UserA.getAuthenticationString()))
                .andDo(print())
                .andExpect(status().isOk());

        // Details(it should fail)
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts/" + ID)
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isNotFound());

        // Group shouldn't contain it
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(0)));

    }

    @Test
    public void createAndModify() throws Exception {


        String contactid = "myid2";

        // Add two contacts

        addContact(UserA.getAuthenticationString(), groupA.getName(), "notmyid", "notmyname");

        String headerValue =
                addContact(UserA.getAuthenticationString(), groupA.getName(), "myid", "myname");


        // Details

        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(contactid)))
                .andExpect(jsonPath("$.firstName", Matchers.is("myname")))
                .andReturn();

        // Is contact in group?
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.id == '" + contactid + "')]", hasSize(1)));

        // Modify the contact
        mvc.perform(put("/groups/" + groupA.getName() + "/contacts/" + contactid)
                .header("Authorization", UserA.getAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"" + contactid + "\",\"firstName\":\"mynewname\"}"))
                .andExpect(status().isOk());

        // Details

        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", Matchers.is("mynewname")))
                .andReturn();

    }

    @Test
    public void search() throws Exception {


        addContact(UserA.getAuthenticationString(), groupA.getName(), "notmyid", "notmyname");

        addContact(UserA.getAuthenticationString(), groupA.getName(), "myid", "myname");

        addContact(UserA.getAuthenticationString(), groupA.getName(), "abcabc", "sanyi");


        // Is contact in group?
        // No one has any idea about the interface so this is just a plan:
        // can you find two "myname"?
        mvc.perform(post("/groups/search")
                .header("Authorization", UserA.getAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"myname\"}"))
                .andExpect(status().isOk());

        // can you find "notmyname"?
        mvc.perform(post("/groups/search")
                .header("Authorization", UserA.getAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"notmyname\"}"))
                .andExpect(status().isOk());

        // can you find "bc" by ID?
        mvc.perform(post("/groups/search")
                .header("Authorization", UserA.getAuthenticationString())
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
            String user = "erzsi";//new UserBuilder(context).getUser().getAuthenticationString();
            users.add(user);

            addContact(user, groupid1, "myid", "myname");
            for (int d = 0; d < i; d++) {
                addContact(user, groupid2, "myid" + i + d, "name" + i);
            }

        }
        int d = 0;
        for (String user : users) {
            addContact(user, groupid2, "new" + d,"name" + d);
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


    private String addContact(String user, String group, String id, String firstName) throws Exception {
        Contact contact = Contact.builder()
                .authHeader(user)
                .groupName(group)
                .firstName(firstName)
                .lastName("lastName")
                .homeEmail("home@email.email")
                .workEmail("work@email.email")
                .nickName("nickName")
                .jobTitle("jobTitle")
                .webApplicationContext(context)
                .id(id)
                .create();

        return contact.getId();

    }
}