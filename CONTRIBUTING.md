# Contributing to Lagoon

Thanks for helping. Lagoon is a redesigned interface on top of the open source Jellyfin for Android TV client, so
most rules below are about keeping the new interface clean and keeping the upstream code easy to update.

## Before you start

- Open an issue first for anything bigger than a small fix, so the approach can be agreed before you write it.
- Problems with a Jellyfin server itself belong with the [Jellyfin project](https://jellyfin.org/docs/general/getting-help.html).
- Never put server addresses, user names, API keys, tokens or anything else private in code, commits, issues,
  screenshots or logs. This repository is public.

## Build and run

1. Install Android Studio, or JDK 21 and the Android SDK.
2. Clone the repository and run `./gradlew assembleDebug`. The debug build installs beside a release build.
3. Run the tests with `./gradlew test`.
4. Try changes on an Android TV or Google TV emulator, or a real device with `adb install`, and drive them with the
   D-pad only. A touch screen hides focus bugs that a remote will find.

## Where code goes

- New interface code lives in `app/src/main/java/org/jellyfin/androidtv/ui/lagoon/`, grouped by feature
  (`browse`, `home`, `library`, `details`, `search`, `player`) plus shared building blocks in `design`.
- Its tests live in `app/src/test/kotlin/ui/lagoon/`, one subject per file.
- Change upstream files only to wire a Lagoon screen in, keep those edits as small as possible, and leave a short
  comment saying why. Every extra upstream edit is a future merge conflict.
- New screens get their destination in `LagoonDestinations`, not in upstream's `Destinations`.

## Test first

1. **Red.** Write a failing test that describes the behaviour, or reproduces the bug. Run it and make sure it fails
   for the right reason.
2. **Green.** Write the smallest code that makes it pass.
3. **Refactor** with the tests green.

- Put decisions in plain Kotlin classes and test them with in-memory data: which rows a screen shows, how results
  are ranked, what a Play button does. Compose code should mostly arrange and display what those classes decide.
- Tests use Kotest's `FunSpec` and hand-written fakes. Name tests as sentences that describe behaviour, for example
  `"An episode opens its series page on that season"`, never `test1`.
- Tests are deterministic: fixed times, no network, no real clock.
- Never delete or weaken a test to make a change pass. "Tests pass" in a pull request means you ran them.

## Code style

- **Single responsibility.** One reason to change per class. If a class needs "and" to describe it, split it.
- **Depend on small interfaces.** Screens talk to the rest of the app through interfaces such as `LagoonActions`
  and `PlayerCommands`, so they can be tested and so upstream details stay in one place.
- **Names reveal intent.** Classes are nouns, functions are verbs, booleans read as questions (`isFocused`,
  `hasNext`). No `Manager`, `Helper`, `Util`, `Data` or `Info` classes, no abbreviations, named constants instead of
  magic numbers.
- **Code reads like prose.** Rename or extract before you comment. A comment explains why, never what. No commented
  out code, journal comments, banners or closing-brace comments.
- **One type per file**, file-scoped package, tabs for indentation to match upstream.
- **Time.** Read the clock through a parameter or an injected provider, not directly, so the code can be tested.
- **No em dashes** anywhere: code, comments, UI text, documentation or commit messages.

## Designing for a remote

- Every screen must work with Up, Down, Left, Right, Select and Back alone.
- Focus is always visible: focused items grow slightly and take the teal accent.
- Back steps out one level at a time: close a panel, then hide controls, then leave the screen.
- When a screen opens or you return to it, focus lands somewhere sensible, and returning restores where you were.
- Text is short and plain. Buttons say what they do.

## Dependencies and licenses

- Lagoon is GPL-2.0 because the upstream project is. Contributions are accepted under the same license.
- New dependencies must be compatible with that license, preferably MIT, Apache-2.0 or BSD. Record each new one in
  [NOTICES.md](NOTICES.md).
- Prefer a small piece of our own code to a new dependency.

## Commits and pull requests

- Commit subjects are sentence case and imperative, and say what changed: "Show unwatched counts on poster cards",
  not "fixes" or "update ui".
- The body explains why, and what was deliberately left alone. Plain text, no emojis.
- Fill in the pull request template, including which screens you checked with a D-pad and which upstream files you
  touched.

## Releases

- A release is a version tag pushed from `master`: `v1.2.3`, or `v1.2.3-rc.1` for a pre-release. Lagoon uses plain
  semantic versions because Android derives its version code from them.
- The release workflow runs the tests, builds a signed APK and publishes it as a GitHub release. A failing test
  publishes nothing.
- The website's download button always points at the latest release, so nothing else needs updating.

## Keeping up with upstream

The upstream project is the `upstream` remote. To bring in its changes:

```bash
git fetch upstream --tags
git merge v0.21.0
```

Resolve conflicts in favour of upstream behaviour and Lagoon's interface, run the tests, and check the main
screens on a TV before merging.
