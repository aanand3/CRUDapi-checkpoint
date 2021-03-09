package com.example.demo;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests
{
    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository repo;

    @Test
    @Transactional
    @Rollback
    void getTheWholeTable() throws Exception
    {
        MockHttpServletRequestBuilder request = get("/users")
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$[0].id", is(instanceOf(Number.class))));
    }

    @Test
    @Transactional
    @Rollback
    void postToTheTable() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();

        User myUser = new User();
        myUser.setEmail("hello there");
        myUser.setPassword("whats up man");

        User myUser1 = new User();
        myUser.setEmail("howdy there");
        myUser.setPassword("hey man");

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(myUser));

        this.mvc.perform(postRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(1)));

        getTheWholeTable();
    }

    @Test
    @Transactional
    @Rollback
    void getSpecificEntry() throws Exception
    {
        User myUser = new User();
        myUser.setEmail("hello there");
        myUser.setPassword("whats up man");
        repo.save(myUser);

        MockHttpServletRequestBuilder requestFirstElement = get("/users/1")
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(requestFirstElement)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.email", is(myUser.getEmail())));
    }

    @Test
    @Transactional
    @Rollback
    void postToTheTableAndThenPatchIt() throws Exception
    {
        User myUser = new User();
        myUser.setEmail("hello there");
        myUser.setPassword("whats up man");

        User myUser1 = new User();
        myUser1.setEmail("howdy there");
        myUser1.setPassword("hey man");

        repo.save(myUser); repo.save(myUser1);

        Map<String, Object> patches = new HashMap<>();
        patches.put("email", "working");

        ObjectMapper objectMapper = new ObjectMapper();

        MockHttpServletRequestBuilder patchRequest = patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patches));

        this.mvc.perform(patchRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.email", is("working")));

        getTheWholeTable();
    }


}
