package com.bananabill.service;

import com.bananabill.model.Bill;
import com.bananabill.security.UrlValidator;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;

/**
 * WhatsApp Service - Sends messages via Twilio WhatsApp Business API
 * 
 * PERFORMANCE: All methods are async to prevent blocking main thread
 * SECURITY: All image URLs are validated against SSRF attacks
 */
@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private final ImageUploadService imageUploadService;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String fromNumber;

    public WhatsAppService(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank() &&
                authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio WhatsApp service initialized");
        } else {
            logger.warn("Twilio credentials not configured - WhatsApp disabled");
        }
    }

    /**
     * Send bill to farmer's WhatsApp with image
     * 
     * @param bill     The bill to send
     * @param imageUrl URL of bill image (HTTPS from allowed domain) or base64 data
     *                 URI
     * @throws SecurityException if imageUrl is not from allowed domain (when not
     *                           base64)
     */
    public void sendBillToFarmer(Bill bill, String imageUrl) {
        // Skip if Twilio is not configured
        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()) {
            logger.warn("Twilio not configured - skipping WhatsApp send for bill {}", bill.getBillNumber());
            throw new RuntimeException(
                    "Twilio WhatsApp is not configured. Please configure TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_WHATSAPP_FROM in environment.");
        }

        try {
            String farmerMobile = bill.getFarmer().getMobileNumber();
            String toNumber = formatPhoneNumber(farmerMobile);

            String messageText = String.format(
                    "🍌 *Banana Bill*\n\n" +
                            "📋 Bill No: %s\n" +
                            "👤 Name: %s\n" +
                            "💰 Net Amount: ₹%,.0f\n\n" +
                            "Thank you!",
                    bill.getBillNumber(),
                    bill.getFarmer().getName(),
                    bill.getNetAmount().doubleValue());

            // Validate image URL (frontend uploads to Cloudinary, sends URL)
            String publicImageUrl = null;
            if (imageUrl != null && !imageUrl.isBlank()) {
                // SECURITY: Validate image URL to prevent SSRF
                publicImageUrl = UrlValidator.validateImageUrl(imageUrl);
            }

            // Send message with or without image
            if (publicImageUrl != null) {
                Message message = Message.creator(
                        new PhoneNumber(toNumber),
                        new PhoneNumber(fromNumber),
                        messageText)
                        .setMediaUrl(java.util.Arrays.asList(URI.create(publicImageUrl)))
                        .create();

                logger.info("WhatsApp bill with image sent successfully. SID: {}", message.getSid());
            } else {
                // No image or upload failed, send text only
                Message message = Message.creator(
                        new PhoneNumber(toNumber),
                        new PhoneNumber(fromNumber),
                        messageText)
                        .create();

                logger.info("WhatsApp text-only bill sent successfully. SID: {}", message.getSid());
            }

        } catch (SecurityException e) {
            logger.error("SSRF attempt blocked in sendBillToFarmer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp message: {}", e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }

    /**
     * Send text-only message to farmer's WhatsApp
     */
    public void sendTextMessage(String mobileNumber, String messageText) {
        try {
            String toNumber = formatPhoneNumber(mobileNumber);

            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    messageText)
                    .create();

            logger.info("WhatsApp text message sent. SID: {}", message.getSid());

        } catch (Exception e) {
            logger.error("Failed to send WhatsApp text message: {}", e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }

    /**
     * Send statement image to farmer's WhatsApp
     */
    public void sendStatementToFarmer(String mobileNumber, String farmerName, int billCount,
            double totalAmount, String imageUrl) {
        // SECURITY: Validate image URL
        String validatedUrl = UrlValidator.validateImageUrl(imageUrl);

        try {
            String toNumber = formatPhoneNumber(mobileNumber);

            String messageText = String.format(
                    "🍌 *Bill Statement*\n\n" +
                            "👤 Farmer: %s\n" +
                            "📋 Total Bills: %d\n" +
                            "💰 Total Amount: ₹%,.0f\n\n" +
                            "Thank you!",
                    farmerName,
                    billCount,
                    totalAmount);

            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    messageText)
                    .setMediaUrl(java.util.Arrays.asList(URI.create(validatedUrl)))
                    .create();

            logger.info("Statement sent via WhatsApp. SID: {}", message.getSid());

        } catch (SecurityException e) {
            logger.error("SSRF attempt blocked in sendStatementToFarmer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to send statement via WhatsApp: {}", e.getMessage());
            throw new RuntimeException("Failed to send statement via WhatsApp", e);
        }
    }

    /**
     * Send payment confirmation to farmer's WhatsApp
     */
    public void sendPaymentConfirmation(Bill bill) {
        // Skip if Twilio is not configured
        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()) {
            logger.warn("Twilio not configured - skipping payment confirmation WhatsApp for bill {}",
                    bill.getBillNumber());
            return;
        }

        try {
            String farmerMobile = bill.getFarmer().getMobileNumber();
            String toNumber = formatPhoneNumber(farmerMobile);

            double paidAmount = bill.getPaidAmount() != null ? bill.getPaidAmount().doubleValue()
                    : bill.getNetAmount().doubleValue();

            String messageText = String.format(
                    "✅ *Payment Done!*\n\n" +
                            "Dear %s,\n\n" +
                            "📋 Bill No: %s\n" +
                            "� Date: %s\n" +
                            "💰 Amount Paid: ₹%,.0f\n\n" +
                            "Your payment has been transferred successfully.\n\n" +
                            "Thank you! 🍌",
                    bill.getFarmer().getName(),
                    bill.getBillNumber(),
                    bill.getCreatedAt().toLocalDate().toString(),
                    paidAmount);

            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    messageText)
                    .create();

            logger.info("Payment confirmation sent via WhatsApp. SID: {}", message.getSid());

        } catch (Exception e) {
            logger.error("Failed to send payment confirmation: {}", e.getMessage());
            throw new RuntimeException("Failed to send payment confirmation", e);
        }
    }

    /**
     * Format phone number for WhatsApp
     */
    private String formatPhoneNumber(String mobile) {
        String cleanMobile = mobile.replaceAll("\\D", "");
        return cleanMobile.startsWith("91")
                ? "whatsapp:+" + cleanMobile
                : "whatsapp:+91" + cleanMobile;
    }
}
