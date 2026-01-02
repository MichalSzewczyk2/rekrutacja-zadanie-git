package com.example.atiperarekrutacja;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GithubIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    @DisplayName("Should return repository list with branches for existing user (ignoring forks)")
    void shouldReturnRepositoriesWithBranches() {
        // Given
        String username = "adam-test";

        // Stub: Get Repos (1 non-fork, 1 fork)
        wireMock.stubFor(get(urlPathEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "cool-project",
                                    "owner": { "login": "adam-test" },
                                    "fork": false
                                },
                                {
                                    "name": "forked-project",
                                    "owner": { "login": "adam-test" },
                                    "fork": true
                                }
                            ]
                        """)));

        // Stub: Get Branches for the non-fork repo
        wireMock.stubFor(get(urlPathEqualTo("/repos/" + username + "/cool-project/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "main",
                                    "commit": { "sha": "sha-123" }
                                },
                                {
                                    "name": "develop",
                                    "commit": { "sha": "sha-456" }
                                }
                            ]
                        """)));

        // When
        ResponseEntity<List> response = restTemplate.getForEntity("/repositories/" + username, List.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> body = response.getBody();
        assertThat(body).hasSize(1); // Only the non-fork repo

        Map<String, Object> repo = body.get(0);
        assertThat(repo.get("name")).isEqualTo("cool-project");
        assertThat(repo.get("ownerLogin")).isEqualTo("adam-test");

        List<Map<String, Object>> branches = (List<Map<String, Object>>) repo.get("branches");
        assertThat(branches).hasSize(2);
        assertThat(branches.get(0).get("name")).isEqualTo("main");
        assertThat(branches.get(0).get("lastCommitSha")).isEqualTo("sha-123");
    }

    @Test
    @DisplayName("Should return 404 with specific message when user does not exist")
    void shouldReturn404ForNonExistingUser() {
        // Given
        String username = "ghost-user";

        // Stub: 404 from GitHub
        wireMock.stubFor(get(urlPathEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(404)));

        // When
        ResponseEntity<Map> response = restTemplate.getForEntity("/repositories/" + username, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("status")).isEqualTo(404);
        assertThat(body.get("message")).isEqualTo("User " + username + " not found");
    }

}
