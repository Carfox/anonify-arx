package com.anonify.files;

import com.anonify.common.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path; // ← este es el import que faltaba
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Path storedPath = fileStorageService.storeFile(file);

            return ResponseEntity.ok(
                    new BaseResponse<>(
                            "success",
                            0,
                            "Archivo cargado correctamente",
                            storedPath.toString()
                    )
            );

        } catch (FileStorageException e) {
            return ResponseEntity.badRequest().body(
                    new BaseResponse<>(
                            "error",
                            -1,
                            e.getMessage(),
                            null
                    )
            );
        }
    }






}
