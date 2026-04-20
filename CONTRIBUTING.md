# Contributing to GOST Manager

Thank you for your interest in improving this project.

## Development setup

1. Clone the repository.
2. Use **JDK 21** (or a JDK compatible with the Kotlin version in `gradle/libs.versions.toml`).
3. Run the app: `./gradlew :composeApp:run`
4. Compile: `./gradlew :composeApp:compileKotlinJvm`
5. Tests: `./gradlew :composeApp:jvmTest`

## Pull requests

- Keep changes **focused** on one concern when possible.
- Match existing **Kotlin** and **Compose** style in the codebase.
- Ensure `./gradlew :composeApp:compileKotlinJvm` and `./gradlew :composeApp:jvmTest` pass before opening a PR.

## Upstream GOST

Behavior of the GOST proxy itself, wire protocols, and official API semantics are defined by **[go-gost/gost](https://github.com/go-gost/gost)**. This UI aims to follow that API; questions about server-side behavior are often best answered upstream.

## License

By contributing, you agree that your contributions will be licensed under the **Apache License 2.0**, the same as the rest of the project (see [LICENSE](LICENSE)).
