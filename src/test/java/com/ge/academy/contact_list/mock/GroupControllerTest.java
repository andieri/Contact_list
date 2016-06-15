package com.ge.academy.contact_list.mock;

import com.ge.academy.contact_list.ContactListApplication;
import com.ge.academy.contact_list.utils.ContactGroup;
import com.ge.academy.contact_list.utils.Contact;
import com.ge.academy.contact_list.utils.GroupIdModifier;
import com.ge.academy.contact_list.utils.UserBuilder;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by 212565332 on 6/13/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ContactListApplication.class)
@WebAppConfiguration
public class GroupControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() throws Exception {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    public void getAllGroupsShouldReturnEmptyJsonWhenNoGroupsArePresent() throws Exception {
        // Given
        String authHeader = new UserBuilder(context).getUser().getUserAuthenticationString();

        // When
        mvc.perform(get("/groups").header("Authorization", authHeader))
                .andDo(print())

                // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void createGroupAndCheckIfCreated() throws Exception {
        // Given
        String authHeader = new UserBuilder(context).getUser().getUserAuthenticationString();


        // When
        ContactGroup contactGroup = ContactGroup.creator()
                .authHeader(authHeader)
                .name("name")
                .displayName("displayName")
                .webApplicationContext(context)
                .create();

        String groupName = contactGroup.getName();

        // Then
        mvc.perform(get("/groups").header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        // Then 2
        mvc.perform(get("/groups/" + groupName).header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(groupName)))
                .andExpect(jsonPath("$.displayName", is("displayName")));

    }

    @Test
    public void renameGroupShouldReturnRenamedGroup() throws Exception {

        // Given
        String authHeader = new UserBuilder(context).getUser().getUserAuthenticationString();

        ContactGroup contactGroup = ContactGroup.creator()
                .authHeader(authHeader)
                .name("name")
                .displayName("displayName")
                .webApplicationContext(context)
                .create();

        String groupName = contactGroup.getName();

        mvc.perform(get("/groups").header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // When
        String modifiedGroupId = GroupIdModifier.builder()
                .authHeader(authHeader)
                .groupId(groupName)
                .name("name2")
                .displayName("displayName2")
                .webApplicationContext(context)
                .build();

        // Then
        mvc.perform(get("/groups").header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name", is("name2")))
                .andExpect(jsonPath("$[0].displayName", is("displayName2")));

        // Then 2
        mvc.perform(get("/groups/" + modifiedGroupId).header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("name2")))
                .andExpect(jsonPath("$.displayName", is("displayName2")));
    }

    @Test
    public void createNewContactShouldAddContactToGroup() throws Exception {
        // Given
        String authHeader = new UserBuilder(context).getUser().getUserAuthenticationString();


        ContactGroup contactGroup = ContactGroup.creator()
                .authHeader(authHeader)
                .name("name")
                .displayName("displayName")
                .webApplicationContext(context)
                .create();

        String groupName = contactGroup.getName();

        // When
        Contact contact = Contact.builder()
                .authHeader(authHeader)
                .groupId(groupName) // !!! rename
                .id("id")
                .firstName("firstName")
                .lastName("lastName")
                .homeEmail("home@email.email")
                .workEmail("work@email.email")
                .nickName("nickName")
                .jobTitle("jobTitle")
                .webApplicationContext(context)
                .create();

        String contactId = contact.getId();

        // Then
        mvc.perform(get("/groups/" + groupName + "/contacts/" + contactId).header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void differentUsersShouldNotAccessEachOthersGroups() throws Exception {

        // Given
        String authHeader1 = new UserBuilder(context).getUser().getUserAuthenticationString();
        String authHeader2 = new UserBuilder(context).getUser().getUserAuthenticationString();

        // When
        ContactGroup contactGroup1 = ContactGroup.creator()
                .authHeader(authHeader1)
                .name("name")
                .displayName("displayName")
                .webApplicationContext(context)
                .create();

        ContactGroup contactGroup2 = ContactGroup.creator()
                .authHeader(authHeader2)
                .name("name")
                .displayName("displayName")
                .webApplicationContext(context)
                .create();

        String groupName1 = contactGroup1.getName();
        String groupName2 = contactGroup2.getName();

        // Then
        mvc.perform(get("/groups").header("Authorization", authHeader1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/groups").header("Authorization", authHeader2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/groups/" + groupName1).header("Authorization", authHeader1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name", is(groupName1)))
                .andExpect(jsonPath("$[0].displayName", is("displayName")));

        mvc.perform(get("/groups/" + groupName2).header("Authorization", authHeader2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name", is(groupName2)))
                .andExpect(jsonPath("$[0].displayName", is("displayName")));

        mvc.perform(get("/groups/" + groupName1).header("Authorization", authHeader2))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(get("/groups/" + groupName2).header("Authorization", authHeader1))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}