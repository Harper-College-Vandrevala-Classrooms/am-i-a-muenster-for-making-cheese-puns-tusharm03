package com.csc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CheeseAnalyzer {

    private static final double MOISTURE_THRESHOLD = 41.0;
    private static final Pattern LACTIC_PATTERN = Pattern.compile("\\blactic\\b", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) {
        String csvFile = "cheese_data.csv"; 
        String outputFile = "output.txt";

        int pasteurizedCount = 0;
        int rawCount = 0;
        int organicMoistCount = 0;
        int lacticCount = 0;
        double totalMoisture = 0.0;
        int cheeseCount = 0;
        Map<String, Integer> milkTypeCount = new HashMap<>();
        
        // Use a Set to track present IDs dynamically
        Set<Integer> presentIds = new HashSet<>();

        // Process the CSV file
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String[] headers = br.readLine().split(",");
            
            int milkTreatmentIndex = getColumnIndex(headers, "MilkTreatmentTypeEn");
            int organicIndex = getColumnIndex(headers, "Organic");
            int moistureIndex = getColumnIndex(headers, "MoisturePercent");
            int milkTypeIndex = getColumnIndex(headers, "MilkTypeEn");
            int flavourIndex = getColumnIndex(headers, "FlavourEn");
            int cheeseIdIndex = getColumnIndex(headers, "CheeseId");

            // Create files for further tasks
            try (BufferedWriter bwNoHeaders = new BufferedWriter(new FileWriter("cheese_without_headers.csv"));
                 BufferedWriter bwNoIds = new BufferedWriter(new FileWriter("cheese_without_ids.csv"));
                 BufferedWriter bwMissingIds = new BufferedWriter(new FileWriter("missing_ids.txt"))) {
                
                int lastId = 200; // Starting from 200

                // Read data and perform calculations
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length <= Math.max(milkTreatmentIndex, Math.max(organicIndex, Math.max(moistureIndex, Math.max(milkTypeIndex, Math.max(flavourIndex, cheeseIdIndex)))))) {
                        continue; // Skip if any required column is missing
                    }

                    String milkTreatment = values[milkTreatmentIndex].trim();
                    String organic = values[organicIndex].trim();
                    String moisture = values[moistureIndex].trim();
                    String milkType = values[milkTypeIndex].trim();
                    String flavour = values[flavourIndex].trim();
                    String cheeseId = values[cheeseIdIndex].trim();

                    // Write to cheese_without_headers.csv
                    bwNoHeaders.write(line);
                    bwNoHeaders.newLine();

                    // Write to cheese_without_ids.csv
                    StringBuilder noIdLine = new StringBuilder();
                    for (int i = 0; i < values.length; i++) {
                        if (i != cheeseIdIndex) {
                            noIdLine.append(values[i]);
                            if (i < values.length - 1) {
                                noIdLine.append(",");
                            }
                        }
                    }
                    bwNoIds.write(noIdLine.toString());
                    bwNoIds.newLine();

                    // Track present IDs
                    int id = Integer.parseInt(cheeseId);
                    presentIds.add(id); // Add ID to the Set
                    lastId = Math.max(lastId, id);

                    // Count pasteurized and raw cheeses
                    if (milkTreatment.equalsIgnoreCase("pasteurized")) {
                        pasteurizedCount++;
                    } else if (milkTreatment.equalsIgnoreCase("raw")) {
                        rawCount++;
                    }

                    // Calculate average moisture
                    if (!moisture.isEmpty()) {
                        try {
                            double moistureValue = Double.parseDouble(moisture);
                            totalMoisture += moistureValue;
                            cheeseCount++;
                            if (organic.equals("1") && moistureValue > MOISTURE_THRESHOLD) {
                                organicMoistCount++;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid moisture value: " + moisture);
                        }
                    }

                    // Count lactic cheeses
                    if (LACTIC_PATTERN.matcher(flavour).find()) {
                        lacticCount++;
                    }

                }

                // Identify missing IDs
                for (int i = 200; i <= lastId; i++) {
                    if (!presentIds.contains(i)) {
                        bwMissingIds.write(String.valueOf(i));
                        bwMissingIds.newLine();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write results to output file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            bw.write("Number of cheeses using pasteurized milk: " + pasteurizedCount);
            bw.newLine();
            bw.write("Number of cheeses using raw milk: " + rawCount);
            bw.newLine();
            bw.write("Number of organic cheeses with moisture > 41.0%: " + organicMoistCount);
            bw.newLine();
            bw.write("Average moisture percent: " + (cheeseCount > 0 ? totalMoisture / cheeseCount : 0.0));
            bw.newLine();
            bw.write("Number of lactic cheeses: " + lacticCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getColumnIndex(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1; 
    }
}
