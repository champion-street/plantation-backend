package com.plantation.backend.service;

import com.plantation.backend.model.Plant;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class JSONServiceTest {

    Plant plant = Plant.builder().name("prettyLongNameToBeACoincidence").build();

    @Test
    public void objectToString() {
        assertTrue(JSONService.objectToString(plant).contains("prettyLongNameToBeACoincidence") );
    }
}