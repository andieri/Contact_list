package com.ge.academy.contact_list.mock;

import com.ge.academy.contact_list.TestingApplication;
import com.ge.academy.contact_list.utils.User;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by 212565332 on 6/13/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestingApplication.class)
@WebAppConfiguration//Scenarios for controller: 6, 7, 8

public class ContactScenarios {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private ContactController contacts;

    @Before
    public void setup() {
        contacts.reset();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

    }


    //good scenarios

    @Test
    public void createAndDeleteContact() throws Exception {
        User UserA = new User();
        String group = "group";

        String contactid = "myid2";

        // Create two contacts in group
        mvc.perform(post("/groups/1/contacts")
                .header("Authorization", UserA.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        // Create two contacts in group
        MvcResult result = mvc.perform(post("/groups/1/contacts")
                .header("Authorization", UserA.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"" + contactid + "\",\"firstName\":\"myname\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String headerValue = result.getResponse().getHeader("Location");

        // Details

        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getAuthString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(contactid)))
                .andReturn();

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
        User UserA = new User();
        String group = "group";

        String contactid = "myid2";

        // Create two contacts in group
        mvc.perform(post("/groups/1/contacts")
                .header("Authorization", UserA.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"notmyid\",\"firstName\":\"notmyname\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        // Create two contacts in group
        MvcResult result = mvc.perform(post("/groups/1/contacts")
                .header("Authorization", UserA.getAuthString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"" + contactid + "\",\"firstName\":\"myname\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String headerValue = result.getResponse().getHeader("Location");

        // Details

        mvc.perform(get(headerValue)
                .header("Authorization", UserA.getAuthString()))
                .andDo(print())
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


}