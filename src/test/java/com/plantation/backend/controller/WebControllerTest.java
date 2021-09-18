package com.plantation.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plantation.backend.model.Plant;
import com.plantation.backend.repository.PlantRepository;
import com.plantation.backend.service.PlantService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(WebController.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class WebControllerTest {

    private MockMvc mvc;

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private WebController webController;

    private JacksonTester<Plant> jsonPlant;

    @Before
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
        mvc = MockMvcBuilders.standaloneSetup(webController)
                .build();
    }

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

        Assert.assertEquals("HTTP Response code is not 404", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));
    }

/*    @Test
    public void testCreatePlantShouldReturnsOkWhenBodyIsValid() throws Exception {
        String body = objectMapper.writeValueAsString(plantObject);

        mvc.perform(post("/plant")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(body)
        )
                .andExpect(status().isOk());

    }*/



}