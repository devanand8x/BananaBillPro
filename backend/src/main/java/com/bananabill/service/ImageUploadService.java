package com.bananabill.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Image Upload Service - Uploads base64 images to Cloudinary for public URLs
 * Required for Twilio WhatsApp which needs HTTPS URLs for media
 */
@Service
public class ImageUploadService {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadService.class);

    @Value("${cloudinary.cloud.name:}")
    private String cloudName;

    @Value("${cloudinary.upload.preset:}")
    private String uploadPreset;

    private final RestTemplate restTemplate = new RestTemplate();

    @jakarta.annotation.PostConstruct
    public void init() {
        if (cloudName != null && !cloudName.isBlank() && uploadPreset != null && !uploadPreset.isBlank()) {
            logger.info("Cloudinary configured: cloud={}, preset={}", cloudName, uploadPreset);
        } else {
            logger.warn("Cloudinary NOT configured - WhatsApp images will be text-only");
        }
    }

    /**
     * Upload base64 image to Cloudinary and return public URL
     * Uses unsigned upload with upload preset for simplicity
     * 
     * @param base64Image Base64 encoded image (with or without data URI prefix)
     * @return Public HTTPS URL of uploaded image
     */
    public String uploadBase64Image(String base64Image) {
        logger.info("=== uploadBase64Image called, image size: {} chars ===",
                base64Image != null ? base64Image.length() : 0);

        if (cloudName == null || cloudName.isBlank() || uploadPreset == null || uploadPreset.isBlank()) {
            logger.warn("Cloudinary not configured - cannot upload image");
            return null;
        }

        logger.info("Cloudinary config OK, uploading to cloud: {}, preset: {}", cloudName, uploadPreset);

        try {
            String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

            // Prepare JSON request - Cloudinary accepts JSON with base64 data URI
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Keep data URI format - Cloudinary accepts it directly
            String imageData = base64Image;
            if (!base64Image.startsWith("data:")) {
                imageData = "data:image/png;base64," + base64Image;
            }

            // Use JSON body with explicit public_id to avoid "Display name cannot contain
            // slashes" error
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put("file", imageData);
            body.put("upload_preset", uploadPreset);
            body.put("public_id", "bill_" + System.currentTimeMillis()); // Unique ID without slashes

            HttpEntity<java.util.Map<String, String>> request = new HttpEntity<>(body, headers);

            // Upload to Cloudinary
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String secureUrl = (String) response.getBody().get("secure_url");
                if (secureUrl != null) {
                    logger.info("Image uploaded to Cloudinary successfully: {}", secureUrl);
                    return secureUrl;
                }
            }

            logger.error("Failed to upload image to Cloudinary: {}", response.getBody());
            return null;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Cloudinary HTTP error: {} - Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            logger.error("Cloudinary server error: {} - Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error uploading image to Cloudinary: {} - Type: {}", e.getMessage(), e.getClass().getName());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if Cloudinary is configured
     */
    public boolean isConfigured() {
        return cloudName != null && !cloudName.isBlank()
                && uploadPreset != null && !uploadPreset.isBlank();
    }
}
