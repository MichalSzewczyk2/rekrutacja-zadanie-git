package com.example.atiperarekrutacja;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GithubIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.url", wireMock::baseUrl);
    }

    @Test
    @DisplayName("Should return repository list with branches for existing user (ignoring forks)")
    void shouldReturnRepositoriesWithBranches() {

        String username = "user-test";
        wireMock.stubFor(get(urlPathEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            [
                                {
                                    "name": "cool-project",
                                    "owner": { "login": "user-test" },
                                    "fork": false
                                },
                                {
                                    "name": "forked-project",
                                    "owner": { "login": "user-test" },
                                    "fork": true
                                }
                            ]
                        """)));

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

        webTestClient.get().uri("/repositories/" + username)
                .exchange() // Wykonaj request
                .expectStatus().isOk() // Sprawdź status HTTP 200
                .expectBody()
                // Weryfikacja: oczekujemy 1 repozytorium (bo drugie to fork)
                .jsonPath("$.length()").isEqualTo(1)

                // Sprawdzamy pola pierwszego obiektu
                .jsonPath("$[0].name").isEqualTo("cool-project")
                .jsonPath("$[0].ownerLogin").isEqualTo("adam-test")

                // Sprawdzamy zagnieżdżoną listę branches
                .jsonPath("$[0].branches.length()").isEqualTo(2)
                .jsonPath("$[0].branches[0].name").isEqualTo("main")
                .jsonPath("$[0].branches[0].lastCommitSha").isEqualTo("sha-123");
    }

    @Test
    @DisplayName("Should return 404 with specific message when user does not exist")
    void shouldReturn404ForNonExistingUser() {
        String username = "ghost-user";

        wireMock.stubFor(get(urlPathEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get().uri("/repositories/" + username)
                .exchange()
                .expectStatus().isNotFound() // Sprawdź status 404
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("User " + username + " not found");
    }

}
