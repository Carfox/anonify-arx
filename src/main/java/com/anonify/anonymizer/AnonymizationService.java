package com.anonify.anonymizer;

import com.anonify.common.AnonymizationRequestDTO;
import com.anonify.common.AttributeDTO;
import org.deidentifier.arx.*;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.KAnonymity;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

@Service
public class AnonymizationService {

    public ARXResult anonymizeData(Data data, AnonymizationRequestDTO request) throws Exception {
        DataDefinition definition = data.getDefinition();
        List<AttributeDTO> attributes = request.getAttributes();

        for (AttributeDTO attr : attributes) {
            String name = attr.getName();
            String attrType = attr.getAttributeType();
            Map<String, Object> strategy = attr.getHierarchyStrategy();

            switch (attrType) {
                case "IDENTIFYING_ATTRIBUTE":
                    // No es posible modificar los datos directamente en ARX; solo marcamos como INSENSITIVE
                    definition.setAttributeType(name, AttributeType.IDENTIFYING_ATTRIBUTE);
                    break;

                case "QUASI_IDENTIFYING_ATTRIBUTE":
                    if (strategy != null) {
                        String strategyType = (String) strategy.get("type");
                        if ("class_intervals".equals(strategyType)) {
                            List<Integer> intValues = getNumericColumnValues(data, name);
                            Hierarchy hierarchy = buildNumericHierarchy(
                                    intValues,
                                    (Integer) strategy.getOrDefault("numClasses", 5),
                                    (Integer) strategy.getOrDefault("amplitud", -1)
                            );
                            definition.setAttributeType(name, hierarchy);
                            continue;
                        } else if ("categorical".equals(strategyType)) {
                            List<String> strValues = getStringColumnValues(data, name);
                            Hierarchy hierarchy = buildCategoricalHierarchy(strValues);
                            definition.setAttributeType(name, hierarchy);
                            continue;
                        }
                    }
                    definition.setAttributeType(name, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
                    break;

                case "SENSITIVE_ATTRIBUTE":
                    definition.setAttributeType(name, AttributeType.SENSITIVE_ATTRIBUTE);
                    break;

                case "INSENSITIVE_ATTRIBUTE":
                    definition.setAttributeType(name, AttributeType.INSENSITIVE_ATTRIBUTE);
                    break;

                default:
                    throw new IllegalArgumentException("Tipo de atributo desconocido: " + attrType);
            }
        }

        ARXConfiguration config = ARXConfiguration.create();

        Map<String, Object> params = request.getPrivacyModel().getParameters();
        String type = request.getPrivacyModel().getType();

        if ("K_ANONYMITY".equals(type)) {
            int k = (Integer) params.get("k");
            config.addPrivacyModel(new KAnonymity(k));
        } else if ("K_AND_L_DIVERSITY".equals(type)) {
            int k = (Integer) params.get("k");
            int l = (Integer) params.get("l");
            config.addPrivacyModel(new KAnonymity(k));
            config.addPrivacyModel(new DistinctLDiversity(request.getAttributes().stream()
                    .filter(attr -> "SENSITIVE_ATTRIBUTE".equals(attr.getAttributeType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No sensitive attribute found"))
                    .getName(), l));
        } else {
            throw new IllegalArgumentException("Modelo de privacidad desconocido: " + type);
        }

        config.setSuppressionLimit(1.0);
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        return anonymizer.anonymize(data, config);
    }

    public void exportAnonymizedData(ARXResult result, String outputPath) throws Exception {
        DataHandle handle = result.getOutput();
        if (handle == null) {
            throw new IllegalStateException("No se generó un resultado anonimizador.");
        }
        try (OutputStream out = new FileOutputStream(outputPath)) {
            handle.save(out, ',');
        }
    }

    private List<Integer> getNumericColumnValues(Data data, String columnName) throws Exception {
        List<Integer> values = new ArrayList<>();
        int columnIndex = data.getHandle().getColumnIndexOf(columnName);
        for (int i = 0; i < data.getHandle().getNumRows(); i++) {
            String val = data.getHandle().getValue(i, columnIndex);
            try {
                values.add(Integer.parseInt(val.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor no numérico '" + val + "' en columna '" + columnName + "'.");
            }
        }
        return values;
    }

    private List<String> getStringColumnValues(Data data, String columnName) {
        List<String> values = new ArrayList<>();
        int columnIndex = data.getHandle().getColumnIndexOf(columnName);
        for (int i = 0; i < data.getHandle().getNumRows(); i++) {
            String val = data.getHandle().getValue(i, columnIndex);
            if (val != null && !val.isEmpty()) {
                values.add(val.trim());
            }
        }
        return values;
    }

    private Hierarchy buildNumericHierarchy(List<Integer> values, int numClasses, int amplitud) {
        if (values.isEmpty()) throw new IllegalArgumentException("La lista de valores está vacía.");
        int minVal = Collections.min(values);
        int maxVal = Collections.max(values);

        if (amplitud == -1) {
            amplitud = (maxVal - minVal + 1 + numClasses - 1) / numClasses;
        }

        List<int[]> intervals = new ArrayList<>();
        for (int start = minVal; start <= maxVal; start += amplitud) {
            int end = Math.min(start + amplitud - 1, maxVal);
            intervals.add(new int[]{start, end});
        }

        List<String[]> hierarchyData = new ArrayList<>();
        for (int val : values) {
            for (int[] interval : intervals) {
                if (val >= interval[0] && val <= interval[1]) {
                    hierarchyData.add(new String[]{String.valueOf(val), interval[0] + "-" + interval[1]});
                    break;
                }
            }
        }

        String[][] hierarchyArray = hierarchyData.toArray(new String[0][]);
        for (String[] row : hierarchyData) {
            System.out.println("Jerarquía: " + Arrays.toString(row));
        }
        return Hierarchy.create(hierarchyArray);
    }

    private Hierarchy buildCategoricalHierarchy(List<String> values) {
        List<String[]> hierarchyData = new ArrayList<>();
        for (String val : values) {
            val = val.trim();
            List<String> levels = new ArrayList<>();
            levels.add(val);
            for (int i = 1; i < Math.min(3, val.length()); i++) {
                levels.add(val.substring(0, val.length() - i) + new String(new char[i]).replace("\0", "*"));
            }
            levels.add("*");
            for (int j = 0; j < levels.size() - 1; j++) {
                hierarchyData.add(new String[]{levels.get(j), levels.get(j + 1)});
            }
        }
        String[][] hierarchyArray = hierarchyData.toArray(new String[0][]);
        return Hierarchy.create(hierarchyArray);
    }
}
