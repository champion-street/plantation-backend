package com.plantation.backend.service;

import com.plantation.backend.exception.FileStorageException;
import com.plantation.backend.property.FileStorageProperties;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageService {

    private final Path fileStorageLocation;


    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not create the directory where the files should be stored.", e);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = UUID.randomUUID().toString();
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        fileName += "." + extension;
        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return String.valueOf(targetLocation);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileStorageException("Could not store file!");
        }
    }

    public boolean deleteFile(String fileName) {
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        File file = new File(String.valueOf(targetLocation));
        return file.delete();
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}
