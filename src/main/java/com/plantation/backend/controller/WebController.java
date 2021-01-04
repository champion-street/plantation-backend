package com.plantation.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.plantation.backend.model.Plant;
import com.plantation.backend.service.JSONService;
import com.plantation.backend.service.PlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
public class WebController {

    @Autowired
    PlantService plantService;

    @GetMapping("/list")
    public ResponseEntity getPlants() {
        List<Plant> plantList = plantService.getAllPlants();
        return createResponse(plantList);
    }

    @PostMapping(value = "/plant")
    public ResponseEntity addPlant(@RequestBody String body) {
        Plant plant = plantService.addPlant(body);
        return createResponse(plant);
    }

    @GetMapping("/plant/{id}")
    public ResponseEntity getPlant(@PathVariable("id") long id) {
        Plant plant = plantService.getPlant(id);
        plantService.calculateAndSetWateringDeadline(plant);
        return createResponse(plant);
    }

    @PostMapping(value = "/plant/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity addImageToPlant(@PathVariable("id") long id, @RequestParam MultipartFile file) {
        Plant plant = plantService.getPlant(id);
        String imageURL = plantService.uploadPlantImage(plant, file);
        plantService.setPlantImageURL(plant, imageURL);
        return createResponse(plant);
    }

    @PutMapping("/plant/{id}")
    public ResponseEntity updatePlantWatered(@PathVariable("id") long id) {
        Plant plant = plantService.getPlant(id);
        return createResponse(plantService.updatePlantWatered(plant));
    }

    @DeleteMapping("/plant/{id}")
    public ResponseEntity deletePlant(@PathVariable("id") long id) {
        plantService.deletePlant(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PatchMapping(value = "/plant/{id}", consumes = "application/json-patch+json")
    public ResponseEntity updatePlant(@PathVariable("id") long id, @RequestBody JsonPatch jsonPatch) {
        Plant plant = plantService.getPlant(id);
        Plant patchedPlant = null;
        try {
            patchedPlant = plantService.applyPatchToPlant(jsonPatch, plant);
        } catch (JsonPatchException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return createResponse(patchedPlant);
    }

    private ResponseEntity createResponse(Object object) {
        String returnJson = JSONService.objectToString(object);
        if (!StringUtils.isEmpty(returnJson)) {
            return new ResponseEntity(returnJson, HttpStatus.OK);
        } else {
            return new ResponseEntity("Something went wrong!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
