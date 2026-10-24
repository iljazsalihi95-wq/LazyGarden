# LazyGarden

Original garden-restoration + match-3 Android game.

## Core game loop

`Garden -> choose restoration task -> play Match-3 -> earn game coins -> return to Garden -> restore object -> unlock story/task`

## Prototype target: Level 1

The first playable vertical slice is deliberately small and polished before expanding the campaign:

1. Intro: arrive at a completely neglected estate.
2. Character setup: choose **Meli**, **Bejdi**, **Meti**, **Lina**, or enter a custom player name.
3. Garden screen: the estate starts unrestored and Task 1 targets the broken fountain.
4. Match-3 Level 1: 25 moves; collect flowers, apples and leaves.
5. Win reward: game coins only.
6. Return to Garden and spend game coins on Task 1.
7. Fountain changes from broken/unrestored to restored.
8. Task 2 unlocks only after Task 1 is complete.

## Architecture rules

- Garden/restoration and Match-3 are separate game screens/systems.
- Match-3 levels generate progression currency; they are not construction tasks.
- Restoration objects have explicit states such as `BROKEN`, `RESTORING`, and `RESTORED`.
- Character name and selected avatar are persistent player state.
- Visual assets are original LazyGarden assets. Gardenscapes is only a gameplay/architecture reference; no proprietary assets or code are copied.
- LazyGarden remains standalone while the vertical slice is built and tested. Integration into We Earn Together happens only after approval.

## Visual direction

Warm premium cartoon garden-restoration style with a distinct LazyGarden identity: abandoned manor, overgrown paths, original fountain/bench/lighting/decor, readable mobile UI, expressive original characters and polished match-3 pieces.
