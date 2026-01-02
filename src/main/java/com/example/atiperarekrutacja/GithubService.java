package com.example.atiperarekrutacja;

import com.example.atiperarekrutacja.GithubModels.GithubRepoDto;
import com.example.atiperarekrutacja.ResponseModels.BranchResponse;
import com.example.atiperarekrutacja.ResponseModels.RepositoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GithubService {

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> getUserRepositories(String username) {
        List<GithubRepoDto> repos = githubClient.fetchRepositories(username);

        return repos.stream()
                .filter(repository -> !repository.fork())
                .map(repository -> {
                    List<BranchResponse> branches = githubClient.fetchBranches(repository.owner().login(), repository.name())
                            .stream()
                            .map(branch -> new BranchResponse(branch.name(), branch.commit().sha()))
                            .toList();

                    return new RepositoryResponse(repository.name(), repository.owner().login(), branches);
                })
                .toList();
    }
}
