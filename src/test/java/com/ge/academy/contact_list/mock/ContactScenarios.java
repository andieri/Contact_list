package com.ge.academy.contact_list.mock;

import com.ge.academy.contact_list.TestingApplication;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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


    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

    }


    //good scenarios

    @Test
    public void createAndDeleteContact() throws Exception {
        String userid = "1";
        String usersession = "1";
        String group = "group";
        String auth = "Bearer aabc";
        String contactid = "myid";
        // List contacts

        mvc.perform(get("/groups/1/contacts"))
//                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(jsonPath("$[*].id", Matchers.not(contactid)));


        // Create contact in group
        MvcResult result = mvc.perform(post("/groups/1/contacts").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"" + contactid + "\",\"firstName\":\"myname\"}"))
                .andExpect(status().isCreated())
//                .andDo(print())
                .andReturn();

        String headerValue = result.getResponse().getHeader("Location");

        // Details

        result = mvc.perform(get(headerValue))
                .andExpect(status().isOk())
 //               .andDo(print())
                .andExpect(jsonPath("$.id", Matchers.is(contactid)))
                .andReturn();
/*
        // Is contact in group?
        mvc.perform(get("/groups/" + group + "/contats"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id", Matchers.is("myid")));
        ;

        // Details
        mvc.perform(get("/groups/" + group + "/contacts/" + contactid)).andExpect(status().isOk());

        // Delete the contact
        mvc.perform(delete("/groups/" + group + "/contacts/" + contactid)).andExpect(status().isOk());

        // Is contact in group?(it shouldn't be)
        mvc.perform(get("/groups/" + group + "/")).andExpect(status().isOk());

        // Details(it should fail)
        mvc.perform(get("/groups/" + group + "/contacts/" + contactid)).andExpect(status().isNotFound());
  */
    }


}