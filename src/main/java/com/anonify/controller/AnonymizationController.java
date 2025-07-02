package com.anonify.controller;

import org.deidentifier.arx.*;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.Data.DefaultData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/anonymize")
public class AnonymizationController {

    @PostMapping
    public ResponseEntity<?> testArxLibrary() {
        try {
            // Intentar crear instancia de ARXAnonymizer para comprobar carga de la librería
            ARXAnonymizer anonymizer = new ARXAnonymizer();

            // Listado simple de clases principales de ARX como "diagnóstico"
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


}
