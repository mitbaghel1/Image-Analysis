package com.ia;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;

public class CustomVisionPrediction {

    public static void main(String[] args) {
        try {
            // Azure Custom Vision Prediction API endpoint and prediction key
            String apiUrl = "https://testingimage-prediction.cognitiveservices.azure.com/customvision/v3.0/Prediction/345d1e0d-c725-4f4a-a58f-c17a5b0d03c6/detect/iterations/Iteration4/image";
            
            //https://testingimage-prediction.cognitiveservices.azure.com/customvision/v3.0/Prediction/345d1e0d-c725-4f4a-a58f-c17a5b0d03c6/detect/iterations/Iteration4/image
            String predictionKey = "6b5a975fa95644278b491fa97b9cc8e8";

            // Image file to be predicted
            File imageFile = new File("C:\\Users\\mitba\\Downloads\\archive\\pillsPicture\\images\\medicine(8).jpg");
            double customThreshold = 0.8;
            
            System.out.println(imageFile.exists());
            // Encode the image as Base64
            String base64Image = encodeImageToBase64(imageFile);
            double threshold = 16.0;
            // Create the HTTP request
            URL url = new URL(apiUrl+"?threshold=" + threshold);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Prediction-Key", predictionKey);
            connection.setDoOutput(true);

            // Send the image data
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(Base64.getDecoder().decode(base64Image));
            }

            // Read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse and handle the prediction results (in JSON format)
            	 try (InputStream inputStream = connection.getInputStream()) {
                     byte[] responseBytes = readAllBytes(inputStream);
                     String jsonResponse = new String(responseBytes);
                     
                     // Check the probability in the response
                     double probability = extractProbability(jsonResponse);
                     
                     // Process the prediction based on the custom threshold
                     if (probability >= customThreshold) {
                         System.out.println("High confidence prediction: " + jsonResponse);
                     } else {
                         System.out.println("Low confidence prediction: " + jsonResponse);
                     }
                     System.out.println(jsonResponse);
                 }
            } else {
                System.err.println("Prediction request failed. Response code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static byte[] readAllBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[1024];
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private static String encodeImageToBase64(File imageFile) throws Exception {
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }
    
    private static String getHighestProbabilityTag(List<Prediction> predictions) {
        double maxProbability = 0.0;
        String highestProbabilityTag = null;

        for (Prediction prediction : predictions) {
            if (prediction.getProbability() > maxProbability) {
                maxProbability = prediction.getProbability();
                highestProbabilityTag = prediction.getTagName();
            }
        }

        return highestProbabilityTag;
    }
}
