package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.urlshortener.dto.UrlMappingDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class URLService {

    @Autowired
    private ShortCodeGenerator shortCodeGenerator;

    @Autowired
    private RedisCacheService cacheService;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final int MAX_RETRY_ATTEMPTS = 5;

    /**
     * Shorten a long URL
     * @param longURL the original URL to shorten
     * @return shortened URL
     */
    public String shortenURL(String longURL) {
        // Validate URL
        if (!isValidUrl(longURL)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        // Check if URL already exists in cache
        String existingShortCode = cacheService.getExistingShortCode(longURL);
        if (existingShortCode != null) {
            return baseUrl + "/" + existingShortCode;
        }

        // Check database for existing mapping
        Optional<UrlMapping> existingMapping = urlMappingRepository.findByLongUrl(longURL);
        if (existingMapping.isPresent()) {
            String shortCode = existingMapping.get().getShortCode();
            // Update cache
            cacheService.cacheUrlMapping(shortCode, longURL);
            cacheService.cacheReverseMapping(longURL, shortCode);
            return baseUrl + "/" + shortCode;
        }

        // Generate new short code
        String shortCode = generateUniqueShortCode();

        // Save to database
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode(shortCode);
        urlMapping.setLongUrl(longURL);
        urlMapping.setCreatedAt(LocalDateTime.now());
        urlMapping.setClickCount(0L);
        urlMappingRepository.save(urlMapping);

        // Cache the mapping
        cacheService.cacheUrlMapping(shortCode, longURL);
        cacheService.cacheReverseMapping(longURL, shortCode);

        return baseUrl + "/" + shortCode;
    }

    /**
     * Get original URL from short code
     * @param shortCode the short code
     * @return original URL
     */
    public String getOriginalURL(String shortCode) {
        // First check cache
        String cachedUrl = cacheService.getOriginalUrl(shortCode);
        if (cachedUrl != null) {
            // Log click event
            logClickEvent(shortCode, cachedUrl);
            return cachedUrl;
        }

        // Check database
        Optional<UrlMapping> mapping = urlMappingRepository.findByShortCode(shortCode);
        if (mapping.isPresent()) {
            UrlMapping urlMapping = mapping.get();
            String longUrl = urlMapping.getLongUrl();

            // Update cache
            cacheService.cacheUrlMapping(shortCode, longUrl);

            // Log click event
            logClickEvent(shortCode, longUrl);

            return longUrl;
        }

        return null; // URL not found
    }

    /**
     * Generate a unique short code
     */
    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;

        do {
            shortCode = shortCodeGenerator.generateShortCode();
            attempts++;

            if (attempts > MAX_RETRY_ATTEMPTS) {
                throw new RuntimeException("Unable to generate unique short code after " + MAX_RETRY_ATTEMPTS + " attempts");
            }
        } while (cacheService.existsShortCode(shortCode) || urlMappingRepository.existsByShortCode(shortCode));

        return shortCode;
    }

    /**
     * Log click event to Kafka
     */
    private void logClickEvent(String shortCode, String longUrl) {
        try {
            // Increment click count in database asynchronously
            urlMappingRepository.incrementClickCount(shortCode);

            // Create click event
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setShortCode(shortCode);
            clickEvent.setLongUrl(longUrl);
            clickEvent.setTimestamp(LocalDateTime.now());
            clickEvent.setUserAgent(""); // Can be populated from request
            clickEvent.setIpAddress(""); // Can be populated from request

            // Send to Kafka
            kafkaProducerService.sendClickEvent(clickEvent);
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to log click event: " + e.getMessage());
        }
    }

    /**
     * Basic URL validation
     */
    private boolean isValidUrl(String url) {
        return url != null &&
                !url.trim().isEmpty() &&
                (url.startsWith("http://") || url.startsWith("https://"));
    }
}