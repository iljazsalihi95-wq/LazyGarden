# Recorded dialogue delivery

No licensed human dialogue recordings are currently bundled. `clips` in
`app/src/main/assets/voices/manifest.json` is deliberately empty. Missing clips
are silent; there is no TTS or substitute language. Game SFX are independent.

The manifest lists the 18 requested character IDs, five supported languages,
and stable line bindings for the existing animated restoration renderers.
These bindings describe the current renderers, not completed premium quests.

To deliver a recording, add its asset under `app/src/main/assets/voices/`, retain
its license evidence, and add an entry to `clips` with these exact fields:

```json
{
  "scene": "fountain",
  "line": "talk",
  "character": "meli",
  "language": "sq",
  "asset": "voices/sq/meli/fountain-talk.ogg",
  "license": "License/contract identifier and attribution"
}
```

Each `(scene, line, character, language)` tuple must be unique. Duplicate
mappings, missing files, invalid asset paths and missing license fields are
silent. No audio file is claimed to exist by this example.

`GardenVoice.speakLine` prepares and plays a matching asset asynchronously.
`isTalking` and `onTalkingChanged` expose actual playback boundaries for a
character animation controller. Pause/detach releases the player. Subtitles
remain owned by the scene's localized timeline. Full recorded-clip-driven
subtitle timing and lip animation are PARTIALLY IMPLEMENTED: playback events
are exposed, but existing scenes still use their subtitle timelines.
