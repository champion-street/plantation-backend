package com.plantation.backend.service;

import com.plantation.backend.Application;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FileStorageServiceTest {

    MultipartFile result;

    @Autowired
    FileStorageService fileStorageService;

    @Before
    public void getFile() {
        Path path = Paths.get("E:\\_Munka\\plantation-backend\\src\\test\\resources\\testImages\\testimage.png");
        String name = "testimage.png";
        String originalFileName = "testimage.png";
        String contentType = "image/png";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = new MockMultipartFile(name, originalFileName, contentType, content);
    }

    @After
    public void deleteFile() {
        Path path = fileStorageService.getFileStorageLocation();
        for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
            file.delete();
        }
    }

    @Test
    public void testStoreFileExtensionShouldBeTheSameAsOriginalFile() {
        assertTrue(fileStorageService.storeFile(result).endsWith("png"));
    }

    @Test
    public void testStoreFileDirectoryShouldContainOneFile() {
        fileStorageService.storeFile(result);
        assertEquals(1, Objects.requireNonNull(new File("E:\\opt\\plantation-backend\\images").listFiles()).length);
    }

    @Test
    public void testDeleteFileShouldReturn0AsLengthOfFilesInDirectory() throws IOException {
        fileStorageService.storeFile(result);
        fileStorageService.deleteFile(Objects.requireNonNull(new File("E:\\opt\\plantation-backend\\images").listFiles())[0].getName());
        assertEquals(0, new File("E:\\opt\\plantation-backend\\images").listFiles().length);
    }
}