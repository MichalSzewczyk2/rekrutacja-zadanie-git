package com.example.atiperarekrutacja;

import com.example.atiperarekrutacja.ResponseModels.BranchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GithubService {

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<ResponseModels.RepositoryResponse> getUserRepositories(String username) {
        List<GithubModels.GithubRepoDto> repos = githubClient.fetchRepositories(username);

        // Java Stream API sufficient here.
        // N+1 problem exists (1 call for repos, N calls for branches),
        // but accepted per "No Webflux/Optimization" constraints for simplicity.
        return repos.stream()
                .filter(repo -> !repo.fork()) // Filter out forks
                .map(repo -> {
                    List<BranchResponse> branches = githubClient.fetchBranches(repo.owner().login(), repo.name())
                            .stream()
                            .map(branch -> new BranchResponse(branch.name(), branch.commit().sha()))
                            .toList();

                    return new ResponseModels.RepositoryResponse(repo.name(), repo.owner().login(), branches);
                })
                .toList();
    }
}
