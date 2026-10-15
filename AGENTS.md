# AGENTS.md

Context for AI coding agents (and humans) working on this repository.

## What this project is

Rakambda Toolbox is a personal IntelliJ Platform plugin. It is the single place where the owner collects the small IDE
customisations they want (actions, inspections, intentions, tool windows, settings, etc.). It is not published on the
JetBrains Marketplace: it is distributed through GitHub releases and a custom plugin repository (`updatePlugins.xml`).

Every feature is independent. A new feature must not change the behaviour of existing ones, and it should be easy to
remove on its own.

## Tech stack

| Item                         | Value / location                                                    |
|------------------------------|---------------------------------------------------------------------|
| Language                     | Kotlin (JVM 21 toolchain)                                           |
| Build                        | Gradle (wrapper in `gradle/wrapper`), Kotlin DSL                    |
| IntelliJ Platform plugin     | `org.jetbrains.intellij.platform` 2.x, configured in `build.gradle.kts` |
| Target IDE                   | `platformType` / `platformVersion` in `gradle.properties`           |
| Minimum IDE build            | `pluginSinceBuild` in `gradle.properties` (no upper bound)          |
| Plugin ID                    | `fr.rakambda.rakambdatoolbox`                                       |
| Base package                 | `fr.rakambda.rakambdatoolbox`                                       |
| Only platform dependency     | `com.intellij.modules.platform` (works in every IntelliJ-based IDE) |

The Kotlin standard library is not bundled (`kotlin.stdlib.default.dependency=false`): the one shipped with the IDE is
used.

## Repository layout

```
build.gradle.kts                  Build, IntelliJ Platform and verification configuration
gradle.properties                 Plugin version, target platform, since-build
settings.gradle.kts               Project name, JDK toolchain resolver
src/main/kotlin/                  Plugin code (package fr.rakambda.rakambdatoolbox)
src/main/resources/META-INF/
    plugin.xml                    Plugin descriptor: extensions, actions, dependencies
    pluginIcon.svg                Plugin icon
src/test/kotlin/                  Tests (IntelliJ Platform test framework, JUnit 4)
updatePlugins.xml                 Custom plugin repository descriptor read by the IDE
scripts/check-update-descriptor.sh  Checks updatePlugins.xml against gradle.properties
.run/                             Shared IntelliJ run configurations (Run IDE with Plugin)
.github/workflows/build.yml       CI: build, tests, plugin verification
.github/workflows/release.yml     Attaches the plugin zip to a published GitHub release
```

`src/main/kotlin` and `src/test/kotlin` do not exist yet: create them with the first feature.

## Commands

```bash
./gradlew build          # compile, run tests, build the plugin zip (build/distributions/)
./gradlew test           # tests only
./gradlew runIde         # start a sandbox IDE with the plugin installed
./gradlew verifyPlugin   # IntelliJ Plugin Verifier against the recommended IDE versions (slow, downloads IDEs)
./gradlew buildPlugin    # only build the distributable zip
./scripts/check-update-descriptor.sh
```

The build downloads the IDE from JetBrains servers (`www.jetbrains.com`, `cache-redirector.jetbrains.com`,
`download.jetbrains.com`). In a sandbox without access to those hosts, nothing can be compiled: say so instead of
claiming the build passes. CI (`build.yml`) runs on every push and is the reference.

## Conventions

### Code

- Kotlin only for new code. Follow the official Kotlin coding conventions; `.editorconfig` only defines Java and
  generic IntelliJ settings.
- One feature per sub-package of `fr.rakambda.rakambdatoolbox` (e.g. `...rakambdatoolbox.actions.copypath`), so a
  feature can be found and deleted as a unit.
- Register everything declaratively in `plugin.xml` (actions, extensions, services, listeners). Group the entries of a
  feature together with an XML comment naming it.
- Prefer light services (`@Service`) over `<applicationService>` / `<projectService>` declarations.
- User-visible strings go in a message bundle (`messages/RakambdaToolboxBundle.properties` + a `DynamicBundle`
  wrapper) once the first one is needed. Do not hardcode them.
- Respect the IntelliJ threading model: no blocking work on the EDT, read actions to access PSI/VFS, write actions
  (on the EDT) to modify them. For background work, prefer coroutines launched from the `CoroutineScope`
  injected into a light service's constructor.
- Do not use APIs marked `@ApiStatus.Internal`, `@ApiStatus.Experimental` or deprecated for removal unless there is no
  alternative; `verifyPlugin` reports them.
- If a feature needs a bundled plugin (Java, Kotlin, Git...), add `bundledPlugin(...)` in `build.gradle.kts` **and** an
  optional dependency in `plugin.xml` (`<depends optional="true" config-file="...">`) so the plugin still loads in IDEs
  that lack it.

### Tests

- Tests use the IntelliJ Platform test framework (`BasePlatformTestCase`, `CodeInsightTestFixture`) on JUnit 4.
- Add a test for any logic that can be exercised without UI (inspections, intentions, actions on files, pure helpers).

### Files you must not edit here

These files are synchronised from `RakambdaOrg/rakambda-github-files` and local edits will be overwritten:

- `.github/dependabot.yml`
- `.editorconfig`

`.github/workflows/gradle-wrapper-update.yml` calls a shared workflow from `RakambdaOrg/rakambda-github-workflows`.

### Dependencies

Dependabot updates Gradle plugins/dependencies and GitHub Actions; a scheduled workflow updates the Gradle wrapper.
Do not bump versions manually unless the task requires it.

## Changing the target IDE

1. Change `platformVersion` (and `platformType` if needed) in `gradle.properties`.
2. Set `pluginSinceBuild` to the same major build (e.g. `2025.2.x` -> `252`). Compiling against a newer platform than
   `since-build` means users of older IDEs can hit missing APIs at runtime.
3. Update `since-build` in `updatePlugins.xml` accordingly.

From 2025.3, IntelliJ IDEA Community is no longer distributed as a separate product: targeting it requires switching
to the unified IntelliJ IDEA distribution. Check the IntelliJ Platform Gradle Plugin documentation for the
corresponding `platformType` before upgrading.

## Release process

1. Bump `pluginVersion` in `gradle.properties` (semantic versioning).
2. Update `changeNotes` in `build.gradle.kts`.
3. Update `updatePlugins.xml`: `version`, `url` (`.../releases/download/v<version>/RakambdaToolbox-<version>.zip`) and
   `since-build`. `scripts/check-update-descriptor.sh` (also run in CI) fails if they disagree with
   `gradle.properties`.
4. Merge to `main`, then publish a GitHub release with tag `v<version>`.
5. `release.yml` checks the tag against `pluginVersion`, builds the plugin and attaches the zip to the release.

The IDE reads `updatePlugins.xml` from `main`, so the zip must be attached to the release before users can update:
merging step 3 before publishing the release leaves a short window where the URL returns 404.
