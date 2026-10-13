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
class ContactShareControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ShareStore store;
    @MockBean ContactServiceClient contacts;

    @BeforeEach void setup() {
        store.clearForTests();
        when(contacts.exists("cnt-1")).thenReturn(true);
    }

    @Test void createReturns201AndLocation() throws Exception {
        mvc.perform(post("/v1/contact-shares").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "a4-create-1")
                .content("{\"contactRef\":\"cnt-1\",\"recipientEmail\":\"friend@example.com\",\"message\":\"hello\"}"))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test void sameKeyReplaysOriginalShare() throws Exception {
        String body = "{\"contactRef\":\"cnt-1\",\"recipientEmail\":\"friend@example.com\",\"message\":\"hello\"}";
        mvc.perform(post("/v1/contact-shares").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key","same")
                .content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.share_id").exists());
        mvc.perform(post("/v1/contact-shares").contentType(MediaType.APPLICATION_JSON).header("Idempotency-Key","same")
                .content(body)).andExpect(status().isCreated()).andExpect(header().string("Idempotency-Replayed","true"));
    }

    @Test void malformedJsonIs400() throws Exception {
        mvc.perform(post("/v1/contact-shares").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key","bad").content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test void unknownShareIs404() throws Exception {
        mvc.perform(get("/v1/contact-shares/no-such-id")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
