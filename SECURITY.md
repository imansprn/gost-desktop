# Security policy

## Supported versions

Security fixes are applied to the **default branch** of this repository. There is no separate long-term support policy unless maintainers announce one.

## Reporting a vulnerability

**Please do not** open a public GitHub issue for undisclosed security vulnerabilities.

Preferred options:

1. **GitHub Security Advisories** — use [GitHub’s private vulnerability reporting](https://docs.github.com/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability) for this repository if enabled by the maintainers.
2. Otherwise, contact **repository maintainers** through a private channel they publish on the repository homepage or organization profile.

Include enough detail to reproduce the issue (version or commit, OS, steps, impact).

## Scope

This policy covers **this desktop client**: local configuration handling, encryption of stored credentials, UI logic, and how the app talks to a user-configured GOST HTTP API (including TLS to that server).

It does **not** cover vulnerabilities in **GOST itself** or in your deployment — report those to the upstream [go-gost/gost](https://github.com/go-gost/gost) project or your own security process as appropriate.

## Disclaimer

The software is provided **as-is** under the Apache License 2.0. See [LICENSE](LICENSE) for the full disclaimer of warranty and limitation of liability.
