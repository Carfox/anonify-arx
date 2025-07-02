package com.anonify.files;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() throws IOException {
        // Carpeta donde se guardarán los archivos
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    public Path storeFile(MultipartFile file) {
        String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString(); // <- corregido

        try {
            if (!fileName.toLowerCase().endsWith(".csv")) {
                throw new FileStorageException("Solo se permiten archivos CSV");
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation;
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo almacenar el archivo " + fileName + ". Error: " + ex.getMessage());
        }
    }


    public void deleteFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo eliminar el archivo " + filePath.getFileName() + ". Error: " + ex.getMessage());
        }
    }
}
