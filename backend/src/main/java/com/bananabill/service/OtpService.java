package com.bananabill.service;

import com.bananabill.exception.ValidationException;
import com.bananabill.model.Otp;
import com.bananabill.repository.OtpRepository;
import com.bananabill.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final String fast2SmsApiKey;
    private final String frontendUrl;

    public OtpService(
            OtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Value("${fast2sms.api.key:}") String fast2SmsApiKey,
            @Value("${frontend.url:http://localhost:5173}") String frontendUrl) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.fast2SmsApiKey = fast2SmsApiKey;
        this.frontendUrl = frontendUrl;
    }

    public boolean sendOtp(String mobile, String action) throws Exception {
        // Generate 6-digit OTP
        String otp = String.valueOf((int) (100000 + Math.random() * 900000));

        // Store hashed OTP with 5 minute expiry
        Otp entry = new Otp(mobile, passwordEncoder.encode(otp), Instant.now().plusSeconds(5 * 60), action);
        otpRepository.save(entry);

        // If Fast2SMS API key configured, send via Fast2SMS
        if (fast2SmsApiKey != null && !fast2SmsApiKey.isBlank()) {
            String message = String.format("Your Banana Bill OTP is %s. Valid for 5 minutes.", otp);

            HttpClient client = HttpClient.newHttpClient();
            String body = String.format("{\"route\":\"q\",\"message\":\"%s\",\"language\":\"english\",\"flash\":0,\"numbers\":\"%s\"}",
                    message.replace("\"", "\\\""), mobile);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.fast2sms.com/dev/bulkV2"))
                    .header("authorization", fast2SmsApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // best-effort: consider non-200 as failure
            return response.statusCode() >= 200 && response.statusCode() < 300;
        }

        // If SMS provider not configured, log OTP and return true (developer mode)
        System.out.println("Generated OTP for " + mobile + ": " + otp);
        return true;
    }

    public Optional<String> verifyOtp(String mobile, String otpProvided, String action) {
        Optional<Otp> found = otpRepository.findFirstByMobileNumberAndActionOrderByExpiryDesc(mobile, action);
        if (found.isEmpty()) return Optional.empty();

        Otp entry = found.orElseThrow(() -> new ValidationException("OTP not found"));
        if (entry.isUsed() || entry.getExpiry().isBefore(Instant.now())) {
            return Optional.empty();
        }

        if (!passwordEncoder.matches(otpProvided, entry.getOtpHash())) {
            return Optional.empty();
        }

        // Mark used
        entry.setUsed(true);
        otpRepository.save(entry);

        // For login or reset_password, generate JWT token which frontend can use as access_token
        String token = jwtTokenProvider.generateToken(mobile);
        return Optional.of(token);
    }
}
