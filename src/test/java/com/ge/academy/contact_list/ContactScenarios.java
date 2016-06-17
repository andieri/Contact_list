package com.ge.academy.contact_list;

import com.ge.academy.contact_list.utils.Contact;
import com.ge.academy.contact_list.utils.ContactGroup;
import com.ge.academy.contact_list.utils.UserBuilder;
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


        System.out.println(UserA.getAuthenticationString());
        System.out.println(groupA.getName());
        Contact c1 = addContact(UserA.getAuthenticationString(), groupA.getName(), "firstName1", "lastName1");

        Contact c2 = addContact(UserA.getAuthenticationString(), groupA.getName(), "firstName2", "lastName2");

        // Details
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts/" + c1.getId())
                .header("Authorization", UserA.getAuthenticationString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", Matchers.is("firstName1")))
                .andReturn();


        // Is contact in group?
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.firstName == 'firstName1')]", hasSize(1)));

        // Delete the contact
        mvc.perform(delete("/groups/" + groupA.getName() + "/contacts/" + c1.getId())
                .header("Authorization", UserA.getAuthenticationString()))
                .andDo(print())
                .andExpect(status().isOk());

        // Details(it should fail)
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts/" + c1.getId())
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isNotFound());

        // Group shouldn't contain it
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.firstName == 'firstName1')]", hasSize(0)));

    }

    @Test
    public void createAndModify() throws Exception {


        Contact c1 = addContact(UserA.getAuthenticationString(), groupA.getName(), "firstName1", "lastName1");

        Contact c2 = addContact(UserA.getAuthenticationString(), groupA.getName(), "firstName2", "lastName2");


        // Details

        mvc.perform(get("/groups/" + groupA.getName() + "/contacts/" + c1.getId())
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", Matchers.is("firstName1")))
                .andReturn();

        // Is contact in group?
        mvc.perform(get("/groups/" + groupA.getName() + "/contacts")
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.firstName == 'firstName1')]", hasSize(1)));

        // Modify the contact
        System.out.println("{\n" +
                "    \"firstName\":\"mynewFirstName\",\n" +
                "    \"lastName\":\"lastName1\",\n" +
                "    \"homeEmail\":\"g\",\n" +
                "    \"workEmail\":\"g\",\n" +
                "    \"nickName\":\"g\",\n" +
                "    \"jobTitle\":\"g\"\n" +
                "}");
        mvc.perform(put("/groups/" + groupA.getName() + "/contacts/" + c1.getId())
                .header("Authorization", UserA.getAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"firstName\":\"mynewFirstName\",\n" +
                        "    \"lastName\":\"lastName1\",\n" +
                        "    \"homeEmail\":\"g\",\n" +
                        "    \"workEmail\":\"g\",\n" +
                        "    \"nickName\":\"g\",\n" +
                        "    \"jobTitle\":\"g\"\n" +
                        "}"))
                .andDo(print())
                .andExpect(status().isOk());

        // Details

        mvc.perform(get("/groups/" + groupA.getName() + "/contacts/" + c1.getId())
                .header("Authorization", UserA.getAuthenticationString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", Matchers.is("mynewFirstName")))
                .andReturn();

    }

    @Test
    public void search() throws Exception {


        addContact(UserA.getAuthenticationString(), groupA.getName(), "abcd" , "efghabc");

        addContact(UserA.getAuthenticationString(), groupA.getName(), "ijkl", "mnopabc");

        addContact(UserA.getAuthenticationString(), groupA.getName(), "1234", "abc");




        // can you find all three "abc"-s?
        mvc.perform(post("/search")
                .header("Authorization", UserA.getAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastName\":\"abc\"}"))
                .andExpect(status().isOk());

        // can you find "notmyname"?
        mvc.perform(post("/search")
                .header("Authorization", UserA.getAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"notmyname\"}"))
                .andExpect(status().isOk());

        // can you find "bc" by ID?
        mvc.perform(post("/search")
                .header("Authorization", UserA.getAuthenticationString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"bc\"}"))
                .andExpect(status().isOk());

    }

    @Test
    public void testMultipleGroupsAndUsers() throws Exception {
        List<UserBuilder> users = new ArrayList<>();
        String groupid1 = "group1";
        String groupid2 = "group2";
        // Given
        // N users with two groups each. Every first group will have one contact, and every second group will have i * 2 contact
        for (int i = 0; i < 10; i++) {
            UserBuilder user = new UserBuilder(context).createUser().build();
            users.add(user);
            ContactGroup.creator()
                    .authHeader(UserA.getAuthenticationString())
                    .userName(UserA.getUsername())
                    .name(groupid1)
                    .displayName("displayName")
                    .webApplicationContext(context)
                    .create();
            ContactGroup.creator()
                    .authHeader(UserA.getAuthenticationString())
                    .userName(UserA.getUsername())
                    .name(groupid2)
                    .displayName("displayName")
                    .webApplicationContext(context)
                    .create();

            addContact(user.getAuthenticationString(), groupid1, "myid", "myname");
            for (int d = 0; d < i; d++) {
                addContact(user.getAuthenticationString(), groupid2, "myid" + i + d, "name" + i);
            }

        }
        int d = 0;
        for (UserBuilder user : users) {
            addContact(user.getAuthenticationString(), groupid2, "new" + d, "name" + d);
            d++;
        }

        d = 0;
        for (UserBuilder user : users) {
            mvc.perform(get("/groups/" + groupid1 + "/contacts")
                    .header("Authorization", user.getAuthenticationString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mvc.perform(get("/groups/" + groupid2 + "/contacts")
                    .header("Authorization", user.getAuthenticationString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(d + 1)));
            d++;
        }
    }


    private Contact addContact(String user, String group, String firstName, String lastName) throws Exception {
        Contact contact = Contact.builder()
                .authHeader(user)
                .groupName(group)
                .firstName(firstName)
                .lastName(lastName)
                .homeEmail("home@email.email")
                .workEmail("work@email.email")
                .nickName("nickName")
                .jobTitle("jobTitle")
                .webApplicationContext(context)
                .create();

        return contact;

    }
}