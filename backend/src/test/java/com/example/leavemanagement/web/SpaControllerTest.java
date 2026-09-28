package com.example.leavemanagement.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(SpaController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void forwardsRootToSpaShell() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"));
    }

    @Test
    void forwardsIndexToSpaShell() throws Exception {
        mockMvc.perform(get("/index"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"));
    }
}