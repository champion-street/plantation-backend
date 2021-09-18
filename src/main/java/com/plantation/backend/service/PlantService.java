package com.plantation.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.plantation.backend.model.Plant;
import com.plantation.backend.model.PlantDTO;
import com.plantation.backend.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class PlantService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private final SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    @Autowired
    PlantRepository plantRepository;

    @Autowired
    FileStorageService fileStorageService;

    public List<Plant> getAllPlants() {
        return plantRepository.findAll();
    }

    public Plant getPlant(long id) {
        return plantRepository.findById(id).orElse(null);
    }

    public Plant applyPatchToPlant(JsonPatch patch, Plant originalPlant) throws JsonPatchException, JsonProcessingException, ParseException {
        Plant patchedPlant = (Plant) JSONService.applyPatch(patch, originalPlant, Plant.class);
        calculateAndSetWateringDeadline(patchedPlant);
        return plantRepository.save(patchedPlant);
    }

    public Plant calculateAndSetWateringDeadline(Plant plant) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(plant.getLastWateringDate()));
        c.add(Calendar.DAY_OF_MONTH, plant.getWateringCycleInDays());
        plant.setWateringDeadline(sdf.format(c.getTime()));
        return plant;
    }

    public Plant addPlant(PlantDTO plantDTO) throws ParseException {

        Plant plant = Plant.builder().name(plantDTO.getName()).description(plantDTO.getDescription()).wateringCycleInDays(plantDTO.getWateringCycleInDays()).build();

        Date now = Calendar.getInstance().getTime();
        plant.setLastWateringDate(sdf.format(now));
        plant = calculateAndSetWateringDeadline(plant);
        plantRepository.save(plant);
        return plant;
    }

    public String uploadPlantImage(Plant plant, MultipartFile file) throws IOException {
        if (!StringUtils.isEmpty(plant.getImageURL())) {
            deletePlantImage(plant.getId());
        }
        return fileStorageService.storeFile(file);
    }

    public Plant updatePlantWatered(Plant plant) throws ParseException {
        plant.setLastWateringDate(sdf.format(Calendar.getInstance().getTime()));
        return calculateAndSetWateringDeadline(plant);
    }

    public void deletePlant(long id) throws IOException {
        deletePlantImage(id);
        plantRepository.deleteById(id);
    }

    private void deletePlantImage(long id) throws IOException {
        Plant plant = plantRepository.findById(id).orElse(null);
        if (plant != null && !StringUtils.isEmpty(plant.getImageURL())) {
            fileStorageService.deleteFile(plant.getImageURL());
        }
    }

    public Plant setPlantImageURL(Plant plant, String imageURL) {
        plant.setImageURL(imageURL);
        return plantRepository.save(plant);
    }

    public void deleteAllPlants() {
        plantRepository.deleteAll();
    }

    public void bulkWaterPlants(String body) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode dataTree = mapper.readTree(body);
            String[] idArray = mapper.treeToValue(dataTree.get("ids"), String[].class);
            for (String id : idArray) {
                Plant plant = plantRepository.findById(Long.parseLong(id)).orElse(null);
                if (plant != null) {
                    plant = updatePlantWatered(plant);
                    plantRepository.save(plant);
                }
            }
        } catch (JsonProcessingException | ParseException e) {
            e.printStackTrace();
        }
    }

}
