# GOST Desktop — End-to-End QA Test Plan

**Baseline:** `087c1e9 feat: harden runtime flows and unify desktop UI`  
**Prepared:** 2026-09-24  
**Basis:** Actual source implementation, not documentation assumptions.

## Scope

Implemented: runtime setup/detection, shell navigation, Dashboard, Tunnels, Chains, Authers, Advanced Objects, Logs, Raw Config, Settings, engine lifecycle, local persistence, config export, keyboard shortcuts, dependency propagation, window close/process shutdown.

Not implemented / not applicable: application-user login/logout, profile, RBAC, session expiration, backend API 4xx/5xx, upload/import, push notifications, URL deep links, browser refresh. `Authers` are GOST auth configuration, not application authentication.

## Execution contract

Every case records: **ID, Feature, Scenario, Priority, Preconditions, Test Data, Starting Screen, Steps, Expected Result, Actual Result, Status**.  
Status: `Not Tested`, `Passed`, `Failed`, `Blocked`.

## Runtime / Engine

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-RT-001 | Runtime Setup | First run with auto-detected GOST | Critical | No saved config; executable in known path | Valid GOST | Launch | Launch → wait detection → verify path → Save & Continue | Runtime valid, settings persist, Dashboard opens | Not Tested | Not Tested |
| QA-RT-002 | Runtime Setup | Manual binary selection | Critical | Auto-detection unavailable | Valid executable + workdir | Setup | Browse → select → configure workdir/auto-start → Save | Shell opens with valid runtime | Not Tested | Not Tested |
| QA-RT-003 | Runtime Validation | Blank/missing/directory/non-executable path | Critical | Setup open | Invalid path variants | Setup | Enter each invalid path → Save | Remain on Setup with visible error | Not Tested | Not Tested |
| QA-RT-004 | Working Directory | Missing/file/read-only workdir | High | Binary valid | Invalid workdirs | Setup | Enter workdir → Save → if accepted, Start tunnel | Validate before use or show recoverable process error | Not Tested | Not Tested |
| QA-RT-005 | Auto-start | Relaunch with autoStart=false | High | desiredRunning=true persisted | 1 tunnel | Launch | Launch → open Tunnels | Engine/tunnel remain stopped | Not Tested | Not Tested |
| QA-RT-006 | Auto-start | Restore only desired-running tunnels | Critical | autoStart=true; mixed intent | 2 desired + 1 stopped | Launch | Launch → wait init → inspect | Only desired-running tunnels start | Not Tested | Not Tested |
| QA-RT-007 | Engine | Stop Engine cancel/confirm then restart | Critical | Engine active; running tunnels | 2 tunnels | Shell | Stop → Cancel → Stop → Confirm → Start | Cancel no-op; confirm stops preserving intent; restart restores | Not Tested | Not Tested |
| QA-RT-008 | App Exit | Close with running tunnel, relaunch | Critical | autoStart=true; tunnel running | 1 tunnel | Shell | Close window → relaunch → inspect | Previously intended-running tunnel restores | Not Tested | Not Tested |

## Navigation / Dashboard

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-NAV-001 | Navigation | Visit all sidebar destinations | Critical | Runtime valid | N/A | Dashboard | Dashboard → Tunnels → Chains → Authers → Advanced → Logs → Config → Settings | All reachable; correct highlight; no blank/loop | Not Tested | Not Tested |
| QA-NAV-002 | Back | Escape from Tunnel/Auth editor | High | Child editor opened | Clean editor | Editor | Press Escape | Return one stack level to correct parent | Not Tested | Not Tested |
| QA-NAV-003 | Unsaved Guard | Navigate away from dirty Tunnel/Chain/Auth/Config | Critical | Dirty editor | Changed field | Editor | Navigate → Cancel → verify edits → retry → Discard | Cancel preserves; Discard navigates exactly once | Not Tested | Not Tested |
| QA-NAV-004 | Shortcuts | Cmd/Ctrl+S, R, Comma, Escape | High | Relevant screen active | Dirty Config/Tunnel | Editor | Exercise all shortcuts | Context-correct actions; no dirty-guard bypass | Not Tested | Not Tested |
| QA-NAV-005 | Sidebar | Collapsed setting persists | Medium | Shell active | true/false | Settings | Toggle → navigate → restart | Saved state applied now and after restart | Not Tested | Not Tested |
| QA-DASH-001 | Dashboard | Open Tunnel Console, cancel/create | High | Runtime valid | N/A | Dashboard | Open wizard → Cancel → reopen → create | Logical return to parent route | Not Tested | Not Tested |
| QA-DASH-002 | Dashboard | Runtime/count reflects actual state | Critical | 0/1/many tunnels; engine on/off | Multiple states | Dashboard | Observe across states | Display matches real engine/tunnel state | Not Tested | Not Tested |

## Tunnel Wizard / CRUD

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-TUN-001 | Tunnel Create | Create first valid tunnel end-to-end | Critical | Empty registry | proxy-one, :8080, HTTP/TCP | Tunnels | Create → Basic → Protocol → Advanced → Save | One config + one registry entry | Not Tested | Not Tested |
| QA-TUN-002 | Validation | Blank/unsafe/duplicate name and bad addr | Critical | Wizard open | blank, whitespace, ../bad, duplicate, blank addr | Basic | Enter each → Next | Blocked with visible error; no overwrite | Not Tested | Not Tested |
| QA-TUN-003 | Wizard Navigation | Next/Back/direct step | High | Wizard open | Valid/invalid Basic | Basic | Jump forward invalid → correct → forward/back | Forward validation works; Back retains values | Not Tested | Not Tested |
| QA-TUN-004 | Advanced Validation | Duplicate metadata/incomplete forwarder/auth/TLS/missing ref | High | Advanced step | Invalid combinations | Advanced | Enter each → Save | Save rejected; no disk mutation | Not Tested | Not Tested |
| QA-TUN-005 | Tunnel Edit | Edit IDLE tunnel | Critical | Existing IDLE | Changed addr/protocol | Tunnels | Edit → change → Save | Config/registry update; remains stopped | Not Tested | Not Tested |
| QA-TUN-006 | Tunnel Edit | Edit RUNNING tunnel | Critical | Engine active; Running | Changed config | Tunnels | Edit → Save | Old process stops; config updates; restarts; intent preserved | Not Tested | Not Tested |
| QA-TUN-007 | Rename | Unique then duplicate rename | Critical | Tunnels A/B | A→C, A→B | Edit Tunnel | Rename unique → verify cleanup → attempt collision | Unique succeeds atomically; collision blocked | Not Tested | Not Tested |
| QA-TUN-008 | Draft Recovery | Resume Draft / Start Fresh | High | Dirty new draft | Partial wizard | New Tunnel | Enter → leave → reopen → Resume; repeat Fresh | Resume restores data/step; Fresh clears | Not Tested | Not Tested |
| QA-TUN-009 | Wizard Chain | Create valid Chain inside Tunnel wizard | High | Advanced step | Valid chain/node | Tunnel Advanced | New Chain → configure → Save → finish tunnel | Chain available/referenced; tunnel saves | Not Tested | Not Tested |
| QA-TUN-010 | Wizard Chain | Duplicate/unsafe Chain | High | Existing chain | duplicate, bad.name, ../bad | Chain dialog | Enter invalid → Save | No overwrite; visible recovery feedback | Not Tested | Not Tested |
| QA-TUN-011 | Delete | Confirmation enabled | Critical | confirmDeletes=true | IDLE + RUNNING | Tunnels | Delete/Cancel; Delete/Confirm | Cancel no-op; Confirm stops/removes once | Not Tested | Not Tested |
| QA-TUN-012 | Delete | Confirmation disabled | High | confirmDeletes=false | Existing tunnel | Tunnels | Delete | No dialog; one deletion | Not Tested | Not Tested |
| QA-TUN-013 | Search | Name/ID/address/error/no-match | Medium | Multiple tunnels | Mixed case queries | Tunnels | Search fields → clear → unmatched | Correct filter; recoverable no-result | Not Tested | Not Tested |


## Process Lifecycle

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-PROC-001 | Start Tunnel | IDLE → RUNNING | Critical | Engine active; valid runtime/config | IDLE tunnel | Tunnels | Start → inspect PID/process | Exactly one process; RUNNING; PID populated; desiredRunning=true | Not Tested | Not Tested |
| QA-PROC-002 | Engine Gate | Start/Restart while engine stopped | High | Engine stopped | IDLE tunnel | Tunnels | Inspect controls → attempt action | Start/Restart unavailable; Start Engine is recovery | Not Tested | Not Tested |
| QA-PROC-003 | Stop Tunnel | Graceful and forced termination | Critical | Running process | Normal + stubborn process | Tunnels | Stop each | Process ends; IDLE; UI remains responsive | Not Tested | Not Tested |
| QA-PROC-004 | Restart Tunnel | Restart running process | Critical | Engine active; Running | N/A | Tunnels | Record PID → Restart | Old exits; one new PID/process; intent preserved | Not Tested | Not Tested |
| QA-PROC-005 | Failure | Missing binary / invalid config / invalid workdir | Critical | Failure injected | Failure variants | Tunnels | Start/Restart | ERROR with actionable message and retry | Not Tested | Not Tested |
| QA-PROC-006 | Unexpected Exit | Process exits itself | High | Tunnel Running | Exit-immediately case | Tunnels | Cause process exit | RUNNING→ERROR; exit info visible | Not Tested | Not Tested |
| QA-PROC-007 | Concurrency | Rapid Start/Stop/Restart | High | Engine active | Rapid clicks | Tunnels | Double Start → rapid Restart → Stop/Start | At most one process/service; consistent state; no freeze | Not Tested | Not Tested |

## Chains

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-CHAIN-001 | Chain CRUD | Create valid chain | High | Chains screen | edge-chain | Chains | Create → Save → select | Template exists and opens | Not Tested | Not Tested |
| QA-CHAIN-002 | Chain Validation | Blank/unsafe/duplicate names | High | Existing chain | blank, bad.name, ../bad, duplicate | Chains | Attempt each create | Rejected; existing data untouched | Not Tested | Not Tested |
| QA-CHAIN-003 | Chain Editor | Hop/node CRUD and reorder | Critical | Chain selected | Multiple hops/nodes | Chain editor | Add/edit/reorder/delete → save/reload | Structure/order persists; no index crash | Not Tested | Not Tested |
| QA-CHAIN-004 | Connector/Dialer | Supported type matrix | High | Chain editor | Implementation-listed values | Chain editor | Iterate representative types → save/reload | DTO/config preserves selections | Not Tested | Not Tested |
| QA-CHAIN-005 | SSH Auth | Password vs private key | High | SSH-like dialer | user/pass and key/passphrase | Chain editor | Configure modes → toggle → save/reload | No stale/conflicting auth state | Not Tested | Not Tested |
| QA-CHAIN-006 | Dirty State | Switch/Discard/sidebar while dirty | Critical | Dirty chain | Changed node | Chains | Switch/cancel → discard → internal Discard → sidebar | No silent data loss | Not Tested | Not Tested |
| QA-CHAIN-007 | Dependency | Update/rename/delete referenced chain | Critical | Running tunnel references A | A + dependent tunnel | Chains | Update A → verify dependent → rename/delete | Update propagates/restarts; rename/delete blocked | Not Tested | Not Tested |
| QA-CHAIN-008 | Corruption | Malformed chain template | High | Corrupt JSON | Malformed file | Chains | Select → attempt repair/delete | Explicit corrupt state + recovery action | Not Tested | Not Tested |

## Auth Rules

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-AUTH-001 | Inline Auth | Multiple valid credentials | High | Authers screen | Several user/password pairs | New Auth Rule | Name → add users → Save → reopen | All pairs persist exactly | Not Tested | Not Tested |
| QA-AUTH-002 | Auth Validation | Missing credential part / invalid name | High | Inline mode | Partial pair, invalid ID | Auth editor | Enter invalid → Save | Save blocked until valid | Not Tested | Not Tested |
| QA-AUTH-003 | Plugin Auth | GRPC/HTTP external plugin | High | Auth editor | Valid/blank endpoint + token | Auth editor | Switch mode → test types → save invalid/valid | Required endpoint enforced; plugin config persists | Not Tested | Not Tested |
| QA-AUTH-004 | Dependency | Update/rename/delete referenced Auth Rule | Critical | Running tunnel references A | Auth A | Authers | Update → verify dependent → rename/delete | Update propagates; rename/delete blocked | Not Tested | Not Tested |
| QA-AUTH-005 | Corruption | Malformed auth template | High | Corrupt JSON | Malformed file | Authers | Load → inspect corrupt row → delete on/off confirm | Invalid state explicit; safe deletion | Not Tested | Not Tested |
| QA-AUTH-006 | Security | Credentials at rest | High | Auth saved | Recognizable password/token | Filesystem | Save → inspect template/config | Behavior matches explicit security policy | Not Tested | Not Tested |


## Advanced Objects

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-ADV-001 | Advanced Navigation | Tabs and search | Medium | Templates exist | Mixed categories | Advanced | Switch all tabs → search → clear | Correct category; query resets on tab change | Not Tested | Not Tested |
| QA-ADV-002 | Visual Editor | Create Bypass/Admission/Resolver/Hosts | High | Advanced screen | Representative valid values | Advanced | Create each type → save → reopen | Values persist; required content enforced | Not Tested | Not Tested |
| QA-ADV-003 | Raw JSON | Invalid then corrected JSON | Critical | Dialog open | Malformed then valid JSON | Raw JSON | Save invalid → correct → save | Visible error; dialog remains; retry succeeds | Not Tested | Not Tested |
| QA-ADV-004 | Mode Transition | Visual ↔ Raw after edits | High | Existing object | Changed values in each mode | Advanced dialog | Edit Visual → Raw → edit Raw → Visual → save | No stale state or silent data loss | Not Tested | Not Tested |
| QA-ADV-005 | Dependency | Update/rename/delete referenced Bypass/Admission | Critical | Running dependent tunnel | Referenced object | Advanced | Update → verify regeneration/restart → rename/delete | Update propagates; rename/delete blocked | Not Tested | Not Tested |
| QA-ADV-006 | Corruption | Malformed Advanced object | High | Corrupt template | Malformed JSON | Advanced | Click object → attempt repair/delete | In-app repair or safe delete path exists | Not Tested | Not Tested |

## Raw Config / Export

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-CFG-001 | Config Empty | No tunnels | Medium | Empty registry | N/A | Config | Open Config | Clear empty state; no crash | Not Tested | Not Tested |
| QA-CFG-002 | Config Save | Valid edit of stopped tunnel | Critical | IDLE tunnel | Valid JSON + changed addr | Config | Select → edit → Save | File/registry update; dirty clears | Not Tested | Not Tested |
| QA-CFG-003 | Running Config | Save while Running | Critical | Engine active; Running | Valid changed JSON | Config | Edit → Save | File updates and process restarts | Not Tested | Not Tested |
| QA-CFG-004 | Config Validation | Malformed JSON | Critical | Existing tunnel | `{broken` | Config | Replace content → Save | Error; original file unchanged; correction possible | Not Tested | Not Tested |
| QA-CFG-005 | Config Schema | JSON-valid but GOST-invalid | Critical | Existing tunnel | {}, unrelated object, empty services | Config | Save each → attempt Start | Invalid GOST structure rejected before known-good replacement | Not Tested | Not Tested |
| QA-CFG-006 | Dirty Config | Switch/reload/sidebar/shortcut while dirty | Critical | Dirty content | N/A | Config | Switch → cancel/confirm → reload → Cmd/Ctrl+R → sidebar | Every destructive transition guarded | Not Tested | Not Tested |
| QA-CFG-007 | Export | Writable and failing destination | High | Tunnel selected | Writable + read-only paths | Config | Export valid → compare → export invalid | Exact valid export; visible failure otherwise | Not Tested | Not Tested |

## Logs

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-LOG-001 | Empty State | No output yet | Medium | No logs | N/A | Logs | Open Logs | Clear “No logs yet” state | Not Tested | Not Tested |
| QA-LOG-002 | Parsing | Structured and alias levels | High | Stream available | debug/info/warn/error + aliases | Logs | Emit representative lines → inspect | Correct normalized level/timestamp/service/message | Not Tested | Not Tested |
| QA-LOG-003 | Filtering | Level + service + search | High | Mixed logs | Multiple services/levels/messages | Logs | Toggle level → service → search → clear | Correct filter intersection and no-match recovery | Not Tested | Not Tested |
| QA-LOG-004 | Auto-scroll/Clear | Auto vs Free and replay clear | Medium | Active logs | Continuous stream | Logs | Auto → Free/scroll → Clear → new event | Free does not jump; Clear removes replay | Not Tested | Not Tested |
| QA-LOG-005 | Retention | 500/1000/5000/10000 + >10k events | High | Buffer configurable | >10,000 events | Logs | Change sizes → flood → filter/search | Retention follows setting; UI stays responsive | Not Tested | Not Tested |

## Settings / Persistence / Recovery

| ID | Feature | Scenario | Priority | Preconditions | Test Data | Starting Screen | Steps | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| QA-SET-001 | Appearance | Accent/sidebar settings persist | Medium | Settings open | All accent values | Settings | Change → restart | Values apply and persist | Not Tested | Not Tested |
| QA-SET-002 | Delete Safety | confirmDeletes on/off globally | High | Deletable objects exist | true/false | Settings | Enable/test all domains → disable/retest | Consistent confirmations; dependency blocking still applies | Not Tested | Not Tested |
| QA-SET-003 | Runtime Settings | Reconfigure runtime after setup | High | Runtime valid | Alternate binary/workdir | Settings | Locate edit/reconfigure action | Intentional recovery/reconfiguration path exists | Not Tested | Not Tested |
| QA-SET-004 | Persistence | Settings file not writable | High | Read-only config location | Any setting change | Settings | Change → restart | Failure visible; UI does not imply durable save | Not Tested | Not Tested |
| QA-RES-001 | Corruption | Malformed config.json | High | Corrupt config | Invalid JSON | Launch | Launch | App remains usable; corruption visible/recoverable | Not Tested | Not Tested |
| QA-RES-002 | Corruption | Malformed services.json | Critical | Existing configs + corrupt registry | Invalid JSON | Launch | Launch → Tunnels → inspect configs | Existing data not silently presented as deleted; recovery exists | Not Tested | Not Tested |
| QA-RES-003 | Atomic Persistence | Write/disk failure during save | Critical | Isolated unwritable destination | Service/template save | Editor | Force failure → Save → inspect prior data | Prior data intact; failure visible; memory/disk consistent | Not Tested | Not Tested |
| QA-RES-004 | Draft I/O | Draft unreadable/unwritable/corrupt | Medium | Isolated draft path | Partial draft | New Tunnel | Edit → leave → reopen → Start Fresh | App remains usable; user not trapped | Not Tested | Not Tested |
| QA-RES-005 | File-name Security | Path traversal/illegal IDs | Critical | Create/rename flow | ../../x, absolute path, slash, backslash, dotted name | Editor | Attempt each identifier | No write outside managed dirs; invalid IDs rejected | Not Tested | Not Tested |

## State Transition Matrix

| Flow | Required Transition |
|---|---|
| Runtime setup | Initial → Detecting → Valid → Shell |
| Runtime invalid | Initial → Validation Error → Correct → Shell |
| Tunnel create | List → New → Validate → Save → List |
| Tunnel edit | List → Edit → Save → List |
| Running tunnel edit | Running → Edit → Stop old → Save → Restart → Running |
| Tunnel delete | List → Confirmation → Stop → Delete → List |
| Process start | IDLE → Starting → RUNNING or ERROR |
| Process stop | RUNNING → Stopping → IDLE |
| Process restart | RUNNING → Stop → Start → RUNNING/ERROR |
| Draft recovery | Dirty → Leave → Reopen → Resume/Start Fresh |
| Template dependency | Template Edit → Regenerate dependent config → Restart running dependents |
| Config save | Dirty → Validate → Save → Optional Restart → Clean |
| Config error | Dirty → Save → Error → Correct → Save → Clean |
| Logs | Empty → Events → Filter → Clear → Empty/New Events |
| Unsaved navigation | Dirty → Navigate → Cancel/Discard → Stay/Move |

## Source-backed dead-end / missing-flow candidates

1. Dashboard runtime text/count is hardcoded.
2. Normal window close calls `stopAll()` without preserving desired-running intent.
3. Working directory is not validated in `ConnectionScreenModel`.
4. Runtime values are read-only in Settings after successful setup.
5. Corrupt Advanced objects cannot enter a repair editor.
6. Corrupt Chain parsing can collapse into a no-selection-like state.
7. Wizard-created Chain save errors are not surfaced inline.
8. Advanced Visual and Raw editors keep independent state and need stale-data testing.
9. Several persistence paths use console-only or silent error handling.
10. Raw Config validates JSON syntax but not required GOST structure.
11. Process stop/restart has no explicit UI busy state while waiting for termination.
12. Service config deletion does not verify `File.delete()` success.
13. Auth credentials/templates are persisted as JSON; `EncryptionUtil` is not integrated.
14. Navigation documentation mentions 401/session recovery that is not implemented.

## Coverage Matrix

| Flow | Happy | Negative | Edge | Error Recovery | Dead-End | Priority |
|---|---|---|---|---|---|---|
| Runtime setup/validation | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Engine lifecycle/auto-start | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Navigation/unsaved state | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Dashboard | ✓ | ✓ | ✓ | — | ✓ | Critical |
| Tunnel CRUD/wizard/search/delete | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Process lifecycle | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Chains/dependencies | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Auth Rules/dependencies | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Advanced Objects/dependencies | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Raw Config/export | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Logs | ✓ | ✓ | ✓ | ✓ | ✓ | High |
| Settings | ✓ | ✓ | ✓ | ✓ | ✓ | High |
| Persistence/corruption | ✓ | ✓ | ✓ | ✓ | ✓ | Critical |
| Path traversal | — | ✓ | ✓ | ✓ | — | Critical |

## Execution rules

- Do not mark a scenario Passed from source inspection alone.
- Unit/integration results satisfy only behavior directly asserted by those tests.
- Native Compose UI behavior requires GUI/manual execution or dedicated UI automation.
- Destructive corruption tests must use isolated temporary directories, never the user’s real `~/.gost-manager`.
- A scenario is `Blocked` when the current environment cannot safely exercise it.
- Source-backed findings remain `Known Issue` until reproduced or fixed.
