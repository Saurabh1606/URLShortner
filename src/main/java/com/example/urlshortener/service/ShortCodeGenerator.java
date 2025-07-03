package com.example.urlshortener.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ShortCodeGenerator {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SHORT_CODE_LENGTH = 6;
    private final Random random = new Random();


    public String generateShortCode() {
        StringBuilder shortCode = new StringBuilder();
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(BASE62_CHARS.length());
            shortCode.append(BASE62_CHARS.charAt(randomIndex));
        }
        return shortCode.toString();
    }


    public String encodeToBase62(long number) {
        if (number == 0) return "0";

        StringBuilder result = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % 62);
            result.append(BASE62_CHARS.charAt(remainder));
            number /= 62;
        }
        return result.reverse().toString();
    }


    public long decodeFromBase62(String base62String) {
        long result = 0;
        for (char c : base62String.toCharArray()) {
            result = result * 62 + BASE62_CHARS.indexOf(c);
        }
        return result;
    }
}
