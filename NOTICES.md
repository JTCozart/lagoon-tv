# Notices and attribution

Lagoon is a modified version of **Jellyfin for Android TV**
(<https://github.com/jellyfin/jellyfin-androidtv>), copyright the Jellyfin contributors, released under the
GNU General Public License, version 2. Lagoon is distributed under the same license; see [LICENSE](LICENSE).

## What Lagoon changed

Lagoon started from the upstream `v0.20.0` release. The playback engine, server connection, sign-in, settings and
most supporting code are the upstream project's work. Lagoon added:

- A new interface for the home screen, libraries, details pages, search and the video player controls, under
  `app/src/main/java/org/jellyfin/androidtv/ui/lagoon/`.
- A new name, application id, logo, launcher icon, banner and colour theme.
- Small changes to upstream files to wire the new screens in. `git log` and `git diff v0.20.0` show every change.

"Jellyfin" is the name of the Jellyfin project. Lagoon is an independent client and is not affiliated with or
endorsed by the Jellyfin project.

## Third-party components

The list below covers the libraries Lagoon ships with, as declared in `gradle/libs.versions.toml`. The licenses are
taken from each library's published Maven metadata. The app also shows the complete, generated list, including
transitive dependencies, under **Preferences > About > Licenses**.

| Component | License |
| --- | --- |
| Jellyfin SDK for Kotlin (`org.jellyfin.sdk:jellyfin-core`) | LGPL-3.0 |
| Jellyfin Media3 FFmpeg decoder (`org.jellyfin.media3:media3-ffmpeg-decoder`) | GPL-3.0 |
| AndroidX libraries (activity, appcompat, cardview, Compose, constraintlayout, core, fragment, leanback, lifecycle, navigation3, preference, recyclerview, startup, tvprovider, window, work) | Apache-2.0 |
| AndroidX Media3 (ExoPlayer, HLS, session, UI, OkHttp data source) | Apache-2.0 |
| Accompanist permissions | Apache-2.0 |
| Kotlin coroutines and kotlinx.serialization | Apache-2.0 |
| Koin | Apache-2.0 |
| Coil | Apache-2.0 |
| Markwon | Apache-2.0 |
| ACRA | Apache-2.0 |
| AboutLibraries | Apache-2.0 |
| Timber and slf4j-timber | Apache-2.0 |
| ass-media (`io.github.peerless2012:ass-media`) | MIT |
| Android desugaring library (`com.android.tools:desugar_jdk_libs`) | GPL-2.0 with the Classpath Exception |

Used only to build and test, not shipped: the Android Gradle Plugin, the Kotlin Gradle plugin (Apache-2.0),
Kotest (Apache-2.0) and MockK (Apache-2.0).

## Fonts and artwork

The Lagoon logo, icon and banner are original artwork made for this project and are covered by the project license.
Posters, backdrops and logos shown in the app and in the screenshots come from the user's own Jellyfin server and
belong to their respective owners.
