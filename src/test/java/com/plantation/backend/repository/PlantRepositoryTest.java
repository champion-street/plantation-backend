package com.plantation.backend.repository;

import com.plantation.backend.model.Plant;
import com.plantation.backend.repository.PlantRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PlantRepositoryTest {

    @Autowired
    PlantRepository plantRepository;

    @Test
    public void smoke() {
        assertTrue(true);
    }

    @Test
    public void testSavePlant() {
        Plant plant = Plant.builder().name("name").description("description").wateringCycleInDays(3).build();
        plantRepository.save(plant);
        List<Plant> plantList = plantRepository.findAll();
        assertEquals(1, plantList.size());
    }

}
