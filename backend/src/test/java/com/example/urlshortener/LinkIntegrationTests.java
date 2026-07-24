package com.example.urlshortener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests over the running Spring context, exercising security,
 * persistence, and the redirect path together. The seeded demo users
 * {@code alice} and {@code bob} are used as two distinct owners.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LinkIntegrationTests {

    private static final String LONG_URL = "https://example.com/some/page";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void activeLinkRedirectsWith302() throws Exception {
        String shortCode = createLink("alice", "alice-password", LONG_URL);

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", LONG_URL));
    }

    @Test
    void unknownOrInactiveShortCodeReturns404() throws Exception {
        // Unknown code.
        mockMvc.perform(get("/Zz9Zz9Z"))
                .andExpect(status().isNotFound());

        // Deactivated code.
        String shortCode = createLink("alice", "alice-password", LONG_URL);
        mockMvc.perform(post("/api/links/" + shortCode + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bearer("alice", "alice-password")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isNotFound());
    }

    @Test
    void crossUserActionReturns403() throws Exception {
        String shortCode = createLink("alice", "alice-password", LONG_URL);
        String bobToken = bearer("bob", "bob-password");

        // bob does not own alice's link.
        mockMvc.perform(post("/api/links/" + shortCode + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bobToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/links/" + shortCode + "/stats")
                        .header(HttpHeaders.AUTHORIZATION, bobToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                // The message must not reveal whether the account exists.
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void managementApiRejectsMissingAndInvalidTokens() throws Exception {
        mockMvc.perform(get("/api/links"))
                .andExpect(status().isUnauthorized());

        // Well-formed header, but not a token this service signed.
        mockMvc.perform(get("/api/links")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Signs in and returns a ready-to-use {@code Authorization} header value.
     */
    private String bearer(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("tokenType").asText() + " " + body.get("accessToken").asText();
    }

    private String createLink(String username, String password, String longUrl) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/links")
                        .header(HttpHeaders.AUTHORIZATION, bearer(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\":\"" + longUrl + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("shortCode").asText();
    }
}
