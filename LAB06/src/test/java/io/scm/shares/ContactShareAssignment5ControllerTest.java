package io.scm.shares;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContactShareAssignment5ControllerTest {
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
        return request.header("Authorization", TOKEN).header("Accept", "application/json").header("X-Client-ID", "test-client");
    }
    private String createAndGetId() throws Exception {
        String body = mvc.perform(auth(post("/v1/contact-shares")).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key","create-" + System.nanoTime())
                .content("{\"contactRef\":\"cnt-1\",\"recipientEmail\":\"friend@example.com\",\"message\":\"hello\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("share_id").asText();
    }

    @Test void createHas201LocationAndReplayIsIdempotent() throws Exception {
        String body = "{\"contactRef\":\"cnt-1\",\"recipientEmail\":\"friend@example.com\",\"message\":\"hello\"}";
        mvc.perform(auth(post("/v1/contact-shares")).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key","same-key").content(body))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"));
        mvc.perform(auth(post("/v1/contact-shares")).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key","same-key").content(body))
                .andExpect(status().isCreated()).andExpect(header().string("Idempotency-Replayed","true"));
    }
    @Test void conditionalGetReturns304() throws Exception {
        String id = createAndGetId();
        String etag = mvc.perform(auth(get("/v1/contact-shares/" + id)))
                .andExpect(status().isOk()).andReturn().getResponse().getHeader("ETag");
        mvc.perform(auth(get("/v1/contact-shares/" + id)).header("If-None-Match", etag))
                .andExpect(status().isNotModified()).andExpect(content().string(""));
    }
    @Test void staleIfMatchReturns412() throws Exception {
        String id = createAndGetId();
        mvc.perform(auth(patch("/v1/contact-shares/" + id)).contentType(MediaType.APPLICATION_JSON)
                .header("If-Match", "\"stale-etag\"").content("{\"message\":\"change\"}"))
                .andExpect(status().isPreconditionFailed()).andExpect(jsonPath("$.status").value(412));
    }
    @Test void missingAuthorizationReturns401() throws Exception {
        mvc.perform(get("/v1/contact-shares").header("Accept","application/json"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test void malformedJsonReturns400() throws Exception {
        mvc.perform(auth(post("/v1/contact-shares")).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key","malformed-json").content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }
    @Test void missingShareReturns404() throws Exception {
        mvc.perform(auth(get("/v1/contact-shares/not-a-real-share")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }
    @Test void invalidStatusFilterReturns422() throws Exception {
        mvc.perform(auth(get("/v1/contact-shares").param("status","deleted")))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.status").value(422));
    }
    @Test void optionsAdvertisesAllow() throws Exception {
        mvc.perform(options("/v1/contact-shares/any").header("Origin","https://example.org"))
                .andExpect(status().isNoContent()).andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("PATCH")))
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
