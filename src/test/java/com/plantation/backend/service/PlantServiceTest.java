package com.plantation.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.plantation.backend.Application;
import com.plantation.backend.model.Plant;
import com.plantation.backend.repository.PlantRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PlantServiceTest {

    @Autowired
    PlantRepository plantRepository;

    @Autowired
    PlantService plantService;

    @Autowired
    ObjectMapper objectMapper;

    public Plant plant;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public Object plantObject = new Object() {
        public String name = "plantName";
        public String description = "plantDescription";
        public int wateringCycle = 2;
    };

    public String patchPlant = "[{\"op\":\"replace\",\"path\":\"/description\",\"value\":\"Szep\"}]";

    @Before
    public void setUpDatabase() {
        plant = Plant.builder().name("name").description("description").wateringCycleInDays(3).build();
        plantRepository.save(plant);
    }

    @After
    public void deleteFromDatabase() {
        plantRepository.delete(plant);
    }

    @Test
    public void getAllPlants() {
        List<Plant> plantList = plantService.getAllPlants();
        assertEquals(1, plantList.size());
    }

    @Test
    public void getPlant() {
        List<Plant> plantList = plantRepository.findAll();
        long plantId = plantList.get(0).getId();
        assertEquals(plantList.get(0), plantService.getPlant(plantId));
    }

    @Test
    public void applyPatchToPlant() throws IOException, JsonPatchException {
        plant = plantRepository.findAll().get(0);
        Date now = Calendar.getInstance().getTime();
        plant.setLastWateringDate(sdf.format(now));
        InputStream in = new ByteArrayInputStream(patchPlant.getBytes());
        JsonPatch patch = objectMapper.readValue(in, JsonPatch.class);
        assertEquals("Szep", plantService.applyPatchToPlant(patch, plant).getDescription());
    }

    @Test
    public void calculateAndSetWateringDeadline() {
    }

    @Test
    @Transactional
    public void addPlant() throws JsonProcessingException {
        String plantJson = objectMapper.writeValueAsString(plantObject);
        Plant plant = plantService.addPlant(plantJson);
        assertEquals(2, plantRepository.findAll().size());
    }

    @Test
    public void uploadPlantImage() {
    }

    @Test
    public void updatePlantWatered() {
    }

    @Test
    public void deletePlant() {
        plantService.deletePlant(plantRepository.findAll().get(0).getId());
        assertEquals(0, plantRepository.findAll().size());
    }

    @Test
    public void deletePlantImage() {
    }

    @Test
    public void setPlantImageURL() {
    }

    @Test
    public void deleteAllPlants() {
        Plant newPlant = Plant.builder().name("name2").description("desc").wateringCycleInDays(1).build();
        plantRepository.save(newPlant);
        assertEquals(2, plantRepository.findAll().size());
        plantService.deleteAllPlants();
        assertEquals(0, plantRepository.findAll().size());
    }

    @Test
    public void bulkWaterPlants() {
    }
}