package io.scm.shares.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ContactServiceClient {
    private final RestTemplate restTemplate;
    private final String contactServiceUrl;

    public ContactServiceClient(RestTemplateBuilder builder,
            @Value("${contact.service.url:http://localhost:8082/v1/contacts}") String contactServiceUrl) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(1200))
                .setReadTimeout(Duration.ofMillis(1800)).build();
        this.contactServiceUrl = contactServiceUrl.replaceAll("/+$", "");
    }

    public boolean exists(String contactRef) {
        String url = contactServiceUrl + "/" + contactRef;
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                return response.getStatusCode().is2xxSuccessful();
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404) return false;
                throw new DependencyException(502, "Contact Service rejected the request.");
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().is4xxClientError()) {
                    throw new DependencyException(502, "Contact Service returned a client error.");
                }
                if (attempt == maxAttempts) throw new DependencyException(503, "SCM Contact Service is temporarily unavailable.");
            } catch (ResourceAccessException e) {
                if (attempt == maxAttempts) throw new DependencyException(503, "SCM Contact Service is temporarily unavailable.");
            }
            try {
                long base = 100L * (1L << (attempt - 1));
                Thread.sleep(base + ThreadLocalRandom.current().nextLong(30, 100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DependencyException(503, "Contact validation was interrupted.");
            }
        }
        throw new DependencyException(503, "SCM Contact Service is temporarily unavailable.");
    }

    public static class DependencyException extends RuntimeException {
        private final int status;
        public DependencyException(int status, String message) { super(message); this.status = status; }
        public int getStatus() { return status; }
    }
}
