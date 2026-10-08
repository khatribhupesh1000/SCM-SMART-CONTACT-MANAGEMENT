package com.scm.assignment4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.assignment4.client.ContactClient;
import com.scm.assignment4.dto.CreateEmailRequest;
import com.scm.assignment4.exception.DependencyUnavailableException;
import com.scm.assignment4.store.EmailStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@TestPropertySource(properties = "contact.service.url=http://localhost:8081/api/contacts")
@Import({com.scm.assignment4.service.EmailService.class, EmailStore.class, com.scm.assignment4.exception.GlobalExceptionHandler.class})
class EmailControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EmailStore store;
    @MockBean ContactClient contactClient;

    @BeforeEach
    void setup() {
        store.clear();
        when(contactClient.resolveRecipientEmail(anyString())).thenReturn("receiver@example.com");
    }

    @Test
    void createReturns201AndLocation() throws Exception {
        var body = new CreateEmailRequest("user-1", "contact-1", "Hello", "Test body", null);
        mvc.perform(post("/api/v1/emails").header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.recipientEmail").value("receiver@example.com"));
    }

    @Test
    void sameIdempotencyKeyReturnsOriginal() throws Exception {
        var body = new CreateEmailRequest("user-1", "contact-1", "Hello", "Test body", null);
        var first = mvc.perform(post("/api/v1/emails").header("Idempotency-Key", "same-key")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body))).andReturn();
        var second = mvc.perform(post("/api/v1/emails").header("Idempotency-Key", "same-key")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body))).andReturn();
        org.junit.jupiter.api.Assertions.assertEquals(first.getResponse().getContentAsString(), second.getResponse().getContentAsString());
    }

    @Test
    void malformedBodyReturns400() throws Exception {
        mvc.perform(post("/api/v1/emails").header("Idempotency-Key", "bad-json")
                        .contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unknownIdReturns404() throws Exception {
        mvc.perform(get("/api/v1/emails/EML-missing"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void stateConflictAfterCancellationReturns409() throws Exception {
        var body = new CreateEmailRequest("user-1", "contact-1", "Hello", "Test body", null);
        var result = mvc.perform(post("/api/v1/emails").header("Idempotency-Key", "cancel-key")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body))).andReturn();
        String ref = mapper.readTree(result.getResponse().getContentAsString()).get("emailRef").asText();
        var cancel = "{\"status\":\"CANCELLED\"}";
        mvc.perform(post("/api/v1/emails/" + ref + "/cancellation").contentType(MediaType.APPLICATION_JSON).content(cancel))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/emails/" + ref + "/cancellation").contentType(MediaType.APPLICATION_JSON).content(cancel))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void dependencyUnavailableReturns503() throws Exception {
        when(contactClient.resolveRecipientEmail(anyString())).thenThrow(new DependencyUnavailableException("down"));
        var body = new CreateEmailRequest("user-1", "contact-1", "Hello", "Test body", null);
        mvc.perform(post("/api/v1/emails").header("Idempotency-Key", "down-key")
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body)))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.status").value(503));
    }
}
