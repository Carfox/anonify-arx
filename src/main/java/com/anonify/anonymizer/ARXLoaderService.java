package com.anonify.anonymizer;

import org.deidentifier.arx.Data;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Service
public class ARXLoaderService {

    /**
     * Carga un archivo CSV existente en disco usando ARX.
     * @param filePath Ruta absoluta o relativa del archivo CSV cargado localmente.
     * @return Objeto Data con los datos cargados en memoria.
     * @throws Exception si ocurre un error en la lectura o formato del archivo.
     */
    public Data loadCsvFile(String filePath) throws Exception {
        File csvFile = new File(filePath);

        if (!csvFile.exists() || !csvFile.isFile()) {
            throw new IllegalArgumentException("El archivo especificado no existe o no es un archivo válido: " + filePath);
        }

        // Cargar CSV en memoria con ARX
        Data data = Data.create(csvFile, StandardCharsets.UTF_8, ',');

        // Opcional: imprimir un resumen
        System.out.println("Archivo cargado correctamente con ARX");
        System.out.println("Número de registros: " + data.getHandle().getNumRows());
        System.out.println("Número de columnas: " + data.getHandle().getNumColumns());

        return data;
    }
}
