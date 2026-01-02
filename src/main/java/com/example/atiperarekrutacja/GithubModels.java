package com.example.atiperarekrutacja;

public class GithubModels {

    public record GithubRepoDto(String name, Owner owner, boolean fork) {
        record Owner(String login) {}
    }

    public record GithubBranchDto(String name, Commit commit) {
        record Commit(String sha) {}
    }

}
