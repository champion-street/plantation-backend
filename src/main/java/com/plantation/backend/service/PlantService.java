package com.plantation.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.plantation.backend.model.Plant;
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

    private final String DATE_PATTERN = "yyyy-MM-dd";

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

    public Plant applyPatchToPlant(JsonPatch patch, Plant originalPlant) throws JsonPatchException, JsonProcessingException {
        Plant patchedPlant = (Plant) JSONService.applyPatch(patch, originalPlant, Plant.class);
        calculateAndSetWateringDeadline(patchedPlant);
        return plantRepository.save(patchedPlant);
    }

    public Plant calculateAndSetWateringDeadline(Plant plant) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(plant.getLastWateringDate()));
            c.add(Calendar.DAY_OF_MONTH, plant.getWateringCycleInDays());
            plant.setWateringDeadline(sdf.format(c.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return plant;
    }

    public Plant addPlant(String body) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode dataTree = mapper.readTree(body);
            String name = mapper.treeToValue(dataTree.get("name"), String.class);
            String description = mapper.treeToValue(dataTree.get("description"), String.class);
            int wateringCycle = mapper.treeToValue(dataTree.get("wateringCycle"), Integer.class);
            Plant plant = Plant.builder().name(name).description(description).wateringCycleInDays(wateringCycle).build();

            if (plant != null) {
                Date now = Calendar.getInstance().getTime();
                plant.setLastWateringDate(sdf.format(now));
                plant = calculateAndSetWateringDeadline(plant);
                plantRepository.save(plant);
                return plant;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String uploadPlantImage(Plant plant, MultipartFile file) {
        if (!StringUtils.isEmpty(plant.getImageURL())) {
            deletePlantImage(plant.getId());
        }
        return fileStorageService.storeFile(file);
    }

    public Plant updatePlantWatered(Plant plant) {
        plant.setLastWateringDate(sdf.format(Calendar.getInstance().getTime()));
        return calculateAndSetWateringDeadline(plant);
    }

    public void deletePlant(long id) {
        deletePlantImage(id);
        plantRepository.deleteById(id);
    }

    public void deletePlantImage(long id) {
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
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
