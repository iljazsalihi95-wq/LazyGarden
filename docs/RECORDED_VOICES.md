# Recorded dialogue delivery

No licensed human dialogue recordings are currently bundled. `clips` in
`app/src/main/assets/voices/manifest.json` is deliberately empty. Missing clips use the game Worker at `/api/game/voice`: it first looks up exact produced Studio audio, then calls the existing configured speech provider. Device TTS is not used. Game SFX are independent.

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
  "license": "License/contract identifier and attribution",
  "durationMillis": 3500
}
```

Each `(scene, line, character, language)` tuple must be unique. Duplicate
mappings, missing files, invalid asset paths and missing license fields are
handled by the online speech route and its cached audio. No audio file is claimed to exist by this example.

`GardenVoice.speakLine` prepares and plays a matching asset asynchronously.
`currentLine` exposes scene, line, character, language, asset, subtitle and duration.
`isTalking` and `onTalkingChanged` expose actual playback boundaries for a
character animation controller. Pause/detach releases the player. Subtitles
remain owned by the scene's localized timeline. Full recorded-clip-driven
subtitle timing and lip animation are PARTIALLY IMPLEMENTED: playback events
are exposed, but existing scenes still use their subtitle timelines.

## Voice routing repair

Interactive `fountain_quest` cues are different from Studio archive `story_1` cues. Studio returning UNKNOWN_GAME_DIALOGUE or VOICE_NOT_PRODUCED must not mute the rest of the game. The game Worker now resolves these misses with its configured speech provider and caches successful clips by scene, line, character, language, exact text, model and VOICE_CACHE_VERSION. Concurrent requests on an isolate share production. A recording replay does not start a new provider call while cached. No Studio access token or provider key is included in Android.

Fountain dialogue waits for both loading and playback, with a 35-second upper bound if the network fails. Its audio status/retry control is visible. Leaving a scene cancels the active HTTP connection; a cancelled request cannot apply a global cooldown to the next scene. A missing individual line also cannot apply that cooldown.

Production acceptance requires the voice-diagnostics workflow to receive and decode real MP3 audio in Albanian and Italian from the deployed game endpoint. Unit tests use simulated transport only; they do not prove that the runtime provider credential is valid. Cloudflare deployment and credential validity must be checked separately from Android build success.
