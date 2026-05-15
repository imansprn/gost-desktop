# GOST Desktop

[![Build](https://github.com/imansprn/gost-desktop/actions/workflows/build.yml/badge.svg)](https://github.com/imansprn/gost-desktop/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/imansprn/gost-desktop?style=flat-square)](https://github.com/imansprn/gost-desktop/releases)
[![Coverage Status](https://coveralls.io/repos/github/imansprn/gost-desktop/badge.svg?branch=main)](https://coveralls.io/github/imansprn/gost-desktop?branch=main)
[![License](https://img.shields.io/github/license/imansprn/gost-desktop?style=flat-square)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-macOS%20%7C%20Windows%20%7C%20Linux-blue?style=flat-square)](#)
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/imansprn/gost-desktop?utm_source=oss&utm_medium=github&utm_campaign=imansprn%2Fgost-desktop&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)

Desktop application for managing a **[GOST](https://github.com/go-gost/gost)** instance through its HTTP API. This project is an **independent community client**; it is not affiliated with or endorsed by the upstream GOST maintainers unless they say otherwise.

## Features

- Connect to a GOST API with saved profiles and optional HTTP Basic credentials (passwords stored encrypted locally).
- Dashboard, services, chains, authers, advanced objects, metrics, logs placeholder, raw configuration editor, and settings.
- Material 3–style UI built with [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) for desktop (JVM).

## Preview

| 1. Setup & Connection | 2. Dashboard Overview | 3. Tunnels Management |
| :---: | :---: | :---: |
| ![Setup](./assets/screenshots/setup.png) | ![Dashboard](./assets/screenshots/dashboard.png) | ![Tunnels](./assets/screenshots/tunnels.png) |

| 4. Advanced Objects | 5. Real-time Logs | 6. Application Settings |
| :---: | :---: | :---: |
| ![Advanced](./assets/screenshots/advance.png) | ![Logs](./assets/screenshots/logs.png) | ![Settings](./assets/screenshots/settings.png) |

## Requirements

- **JDK 21** (recommended) or another JDK compatible with **Kotlin 2.3** (see [gradle/libs.versions.toml](gradle/libs.versions.toml)).
- A running GOST instance reachable over the network, with its web/API enabled.

## Build and run

From the repository root:

```shell
./gradlew :composeApp:run
```

On Windows:

```shell
.\gradlew.bat :composeApp:run
```

Compile only:

```shell
./gradlew :composeApp:compileKotlinJvm
```

Run JVM tests:

```shell
./gradlew :composeApp:jvmTest
```

### Native installers

[composeApp/build.gradle.kts](composeApp/build.gradle.kts) configures `nativeDistributions` (DMG, MSI, Deb). Adjust `packageName` / `packageVersion` there for your fork. Current installer id: `xyz.gobliggg.gost`.

## Privacy and local data

The app stores configuration under your user home:

- **`~/.gost-manager/config.json`** — profiles, app settings, and references to saved credentials.
- **Encrypted passwords** — per-profile secrets are not stored in plaintext in that file (see `EncryptionUtil` in the codebase).

No telemetry or cloud sync is built into this repository; all traffic is between your machine and the GOST server you configure.

## License

Licensed under the **Apache License, Version 2.0** — see [LICENSE](LICENSE).

## Third-party software

This application depends on open-source libraries including Kotlin, Compose Multiplatform, Ktor, Voyager, and others resolved via Gradle. Their licenses apply to those components separately.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Participants are expected to follow the [Code of Conduct](CODE_OF_CONDUCT.md).
