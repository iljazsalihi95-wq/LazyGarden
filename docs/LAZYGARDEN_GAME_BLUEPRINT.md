# LazyGarden — Game Design Blueprint (living document)
Status: design specification, NOT a claim of implemented features. Repository: iljazsalihi95-wq/LazyGarden.

## Player's core loop
1. Explore the living estate: a resident asks for help or reveals a clue.
2. Choose a specific construction site; preview before/after and required work.
3. Play Match-3 with varied board objectives. Win grants coins, star and targeted construction materials.
4. Build via earned materials, a key bypass, or purchased missing materials; make a visible world change.
5. Hear character dialogue and uncover the next plot beat. Every completed task must produce a visible improvement.
6. Unlock an optional surprise or minigame, then move to the next site.

## Estate map (schematic, not a finished art asset)
```
                         [10] Observatory / secret letters
                                  |
                   [9] Hill path -- [8] Orchard
                         |             |
 [7] Old greenhouse -- [6] Workshop -- [5] Gate and bridge
          |                                   |
 [4] Pond and ducks -- [3] Gazebo -- [2] Flowerbeds
                                  |
                         [1] Fountain / family courtyard
```
The numbered sites are design targets, NOT assertions about the current RestorationInventory order. Align the actual site order and localized names before shipping.

## Milestone rhythm
- Every level: coins, meaningful board objective, visible feedback and progress towards a named site.
- Every 2–3 levels: a new construction step or scene beat; never gate a tiny visible change behind 13 repetitive wins.
- Every 5 levels: named story landmark, visitor, optional minigame, mystery or surprise; not automatic meaningless currency inflation.
- Every 25 levels: chapter climax and new map region.
- Every 50 levels: distinctive mechanical twist, new region and character arc.
Milestones should be driven by explicit data, not hardcoded rewards disconnected from spendable systems.

## Economy
- Stars: story/restoration actions; do not sell the ability to understand the story.
- Coins: buy missing construction materials, decoration and helpful boosters.
- Keys: alternative path that skips material collection for one restoration, but never erases story progression.
- Materials: earned from board objectives and visibly consumed by construction.
- Chests: only if contents, key costs, unlocks and UI are fully connected and tested.
- Purchases (future): optional cosmetic or convenience products with clear prices; do not introduce until free progression is genuinely enjoyable.

## World and cast
- Preserve canonical character IDs and continuity in GardenStory.
- Each resident needs a motivation, walking/idle/work/talk states, distinct portrait, and dialogue triggered by a world event.
- Story should use a stable, authored narrative; AI may enrich optional lines but must not contradict established events.
- Use native canvas for current game, not pretend it is a 3D engine. Gradually upgrade art, depth/parallax, shadows and sprite animation.

## Technical architecture
- GitHub Actions: test + signed APK with same package/signing identity for in-place updates.
- Cloudflare Worker: AI speech proxy with OPENAI_API_KEY in secret; cache preapproved audio where feasible.
- Live content: versioned JSON manifest for level definitions, chapter text, event schedule, image/audio URLs. Android validates schema/version and caches the last good manifest. Native engine changes still require APK updates.
- Apps Script Code.gs: inspect actual endpoints and Sheet schema before migrating; never guess access or production deployment.
- Saved progress: migration-safe; do not reset existing user data.

## Acceptance criteria for next milestone
1. Current CI build and all device test shards green.
2. Key bypass and coin material purchase validated by tests (balance deductions, no double spend).
3. Map landmarks visible on Android device and aligned to actual chapters.
4. AI speech reaches Worker; offline fallback; cache cleanup and no API key in APK.
5. Document which content is truly live-updatable and which needs a new APK.
