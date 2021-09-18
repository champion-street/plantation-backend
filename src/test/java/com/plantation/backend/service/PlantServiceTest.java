package com.plantation.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.plantation.backend.Application;
import com.plantation.backend.model.Plant;
import com.plantation.backend.model.PlantDTO;
import com.plantation.backend.repository.PlantRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    FileStorageService service;

    public Plant plant;

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public String bulkWaterIds = "{\"ids\":[\"1\", \"2\"]}";

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
    public void testGetAllPlantsShouldReturnOne() {
        List<Plant> plantList = plantService.getAllPlants();
        assertEquals(1, plantList.size());
    }

    @Test
    public void testGetPlantShouldReturnTheOnlyPlant() {
        List<Plant> plantList = plantRepository.findAll();
        long plantId = plantList.get(0).getId();
        assertEquals(plantList.get(0), plantService.getPlant(plantId));
    }

    @Test
    public void testApplyPatchToPlantShouldReturnTheUpdatedValue() throws IOException, JsonPatchException, ParseException {
        Date now = Calendar.getInstance().getTime();
        plant.setLastWateringDate(sdf.format(now));
        InputStream in = new ByteArrayInputStream(patchPlant.getBytes());
        JsonPatch patch = objectMapper.readValue(in, JsonPatch.class);
        assertEquals("Szep", plantService.applyPatchToPlant(patch, plant).getDescription());
    }

    @Test(expected = ParseException.class)
    public void testCalculateAndSetWateringDeadlineShouldThrowParseException() throws ParseException {
        plant.setLastWateringDate("asdasd");
        plantService.calculateAndSetWateringDeadline(plant);
    }

    @Test
    @Transactional
    public void testCreatePlantShouldCreateThePlant() throws JsonProcessingException, ParseException {
        PlantDTO newPlant = PlantDTO.builder().name("newPlant").wateringCycleInDays(3).build();
        Plant plant = plantService.createPlant(newPlant);
        assertEquals(plant.getName(), newPlant.getName());
    }

    @Test
    public void testUploadPlantImageShouldReturnPNG() throws IOException {
        MultipartFile file = getFile();
        assertTrue(plantService.uploadPlantImage(plant, file).endsWith("png"));
        deleteFile();
    }

    @Test
    public void testUpdatePlantWateredShouldReturnTheSameDate() throws ParseException {
        Date now = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DAY_OF_MONTH, plant.getWateringCycleInDays());

        assertEquals(sdf.format(c.getTime()), plantService.updatePlantWatered(plant).getWateringDeadline());
    }

    @Test
    public void testDeletePlantShouldReturnEmptyDb() throws IOException {
        plantService.deletePlant(plantRepository.findAll().get(0).getId());
        assertEquals(0, plantRepository.findAll().size());
    }

    @Test
    public void testSetPlantImageURLShouldReturnTestImage() {
        plant = plantService.setPlantImageURL(plant, "testImage.png");
        assertEquals("testImage.png", plant.getImageURL());
    }

    @Test
    public void testDeleteAllPlantsShouldReturnEmptyDb() {
        Plant newPlant = Plant.builder().name("name2").description("desc").wateringCycleInDays(1).build();
        plantRepository.save(newPlant);
        assertEquals(2, plantRepository.findAll().size());
        plantService.deleteAllPlants();
        assertEquals(0, plantRepository.findAll().size());
    }

    @Test
    public void bulkWaterPlants() {
        Plant newPlant = Plant.builder().name("plantName").description("desc").wateringCycleInDays(2).lastWateringDate(sdf.format(Calendar.getInstance().getTime())).build();
        plantRepository.save(newPlant);
        assertEquals(2, plantRepository.findAll().size());
        List<Plant> plants = plantService.bulkWaterPlants(bulkWaterIds);
        Date now = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DAY_OF_MONTH, plant.getWateringCycleInDays());
        assertEquals(sdf.format(c.getTime()), plants.get(0).getWateringDeadline());
        c.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals(sdf.format(c.getTime()), plants.get(1).getWateringDeadline());
    }

    private MultipartFile getFile() {
        Path path = Paths.get("E:\\_Munka\\plantation-backend\\src\\test\\resources\\testImages\\testimage.png");
        String name = "static/testimage.png";
        String originalFileName = "static/testimage.png";
        String contentType = "image/png";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MockMultipartFile(name, originalFileName, contentType, content);
    }

    private void deleteFile() {
        Path path = service.getFileStorageLocation();
        for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
            file.delete();
        }
    }
}