package com.anonify.controller;

import com.anonify.anonymizer.ARXLoaderService;
import com.anonify.anonymizer.AnonymizationService;
import com.anonify.common.AnonymizationRequestDTO;
import com.anonify.common.PrivacyModelDTO;
import org.deidentifier.arx.*;
import org.deidentifier.arx.criteria.KAnonymity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping
public class AnonymizationController {

    private final ARXLoaderService arxLoaderService;
    private final AnonymizationService anonymizationService;

    public AnonymizationController(ARXLoaderService arxLoaderService,
                                   AnonymizationService anonymizationService) {
        this.arxLoaderService = arxLoaderService;
        this.anonymizationService = anonymizationService;
    }

    // ✅ Endpoint /status para verificar la carga de ARX
    @GetMapping("/status")
    public ResponseEntity<?> testArxLibrary() {
        try {
            ARXAnonymizer anonymizer = new ARXAnonymizer();

            Map<String, String> arxClasses = new LinkedHashMap<>();
            arxClasses.put("ARXAnonymizer", ARXAnonymizer.class.getName());
            arxClasses.put("ARXConfiguration", ARXConfiguration.class.getName());
            arxClasses.put("Data", Data.class.getName());
            arxClasses.put("DataHandle", DataHandle.class.getName());
            arxClasses.put("KAnonymity", KAnonymity.class.getName());

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "✅ La librería ARX se cargó correctamente.",
                            "arxClasses", arxClasses
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "❌ Error al cargar la librería ARX: " + e.getMessage()
                    ));
        }
    }

    // ✅ Endpoint /anonymize/load para cargar CSV ya subido
    @PostMapping("/anonymize/load")
    public ResponseEntity<?> loadCsv(@RequestParam("filePath") String filePath) {
        try {
            Data data = arxLoaderService.loadCsvFile(filePath);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "✅ Archivo cargado correctamente",
                    "numRows", data.getHandle().getNumRows(),
                    "numColumns", data.getHandle().getNumColumns()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "❌ Error al cargar el archivo: " + e.getMessage()
                    ));
        }
    }

    // ✅ Endpoint /anonymize/process para anonimizar solo con k (sin JSON)
    @PostMapping("/anonymize/process")
    public ResponseEntity<?> anonymizeCsv(@RequestParam("filePath") String filePath,
                                          @RequestParam("k") int k) {
        try {
            Data data = arxLoaderService.loadCsvFile(filePath);

            // Crear un DTO manualmente con k-anonymity
            AnonymizationRequestDTO request = new AnonymizationRequestDTO();
            request.setFilePath(filePath);

            PrivacyModelDTO privacyModel = new PrivacyModelDTO();
            privacyModel.setType("K_ANONYMITY");
            privacyModel.setParameters(Map.of("k", k));
            request.setPrivacyModel(privacyModel);

            ARXResult result = anonymizationService.anonymizeData(data, request);

            DataHandle handle = result.getOutput();
            int numRows = handle.getNumRows();
            int numColumns = handle.getNumColumns();

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "✅ Archivo anonimizado correctamente",
                            "numRows", numRows,
                            "numColumns", numColumns
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "status", "error",
                            "message", "❌ Error al anonimizar el archivo: " + e.getMessage()
                    ));
        }
    }

    // ✅ Endpoint /anonymize para recibir JSON completo con atributos + modelo de privacidad
    @PostMapping("/anonymize")
    public ResponseEntity<?> anonymize(@RequestBody AnonymizationRequestDTO request) {
        try {
            Data data = arxLoaderService.loadCsvFile(request.getFilePath());
            ARXResult result = anonymizationService.anonymizeData(data, request);
            DataHandle output = result.getOutput();

            // Exportar el archivo anonimizado
            String outputPath = request.getFilePath().replace(".csv", "_anon.csv");
            anonymizationService.exportAnonymizedData(result, outputPath);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "✅ Anonimización exitosa",
                    "numRows", output.getNumRows(),
                    "numColumns", output.getNumColumns(),
                    "outputFile", outputPath
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "❌ Error en la anonimización: " + e.getMessage()
            ));
        }
    }
}
