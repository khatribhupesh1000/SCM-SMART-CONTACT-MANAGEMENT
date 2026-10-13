package io.scm.shares;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scm.shares.service.ContactServiceClient;
import io.scm.shares.store.ShareStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Assignment6ProblemDetailsTest {
    @Autowired MockMvc mvc;
    @Autowired ShareStore store;
    @MockBean ContactServiceClient contacts;
    private static final String TOKEN = "Bearer scm-assignment5-demo-token";

    @BeforeEach void setup() {
        store.clearForTests();
        when(contacts.exists("cnt-1")).thenReturn(true);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder auth(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        return request.header("Authorization", TOKEN).header("Accept", "application/json")
                .header("X-Client-ID", "assignment6-test");
    }

    @Test void createReturns201AndLocation() throws Exception {
        mvc.perform(auth(post("/v1/contact-shares")).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "a6-create")
                .content("{\"contactRef\":\"cnt-1\",\"recipientEmail\":\"friend@example.com\",\"message\":\"hello\"}"))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/v1/contact-shares/")))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test void invalidFieldsAreCollectedInOne422Problem() throws Exception {
        String response = mvc.perform(auth(post("/v1/contact-shares")).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "a6-invalid")
                .content("{\"contactRef\":\"\",\"recipientEmail\":\"not-an-email\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")))
                .andExpect(jsonPath("$.type").value("invalid-share-request"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errors").isArray())
                .andReturn().getResponse().getContentAsString();
        JsonNode errors = new ObjectMapper().readTree(response).get("errors");
        org.junit.jupiter.api.Assertions.assertTrue(errors.size() >= 2,
                "Expected both contactRef and recipientEmail validation errors");
    }

    @Test void malformedJsonIs400ProblemDetails() throws Exception {
        mvc.perform(auth(post("/v1/contact-shares")).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "a6-malformed").content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")))
                .andExpect(jsonPath("$.type").value("invalid-request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test void missingShareReturns404ProblemDetails() throws Exception {
        mvc.perform(auth(get("/v1/contact-shares/missing-a6")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("share-not-found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test void xmlAcceptReturns406WhenXmlIsNotSupported() throws Exception {
        mvc.perform(post("/v1/contact-shares").header("Authorization", TOKEN)
                .header("Accept", "application/xml").header("X-Client-ID", "assignment6-test"))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")))
                .andExpect(jsonPath("$.type").value("not-acceptable"));
    }

    @Test void unavailableContactServiceReturns503AndRetryAfter() throws Exception {
        when(contacts.exists("cnt-down")).thenThrow(new ContactServiceClient.DependencyException(
                503, "SCM Contact Service is temporarily unavailable."));
        mvc.perform(auth(post("/v1/contact-shares")).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "a6-down")
                .content("{\"contactRef\":\"cnt-down\",\"recipientEmail\":\"friend@example.com\",\"message\":\"hello\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.type").value("dependency-unavailable"));
    }

    @Test void missingAuthorizationReturns401ProblemDetails() throws Exception {
        mvc.perform(get("/v1/contact-shares").header("Accept", "application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")))
                .andExpect(jsonPath("$.status").value(401));
    }
}
