package com.example.atiperarekrutacja;

import com.example.atiperarekrutacja.GithubModels.GithubRepoDto;
import com.example.atiperarekrutacja.GithubModels.GithubBranchDto;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

public class GithubClient {

    private final RestClient restClient;

    GithubClient(RestClient restClient) {
        this.restClient = restClient;
    }

    List<GithubRepoDto> fetchRepositories(String username) {
        try {
            return restClient.get()
                    .uri("/users/{username}/repos", username)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("User " + username + " not found");
        }
    }

    List<GithubBranchDto> fetchBranches(String owner, String repoName) {
        try {
            return restClient.get()
                    .uri("/repos/{owner}/{repo}/branches", owner, repoName)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            return List.of();
        }
    }
}
