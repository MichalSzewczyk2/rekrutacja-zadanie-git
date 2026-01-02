# GitHub Proxy API

A simple proxy application that exposes GitHub repositories for a specific user, filtering out forks and including branch details.

## Stack
* **Java 25** (Preview)
* **Spring Boot 4.0.1** (Conceptual / Latest Stable)
* **Gradle Kotlin DSL**

## Requirements
* Valid Java JDK installed.

## Endpoints

### Get User Repositories
`GET /repositories/{username}`

**Success Response (200 OK):**
```json
[
  {
    "name": "repo-name",
    "ownerLogin": "username",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "1234567890abcdef"
      }
    ]
  }
]
