package com.example.atiperarekrutacja;

import java.util.List;

public class ResponseModels {
    public record RepositoryResponse(String name, String ownerLogin, List<BranchResponse> branches) {}
    public record BranchResponse(String name, String lastCommitSha) {}
    public record ErrorResponse(int status, String message) {}
}
