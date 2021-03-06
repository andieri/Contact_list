package com.ge.academy.contact_list.utils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by 212566301 on 6/13/2016.
 */
public class GroupIdModifier {

    /*
            fluent api builder pattern for groups

            Example usage (all parameters are necessary):
            String groupId = GroupIdBuilder.builder()
                            .authHeader(authHeader)
                            .name("name")
                            .displayName("displayName")
                            .webApplicationContext(context)
                            .create();

            TBD: unit tests
     */

    public GroupIdModifier() {
    }

    private String authHeader;
    private String name;
    private String displayName;
    private String groupId;
    private WebApplicationContext webApplicationContext;

    public static Builder builder() {
        return new GroupIdModifier.Builder();
    }

    public static class Builder {

        private GroupIdModifier instance = new GroupIdModifier();
        private MockMvc mvc;

        public Builder() {
        }

        public Builder authHeader(String authHeader){
            instance.authHeader = authHeader;
            return this;
        }

        public Builder groupId(String groupId){
            instance.groupId = groupId;
            return this;
        }

        public Builder name(String name){
            instance.name = name;
            return this;
        }

        public Builder displayName(String displayName){
            instance.displayName = displayName;
            return this;
        }

        public Builder webApplicationContext(WebApplicationContext webApplicationContext){
            instance.webApplicationContext = webApplicationContext;
            return this;
        }

        public String build() throws Exception {
            this.mvc = MockMvcBuilders.webAppContextSetup(instance.webApplicationContext).build();
            mvc.perform(put("/groups/" + instance.groupId).header("Authorization", instance.authHeader)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content("{\"name\": \"" + instance.name + "\", \"displayName\": \"" + instance.displayName + "\"}"))
                    .andDo(print())
                    .andExpect(status().isOk());
            return instance.name;
        }
    }
}
