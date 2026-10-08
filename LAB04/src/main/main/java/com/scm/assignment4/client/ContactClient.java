package com.scm.assignment4.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.assignment4.exception.ApiException;
import com.scm.assignment4.exception.DependencyUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ContactClient {
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public ContactClient(@Value("${contact.service.url}") String baseUrl, ObjectMapper mapper) {
        this.baseUrl = baseUrl;
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(1500)).build();
    }

    public String resolveRecipientEmail(String contactRef) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl.replaceAll("/$", "") + "/" + contactRef))
                        .timeout(Duration.ofMillis(1500))
                        .header("Accept", "application/json")
                        .GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    JsonNode json = mapper.readTree(response.body());
                    JsonNode email = json.get("email");
                    if (email == null || email.asText().isBlank()) {
                        throw new ApiException(422, "Recipient unavailable", "RECIPIENT_UNAVAILABLE", "The contact has no usable email address.");
                    }
                    return email.asText();
                }
                if (status >= 400 && status < 500) {
                    if (status == 404) {
                        throw new ApiException(422, "Recipient unavailable", "RECIPIENT_UNAVAILABLE", "The referenced contact does not exist.");
                    }
                    throw new ApiException(422, "Recipient rejected", "RECIPIENT_UNAVAILABLE", "The contact service rejected the recipient.");
                }
            } catch (ApiException e) {
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DependencyUnavailableException("Contact service call interrupted", e);
            } catch (IOException | RuntimeException e) {
                if (attempt == 2) throw new DependencyUnavailableException("Contact service unavailable", e);
            }
            try {
                long delay = (long) (100 * Math.pow(2, attempt)) + ThreadLocalRandom.current().nextLong(0, 101);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DependencyUnavailableException("Retry interrupted", e);
            }
        }
        throw new DependencyUnavailableException("Contact service unavailable");
    }
}
