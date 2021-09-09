package com.plantation.backend.controller;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(WebController.class)
@ActiveProfiles("test")
class WebControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testIndexMethodShouldReturnOk() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk()).andExpect(content().string("index"));
    }

    @Test
    public void testGetPlantReturns404WhenThereIsNoPlantWithGivenId() throws Exception {
        String id = RandomString.make(16);
        HttpUriRequest request = new HttpGet("http://localhost:8072/plant/" + id);

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        Assert.assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));
    }

}