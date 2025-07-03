package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenUrlRequest;
import com.example.urlshortener.dto.ShortenUrlResponse;
import com.example.urlshortener.dto.UrlInfoResponse;
import com.example.urlshortener.service.URLService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class URLController {

    @Autowired
    private URLService urlService;

    /**
     * Shorten URL endpoint
     * POST /shorten
     */
    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {
        try {
            String shortUrl = urlService.shortenURL(request.getLongUrl());

            ShortenUrlResponse response = new ShortenUrlResponse();
            response.setShortUrl(shortUrl);
            response.setOriginalUrl(request.getLongUrl());
            response.setMessage("URL shortened successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ShortenUrlResponse errorResponse = new ShortenUrlResponse();
            errorResponse.setError("Invalid URL: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ShortenUrlResponse errorResponse = new ShortenUrlResponse();
            errorResponse.setError("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Redirect to original URL
     * GET /{shortCode}
     */
    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(@PathVariable String shortCode,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {

        String originalUrl = urlService.getOriginalURL(shortCode);

        if (originalUrl != null) {
            response.sendRedirect(originalUrl);
        } else {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getWriter().write("URL not found");
        }
    }

    /**
     * Get URL info without redirect (for debugging/analytics)
     * GET /info/{shortCode}
     */
    @GetMapping("/info/{shortCode}")
    public ResponseEntity<UrlInfoResponse> getUrlInfo(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalURL(shortCode);

        if (originalUrl != null) {
            UrlInfoResponse response = new UrlInfoResponse(shortCode, originalUrl, "active");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}