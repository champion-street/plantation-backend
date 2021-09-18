package com.plantation.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.plantation.backend.model.Plant;
import com.plantation.backend.model.PlantDTO;
import com.plantation.backend.service.JSONService;
import com.plantation.backend.service.PlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;

@CrossOrigin("*")
@RestController
public class WebController {

    @Autowired
    PlantService plantService;

    @GetMapping("/list")
    public ResponseEntity<?> getPlants() {
        return createResponse(plantService.getAllPlants());
    }

    @PostMapping(value = "/plant")
    public ResponseEntity<?> addPlant(@RequestBody PlantDTO plant) {
        Plant newPlant = null;
        try {
            newPlant = plantService.addPlant(plant);
        } catch (ParseException e) {
            createResponse("Something went wrong during creating plant!");
        }
        return createResponse(newPlant, HttpStatus.CREATED);
    }

    @PutMapping("/plant")
    public ResponseEntity<?> bulkWaterPlants(@RequestBody String body) {
        plantService.bulkWaterPlants(body);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/plant/{id}")
    public ResponseEntity<?> getPlant(@PathVariable("id") long id) {
        Plant plant = plantService.getPlant(id);
        try {
            plantService.calculateAndSetWateringDeadline(plant);
        } catch (Exception e) {
            return createResponse("Something went wrong during getting the desired plant!");
        }
        return createResponse(plant);
    }

    @PostMapping(value = "/plant/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> addImageToPlant(@PathVariable("id") long id, @RequestParam MultipartFile file) throws IOException {
        Plant plant = plantService.getPlant(id);
        String imageURL = plantService.uploadPlantImage(plant, file);
        plant = plantService.setPlantImageURL(plant, imageURL);
        return createResponse(plant);
    }

    @PutMapping("/plant/{id}")
    public ResponseEntity<?> updatePlantWatered(@PathVariable("id") long id) {
        Plant plant = plantService.getPlant(id);
        try {
            return createResponse(plantService.updatePlantWatered(plant));
        } catch (ParseException e) {
            return createResponse("Error during watering!");
        }
    }

    @DeleteMapping("/plant/{id}")
    public ResponseEntity<?> deletePlant(@PathVariable("id") long id) {
        try {
            plantService.deletePlant(id);
        } catch (Exception e) {
            return createResponse("Something went wrong while deleting plant!");
        }
        return createResponse("Plant deleted!");
    }

    @PatchMapping(value = "/plant/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<?> updatePlant(@PathVariable("id") long id, @RequestBody JsonPatch jsonPatch) {
        Plant plant = plantService.getPlant(id);
        Plant patchedPlant;
        try {
            patchedPlant = plantService.applyPatchToPlant(jsonPatch, plant);
        } catch (JsonPatchException | JsonProcessingException | ParseException e) {
            return createResponse("Error during updating plant!");
        }
        return createResponse(patchedPlant);
    }

    @DeleteMapping("/bulk-delete-all")
    public ResponseEntity<?> deleteAllPlantFromDb() {
        plantService.deleteAllPlants();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<?> createResponse(Object object) {
        return createResponse(object, null, HttpStatus.OK);
    }

    private ResponseEntity<?> createResponse(String message) {
        return createResponse(null, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<?> createResponse(Object object, HttpStatus statusCode) {
        return createResponse(object, null, statusCode);
    }

    private ResponseEntity<?> createResponse(Object object, String message, HttpStatus statusCode) {
        if (object != null) {
            String returnJson = JSONService.objectToString(object);
            return new ResponseEntity<>(returnJson, statusCode);
        } else {
            return new ResponseEntity<>(message, statusCode);
        }
    }

}
