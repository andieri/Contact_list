package com.ge.academy.contact_list.mock;

import com.ge.academy.contact_list.TestingApplication;
import com.ge.academy.contact_list.utils.GroupBuilder;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by 212565332 on 6/13/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestingApplication.class)
@WebAppConfiguration
public class GroupControllerTest {

    @Autowired
    private WebApplicationContext context;

    MockMvc mvc;

    String authHeader = "Bearer authHeader";

    @Before
    public void setup() throws Exception {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        UserBuilder userBuilder = new UserBuilder(context);
        String authHeader = userBuilder.getUser();
    }

    public String createGroupWithAuthHeaderReturnsGroupId(String authHeader, String name, String displayName) throws Exception {
        MvcResult answer = mvc.perform(post("/groups").header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{'name': '" + name + "', 'displayName': '" + displayName + "'}"))
//                .andDo(print())
                .andReturn();

        return answer.getResponse().getHeader("Location");
    }

    public String modifyGroupWithAuthHeaderReturnsGroupId(String groupId, String header, String name, String displayName) throws Exception {
        MvcResult answer = mvc.perform(put("/groups/" + groupId).header("Authorization", header)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{'name': 'test2', 'displayName': 'testtest2'}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        return answer.getResponse().getHeader("Location");
    }

    @Test
    public void getAllGroupsShouldReturnEmptyJsonWhenNoGroupsArePresent() throws Exception {
        // Given
        UserBuilder userBuilder = new UserBuilder(context);
        String authHeader = userBuilder.getUser();

        // When
        mvc.perform(get("/groups").header("Authorization", authHeader))
                .andDo(print())

        // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//                .andExpect(content().json(""));
    }

    @Test
    public void createGroupAndCheckIfCreated() throws Exception {
        // Given
        // userA and userB and their auth headers

        // When
//        GroupBuilder groupBuilder = new GroupBuilder();
//        String groupId = groupBuilder.build(authHeader, "test", "testtest");
//        String groupId = createGroupWithAuthHeaderReturnsGroupId(authHeader, "test", "testtest");

        String groupId = GroupBuilder.builder()
                            .authHeader(authHeader)
                            .name("name")
                            .displayName("displayName")
                            .webApplicationContext(context)
                            .build();

        // Then
        mvc.perform(get("/groups").header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk());
//                .andExpect(content().json("{}"));

        // Then 2
        mvc.perform(get("/groups/" + groupId).header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk());
//                .andExpect(content().json("{}"));

    }

    @Test
    public void renameGroupShouldReturnRenamedGroup() throws Exception {

        // Given
        // user A and user B
        String groupId = createGroupWithAuthHeaderReturnsGroupId(authHeader, "test", "testtest");

        mvc.perform(get("/groups").header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk());
//                .andExpect(content().json("{}"));

        // When
        String modifiedGroupId = modifyGroupWithAuthHeaderReturnsGroupId(groupId, authHeader, "test2", "testtest2");

        // Then
        mvc.perform(get("/groups").header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk());
//                .andExpect(content().json("{'name': 'test2', 'displayName': 'testtest2'}"));

        // Then 2
        mvc.perform(get("/groups/" + "groupId").header("Authorization", authHeader))
                .andDo(print())
                .andExpect(status().isOk());
//                .andExpect(content().json("{}"));
    }

    @Test
    public void createNewContactShouldAddContactToGroup() throws Exception {
        // Given
        // user A and user B
        // String groupId = build(userA, name, fullname);

        // When
        mvc.perform(post("/groups/" + "groupId").header("Authorization", "testUser")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{'id':'test2', 'firstName':'firstname', 'lastName':'lastname'" +
                        "'homeEmail':'m@m.m', 'workEmail':'k@k.k', 'nickName':'nickname'" +
                        "'jobTitle':'jobtitle'}"))
                .andDo(print())
                .andExpect(status().isOk());
//                .andExpect(content().json("{}"));
                // itt megkapjuk a contactId-t

        // Then
        mvc.perform(get("/groups/" + "groupId" + "/contacts/" + "contactId").header("Authorization", "testUser"))
                .andDo(print())
                .andExpect(status().isOk());
//                .andExpect(content().json("{'id':'test2', 'firstName':'firstname', 'lastName':'lastname'" +
//                        "'homeEmail':'m@m.m', 'workEmail':'k@k.k', 'nickName':'nickname'" +
//                        "'jobTitle':'jobtitle'}"));
    }
}