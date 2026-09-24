# GOST Desktop — QA Execution Report

**Execution date:** 2026-09-24  
**Baseline commit:** `087c1e9 feat: harden runtime flows and unify desktop UI`  
**Plan:** `docs/end-to-end-qa-plan.md`

## Executive Result

| Check | Result |
|---|---|
| `git diff --check` | PASS |
| JVM compilation | PASS |
| Full JVM test suite, forced rerun | PASS |
| Test cases executed by JVM suite | **80** |
| Passed | **80** |
| Failed | **0** |
| Errors | **0** |
| Skipped | **0** |
| GOST runtime available | PASS — `gost 3.2.6 (go1.25.4 darwin/arm64)` |
| Temporary valid GOST config starts | PASS |
| Graceful GOST termination | PASS |
| Temporary malformed config rejected | PASS — exit code 1 |
| Native end-to-end GUI walkthrough | NOT EXECUTED — no native GUI automation/control available in this session |

The automated baseline is green. This does **not** mean every end-to-end scenario in the master plan is Passed: many cases require native UI interaction, real shell navigation, rapid clicks, file chooser interaction, window close/relaunch, or visual verification.

## Commands Executed

```bash
git diff --check
./gradlew :composeApp:compileKotlinJvm --no-daemon
./gradlew :composeApp:jvmTest --no-daemon
./gradlew :composeApp:jvmTest --rerun-tasks --no-daemon
```

Forced rerun result:

```text
BUILD SUCCESSFUL
16 actionable tasks: 16 executed
80 tests
80 passed
0 failed
0 errors
0 skipped
```

## Automated Test Inventory

| Test Class | Tests | Result | Primary Coverage |
|---|---:|---|---|
| ComposeAppDesktopTest | 1 | PASS | Desktop test harness smoke |
| GostDtosTest | 3 | PASS | Chain/Bypass/Hosts serialization |
| AppStateTest | 7 | PASS | Settings update, pending route, engine disconnect semantics |
| ConfigBuilderTest | 8 | PASS | Config formatting, template CRUD, path traversal, raw GOST schema validation, owner-only sensitive-file permissions |
| EncryptionUtilTest | 2 | PASS | Encryption roundtrip/randomization utility |
| LocalConfigRepositoryTest | 3 | PASS | Save/load, missing config, corrupt config fallback |
| ProcessManagerTest | 6 | PASS | Engine gate, stop/stopAll, STARTING-service drain, stopped-engine restart |
| ServiceRegistryTest | 5 | PASS | CRUD, state update, disk persistence |
| ServiceWizardDraftStoreTest | 4 | PASS | Draft save/load/clear/full-field roundtrip |
| NavigationPolicyTest | 3 | PASS | Dynamic new routes + sidebar parent mapping |
| ConnectionScreenModelTest | 3 | PASS | Setup state, valid save/connect, invalid binary rejection |
| DashboardScreenTest | 1 | PASS | Dashboard renders |
| LogsScreenModelTest | 3 | PASS | Parsing/filtering, clear/auto-scroll, service list |
| LogsScreenTest | 1 | PASS | Logs screen renders |
| ServiceFormScreenModelTest | 7 | PASS | State updates, steps, edit load, preview/save, metadata/forwarders, wizard Chain create |
| ServicesScreenModelTest | 2 | PASS | Process-action dispatch, delete flow |
| ServicesScreenTest | 1 | PASS | Tunnels screen renders |
| SettingsScreenTest | 2 | PASS | Settings render + sidebar-default toggle |
| SaaSButtonTest | 1 | PASS | Button click/text |
| SaaSInputsTest | 1 | PASS | Text input |
| SaaSLayoutsTest | 4 | PASS | Background, dialog, header, toggle group |
| ConfigVisualTransformationTest | 2 | PASS | JSON/YAML highlighting |
| FormattersTest | 4 | PASS | Utility formatting |
| ValidatorsTest | 6 | PASS | Address/CIDR/IPv4/pattern/URL/service-name validation |

## Runtime Smoke Test

The runtime smoke used an isolated `/tmp/gost-qa.*` directory. It did not touch `~/.gost-manager`.

### Valid configuration

Temporary HTTP/TCP service was generated on an ephemeral localhost port.

**Actual Result:**

```text
valid_config_process=RUNNING
graceful_stop=PASSED
```

**Status:** PASS

This validates the installed GOST binary and a minimal generated configuration shape. It does not by itself validate the full application ProcessManager UI flow.

### Invalid configuration

Temporary malformed JSON:

```text
{broken
```

**Actual Result:**

```text
invalid_config_exit=1
fatal: While parsing config: invalid character 'b' looking for beginning of object key string
```

**Status:** PASS

GOST itself rejects malformed config predictably.

## QA Plan Coverage From This Execution

### Passed at automated model/data level

The following plan areas have direct automated evidence, but some still need GUI E2E validation:

| Plan ID / Area | Automated Evidence | Execution Assessment |
|---|---|---|
| QA-RT-003 binary path validation | ConnectionScreenModelTest | PASS at model level |
| QA-RT-007 engine stop semantics | AppStateTest + ProcessManagerTest | PASS at state/process-model level |
| QA-NAV-001 dynamic route policy | NavigationPolicyTest + render tests | PARTIAL — shell clicking not executed |
| QA-NAV-005 sidebar default persistence behavior | SettingsScreenTest + AppStateTest | PARTIAL |
| QA-TUN-003 wizard next/back state | ServiceFormScreenModelTest | PASS at model level |
| QA-TUN-005 edit-state loading | ServiceFormScreenModelTest | PARTIAL |
| QA-TUN-009 create Chain from wizard | ServiceFormScreenModelTest | PASS at model level |
| QA-PROC-002 engine gate | ProcessManagerTest | PASS at process-manager level |
| QA-PROC-003 stop/stopAll semantics | ProcessManagerTest + real GOST graceful-stop smoke | PARTIAL |
| QA-PROC-004 restart while engine stopped | ProcessManagerTest | PASS for stopped-engine branch |
| QA-LOG-002 parsing | LogsScreenModelTest | PASS |
| QA-LOG-003 filters | LogsScreenModelTest | PASS at model level |
| QA-LOG-004 clear/auto-scroll state | LogsScreenModelTest | PASS at model level |
| QA-SET-001 setting update/sidebar default | AppStateTest + SettingsScreenTest | PARTIAL |
| QA-RES-001 corrupt local config fallback | LocalConfigRepositoryTest | PASS for implemented fallback behavior |
| QA-RES-004 draft storage mechanics | ServiceWizardDraftStoreTest | PASS at storage level |
| QA-RES-005 path traversal prevention | ConfigBuilderTest | PASS |

### Not executed end-to-end

These remain `Not Tested` or `Blocked` because they require native UI control, destructive filesystem simulation, or window lifecycle automation:

- Full first-run Connection Setup interaction/file chooser.
- Working-directory UI validation.
- Auto-start across a real application close/relaunch.
- Stop Engine confirmation dialog and restoration from the shell.
- Sidebar click navigation and unsaved confirmation dialogs.
- Keyboard shortcut behavior at window level.
- Dashboard live-state verification.
- Full Tunnel wizard with all fields and template selectors.
- Running Tunnel edit/save/restart through UI.
- Rename collision through UI.
- Resume Draft / Start Fresh dialog interaction.
- Tunnel search and delete confirmation UX.
- Rapid multi-click race testing.
- Chain visual CRUD/reorder and SSH field transitions.
- Auth Rule full-screen visual editor and plugin mode.
- Advanced Visual/Raw editor transitions.
- Raw Config file chooser export.
- Log auto-scroll behavior under high-volume real output.
- Settings persistence after full app restart.
- Read-only/disk-full user-visible error handling.
- Corrupt template repair paths.
- Credential-at-rest policy acceptance.

## Post-Fix Issue Verification

The source-backed issues discovered during the QA audit were addressed before this final rerun.

| Issue | Resolution | Verification |
|---|---|---|
| KI-001 Dashboard hardcoded runtime data | Dashboard now derives engine state, configured tunnel count, and active tunnel count from AppState/ServiceRegistry. | Compile + Dashboard render test PASS; native visual verification still required. |
| KI-002 Window close loses desired-running intent | Window close and JVM shutdown now call `stopAll(preserveIntent = true)`. | Source verification + ProcessManager regression suite PASS. |
| KI-003 Working Directory not validated | Setup validates existence, directory type, and accessibility before connect. | ConnectionScreenModelTest PASS. |
| KI-004 Runtime reconfiguration missing | Settings now provides Reconfigure Runtime with cancel/save recovery and engine state restoration. | Compile/model verification PASS; native flow still requires GUI execution. |
| KI-005 Corrupt Advanced object dead-end | Unparseable objects open directly in Raw JSON repair mode. | Compile/source verification PASS; GUI repair interaction still manual. |
| KI-006 Corrupt Chain recovery ambiguous | Chains now exposes explicit invalid-JSON banner, repair editor, discard, and Save Repair flow. | Compile/source verification PASS; GUI interaction still manual. |
| KI-007 Wizard-created Chain errors hidden | Wizard now validates name/duplicate state and surfaces failures via form error + snackbar. | ServiceFormScreenModelTest PASS. |
| KI-008 Advanced Visual/Raw divergence | Visual draft and Raw JSON now synchronize in both directions; identifier updates mutate draft synchronously. | Compile/source verification PASS. |
| KI-009 Persistence failures silent | Settings, registry, and draft persistence now use atomic writes/backups and report user-visible persistence issues. | Repository/model tests PASS; destructive read-only GUI scenario remains manual. |
| KI-010 Raw Config lacks schema validation | Raw Config now requires exactly one service with non-empty name/handler/listener and enforces service name = selected tunnel ID; save rolls back on registry failure. | ConfigBuilderTest PASS. |
| KI-011 No process busy state | STARTING/STOPPING states are modeled and UI actions are disabled during transition; Stop Engine immediately gates new starts and drains STARTING services. | ProcessManagerTest PASS. |
| KI-012 Config delete ignores failure | Service-config deletion now throws on failed filesystem delete and callers report cleanup/persistence issues. | Compile/model verification PASS. |
| KI-013 Credential-at-rest hardening | Managed settings, registry, service configs, templates, and drafts are restricted to owner-only permissions; existing managed config/template trees are hardened on initialization. Plaintext remains necessary for GOST runtime consumption. | POSIX owner-only ConfigBuilderTest PASS. |
| KI-014 Stale session/401 documentation | NavigationPolicy now documents local runtime setup/reconfiguration and explicitly states there is no authenticated session/401 flow. | Source verification PASS. |

### Residual validation

The code-level issues above are fixed, but native GUI execution is still required for window close/relaunch, file chooser behavior, visual repair flows, rapid user interaction, and destructive filesystem-error presentation.

## Build Warnings Observed During Forced Rerun

The test run succeeded but emitted non-failing warnings:

- Redundant `Json` instance creation in ConfigBuilder.
- Deprecated `TabRow` API.
- Deprecated non-auto-mirrored icons.
- Deprecated `String.capitalize()`.
- Deprecated tooltip position provider.
- Several unnecessary null-safe/Elvis/non-null operations.

These are not QA failures, but should remain in technical-debt tracking.

## Current Release Assessment

**Automated regression baseline:** GREEN — 80/80 JVM tests passed.  
**Native E2E readiness:** INCOMPLETE — manual/native UI execution is still required for GUI-only and window/filesystem interaction paths.  
**Critical source-backed blockers from this audit:** Resolved in the current working tree; remaining risk is execution coverage rather than an open code finding from KI-001…KI-014.

No production user data was modified during execution.
