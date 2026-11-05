# Implementation audit and reconstruction checkpoint — 5 November 2026

Baseline: main `8774d6d7b6de0c33100389174f728b8cfff99cd8`. This is an implementation checkpoint, not a completion claim.

## Existing architecture and preserved state

The project is Kotlin/Android native Canvas plus Android Views, not Flutter or a WebView. MainActivity owns navigation; Match3Engine owns the pure rules; Match3BoardView animates wave snapshots. GardenSave owns `garden_progress` SharedPreferences. RestorationInventory, RestorationCatalog, RestorationWorksites, RestorationSequence and GardenStory form the 20-task campaign. CharacterSprites/WorkerArt supply sprite poses; GardenVoice owns audio and offline TTS. GameSkin caches the original raster assets. APK package remains `com.lazygarden.game`, the CI debug keystore is unchanged, and versionCode remains 100 + monotonically increasing workflow run number. No player balance, completed task, active construction, or story cursor is reset.

## Concrete audit findings and implemented repairs

- CI run 41788810516: unit tests/build succeeded, device shards 0 and 1 failed. Logs identify three assertion failures. The old-stock fixture saves 2 pipes but asserted 3; it must preserve 2. Two UI assertions still expected a 2-pipe reward after the real economy moved to 3. These assertions now track real saved values; production inventory is not inflated to satisfy the old fixture.
- MaterialObjectiveHud duplicated an obsolete 2/3/4 reward formula while GardenSave actually granted 3/5/6. HUD and win settlement now call one public preview function. The HUD binds directly to the active engine, updates after a swap/booster and has enough vertical space for its two text rows. Its ambient animator pauses with the Activity.
- Inventory always returned to the fountain, including when opened from the home screen. It now remembers its entry route, including Android Back.
- Restorations 8–20 used `(task-3)*3` to index a 12-object atlas. All those source rectangles were outside the atlas. They now use distinct original raster cutaways, with damaged, repairing and finished states, instead of reading invalid pixels. Existing first-seven artwork and progression stay intact.
- Keys and missing-material purchase existed in GardenSave but were unreachable during the first fountain quest. A build-options control now exposes them at the material-collection stage. The cost, star and key guards remain real; duplicate starts cannot spend again.
- Daily rewards and five-level chests existed in save code but had no home-screen claim path. The rewards panel now exposes both. The home HUD includes the real key count and links to special inventory.
- Daily streak continuity previously broke on 31 December because YEAR*1000+DAY was compared with today-1. It now compares with the actual previous calendar date.
- Work-scene voice IDs `nermir` and `mevludin` did not match the server's `nermiri` and `mevludini`; the client now normalizes these canonical IDs.

## Exact worksite and story mapping (zero-based durable task IDs)

| ID | Site | Main worker | Position x,y | Story unlock |
|---|---|---|---|---|
|0|Fountain pump|Rexha|.58,.64|task 1; interactive fountain claim|
|1|Flower bed|Gëzimi|.38,.68|task 2|
|2|Garden path|Greku|.50,.72|task 3|
|3|Family bench|Luli|.75,.66|task 4|
|4|Green lawn|Gëzimi|.27,.67|task 5|
|5|Main entrance|Greku|.50,.53|task 6|
|6|Facade|Beka|.60,.47|task 7|
|7|Interior|Greku|.50,.49|task 8|
|8|Windows|Besi|.66,.48|task 9|
|9|Roof|Nermiri|.58,.37|task 10|
|10|Children's room|Luli|.40,.49|task 11|
|11|Kitchen|Greku|.59,.49|task 12|
|12|Kitchen installations|Rexha / Mevludini|.62,.50|task 13|
|13|Living room|Luli|.46,.50|task 14|
|14|Balcony|Greku|.73,.47|task 15|
|15|Workshop|Luli|.82,.64|task 16|
|16|Bicycle corner|Greku|.16,.67|task 17|
|17|Yard lighting|Mevludini|.69,.61|task 18|
|18|Play area|Greku|.22,.66|task 19|
|19|Celebration garden|Gëzimi|.50,.66|task 20|

Coordinates remain from RestorationWorksites. Native cutaway objects for jobs 8–20 occupy their own shared bounds and actors approach the drawn object. The original 20 task IDs must not be reassigned to a different site: append future gazebo/pond/bridge/greenhouse/orchard/hill/observatory jobs after these IDs, with new catalog/material/story/scene data and migration tests. Those extra regions are not implemented in this checkpoint.

## Live content implementation

`GardenContent` reads `content/manifest.json` from this repository's main branch over HTTPS. It validates schemaVersion, minAppContentApi, positive increasing release version, level ranges, unique IDs, canonical speakers, supported locales, event times and asset references. Levels, localized chapter titles/dialogue, scheduled home announcements, background/logo/map images and story audio can update without native code. Assets require SHA-256, approved HTTPS hosts, 8 MB bounds and image dimension checks. Manifest is replaced using Android AtomicFile only after every referenced asset validates. Failed network/validation preserves the last working release. Download runs away from the main thread; activation waits for the next cold launch. No executable code is downloaded.

New engine saves append the eight LevelSpec integers to the old 268-token state, pinning the level's move and goal definitions (276 tokens). Restore still accepts 138- and 268-token legacy boards against bundled definitions. New live content cannot silently change objectives in an active saved board. The 250-level native ceiling remains; increasing the ceiling/new mechanics/new restoration types requires an APK. Runtime economic costs are intentionally not remotely mutable.

The initial manifest includes three explicit level variants and localized first-chapter completion dialogue in all five languages. Empty event and asset lists mean no new media or event is claimed to have been published.

## Backend verification and remaining infrastructure gap

On 5 November, direct GET /health, GET / and POST /api/voice/speech to the configured `lazygarden-backend.lazisalihi.workers.dev` returned HTTP 404 with Cloudflare error 1042. The repository Worker source has those routes, but that does not prove a working deployed service. No Worker deployment or secret change is claimed.

The configured Apps Script URL answered HTTP 200 with `{"ok":false,"error":"Cannot read properties of null (reading 'getSheetByName')"}` for the app's line request. Drive discovery found the actual **LazyGarden — Dialogë & AI Voice Backend** sheet, ID `1Ea6G6L6eMJ6DLlHDfFgo-ZD0x1LKhLXTWC5aSDaVegI`. It contains localized flower/path/bench/lighting dialogue, audio_url columns currently empty and PENDING voice status; only four character rows were present in the readable result. The repository does not include Code.gs. The error is consistent with an Apps Script deployment that has no active spreadsheet context; the deployed source must be inspected before changing it. Reuse this real sheet ID with openById in that deployment, not a guessed sheet ID. Also reconcile existing scene/line IDs (flower_garden, call_01, etc.) with the app's renderers before claiming remote clips are wired.

## Validation and remaining work

CI validates native compilation, JVM rules and three emulator shards. Added device coverage checks manifest staging/downgrades/failed assets/pinned goals, actual key and material deductions, duplicate daily/chest claims, HUD preview vs settlement, and unique before/work/after room rendering. Current CI results are reported separately after the jobs finish; a build passing alone is not a device pass.

Still required for the full requested game: new outer estate regions and authored connected mysteries; improved unique specialist/family sprites; verified deployed speech and cached real audio; broader device/visual review on Samsung; hand-balanced level playthroughs (250 procedural definitions do not equal 250 balanced levels); localized remaining legacy menus; full scene/line integration with the discovered backend. Full garden navigation and restoration scene persistence across arbitrary process death also needs a wider acceptance pass. This document must not be used as evidence those features already exist.

## Follow-up hardening

Content initialization now happens once per process, so Activity recreation cannot activate a staged release. HUD drawing resets its Paint alpha each frame: screenshot inspection showed that a translucent progress-bar Paint was dimming the entire next frame. SpeechCache stores successfully retrieved Worker clips by speaker/language/text hash with a 4 MB per-clip limit and 20 MB LRU budget. Offline playback checks it before Android TTS; failed backends are cooled down for one minute instead of being retried for every subtitle. This supplies offline reuse only after a real clip has been obtained; it does not claim the currently failing Worker produced audio.

The later-restoration renderer now samples the original `estate_room_atlas.webp`, a 4×4 HD art sheet generated for this project on 5 November. The first 13 cells map exactly to durable task IDs 7–19; the last three are unused art, not implemented restoration claims. Construction uses a weathered version of the same room, localized crack/debris overlays, a progressive restored-area reveal and final cleanup, with the existing animated cast and tool interactions in front. It does not replace gameplay with a static album.
