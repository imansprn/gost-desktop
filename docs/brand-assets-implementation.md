# GOST brand asset implementation

## Goal and source
Apply the supplied GOST brand board to existing desktop surfaces while preserving compact layouts and readable controls. The current PNG is an image-generated extraction of the reference, not an exact vector original. Concept icons are native Compose adaptations of the board.

## Asset mapping
| Asset | Implementation |
| --- | --- |
| Colored mark | Existing sidebar, dashboard and setup; shared PNG painter |
| Wordmark and Tunnel Freely tagline | Reusable accessible text lockup in setup and Settings > About |
| Simple. Powerful. Private. | Brand slogan in About, alongside a factual note that transport protection depends on configuration |
| Monochrome black/white | Shared tint variant using theme foreground; available for surfaces requiring a single color |
| Gray variant | Shared muted variant for future noninteractive contexts; never used to indicate engine status |
| Tunnel / Routing / Connection / Privacy | Four compact, labeled concept explanations in About, with native vector drawings |
| Purple / blue / teal palette | Brand-only gradient tokens; existing appearance preferences and semantic status colors retained |
| App tile | PNG, ICNS and ICO packaging assets, generated from the shared PNG |
| Safe area and size chart | Existing normalization to an 800px mark inside 1024px canvas; validate sidebar 44/56dp and dashboard 120dp |
| Tray/menu bar | Deferred: no tray exists today; adding it requires lifecycle, quit and visibility decisions |
| Board mockups and palette swatches | Design references; not inserted as application content |

## Implementation sequence
1. Add shared brand components with colored/themed/muted marks, native wordmark text and tagline.
2. Replace setup header branding, retain clear setup instructions and scrollable form.
3. Add colored lockup, slogan, and four compact explanatory rows to About; preserve version/runtime information.
4. Compile and inspect changes for resource collisions and obsolete references.

## Acceptance checks
- Setup remains usable in its 520x640 window; branding is inside the existing scroll area.
- About content scrolls; labels and explanations can wrap.
- Image has a meaningful description when standalone; decorative mark in text lockup has no redundant description.
- Monochrome logo follows semantic foreground colors; no hardcoded white on light surfaces.
- Brand gradients are limited to illustrative icons, never used to convey error/success.
- Privacy copy does not promise that all tunnels are encrypted or anonymous.
- Resource basenames remain unique, PNG alpha and proportions are retained.
- Existing setup save/cancel and runtime controls remain operational.

## Validation
Implemented on 2026-09-25:
- Added `GostBrand.kt` with native text lockup, colored/monochrome/muted variants, brand palette and four vector concept icons.
- Setup uses the colored lockup; its form stays in the existing scroll container.
- Settings > About uses the colored lockup, slogan, concept explanations and existing runtime/version rows.
- `./gradlew :composeApp:compileKotlinJvm --no-daemon`: BUILD SUCCESSFUL.
- `git diff --check`: passed.
- Live visual checks in the running desktop app and light/dark comparisons remain unverified. Gray variant is available but has no forced placement. Tray integration is deferred as described above.
- No commit or push performed.

## Sidebar integration
- Tunnels uses the Tunnel concept icon; Chains uses Routing; Authers uses Privacy.
- The expanded runtime panel uses the Connection icon beside its state indicator. There is no separate Connection route.
- Navigation icons use semantic foreground colors for idle/hover states and the brand gradient when selected; sizes stay on the existing icon token.
- Accessible labels and both expanded/collapsed navigation use the same icon mapping.
- Other navigation icons and engine power controls retain their existing meanings.

## About logo consistency
About uses the full-color logo, matching setup, dashboard and sidebar. The monochrome variant remains available but is not selected in About.
