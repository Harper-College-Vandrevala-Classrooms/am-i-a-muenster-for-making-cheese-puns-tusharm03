package com.csc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CheeseAnalyzer {

    public static void main(String[] args) {
        String csvFile = "cheese_data.csv"; 
        String outputFile = "output.txt";

        int pasteurizedCount = 0;
        int rawCount = 0;
        int organicMoistCount = 0;
        Map<String, Integer> milkTypeCount = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String[] headers = br.readLine().split(",");
            int milkTreatmentIndex = getColumnIndex(headers, "MilkTreatmentTypeEn");
            int organicIndex = getColumnIndex(headers, "Organic");
            int moistureIndex = getColumnIndex(headers, "MoisturePercent");
            int milkTypeIndex = getColumnIndex(headers, "MilkTypeEn");

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                // Check for missing values
                if (values.length <= milkTreatmentIndex || values.length <= organicIndex ||
                    values.length <= moistureIndex || values.length <= milkTypeIndex) {
                    continue; 
                }

                String milkTreatment = values[milkTreatmentIndex].trim();
                String organic = values[organicIndex].trim();
                String moisture = values[moistureIndex].trim();
                String milkType = values[milkTypeIndex].trim();

                if (milkTreatment.equalsIgnoreCase("pasteurized")) {
                    pasteurizedCount++;
                } else if (milkTreatment.equalsIgnoreCase("raw")) {
                    rawCount++;
                }

                if (organic.equals("1") && !moisture.isEmpty()) {
                    double moistureValue = Double.parseDouble(moisture);
                    if (moistureValue > 41.0) {
                        organicMoistCount++;
                    }
                }

                if (!milkType.isEmpty()) {
                    milkTypeCount.put(milkType, milkTypeCount.getOrDefault(milkType, 0) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Determine the most common milk type
        String mostCommonMilkType = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : milkTypeCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonMilkType = entry.getKey();
            }
        }

        // Write results to output file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            bw.write("Number of cheeses using pasteurized milk: " + pasteurizedCount);
            bw.newLine();
            bw.write("Number of cheeses using raw milk: " + rawCount);
            bw.newLine();
            bw.write("Number of organic cheeses with moisture > 41.0%: " + organicMoistCount);
            bw.newLine();
            bw.write("Most common type of animal milk: " + mostCommonMilkType);
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
