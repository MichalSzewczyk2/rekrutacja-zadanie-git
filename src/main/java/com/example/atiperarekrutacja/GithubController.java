package com.example.atiperarekrutacja;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GithubController {

    private final GithubService githubService;

    GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/{username}")
    ResponseEntity<List<ResponseModels.RepositoryResponse>> getUserRepositories(@PathVariable String username) {
        return ResponseEntity.ok(githubService.getUserRepositories(username));
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ResponseModels.ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseModels.ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    // Catch-all for other potential issues (e.g. rate limits) not explicitly requested but good practice
    @ExceptionHandler(Exception.class)
    ResponseEntity<ResponseModels.ErrorResponse> handleGeneralError(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
}
