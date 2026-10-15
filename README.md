# Rakambda Toolbox

IntelliJ plugin for my personal needs.

## Features

- **Terminal title notifier**: sends a notification (system notification when the IDE is in the background, IDE
  notification otherwise) as soon as the title of a terminal of the Terminal tool window contains one of the
  configured strings. Works with the Classic and Reworked terminal engines. Configure it in
  *Settings | Tools | Terminal Title Notifier*.

  **Remote development: the plugin must be installed in the JetBrains Client**, not only on the host. Reworked
  terminals (the default engine) and system notifications only exist on the client side: without the plugin there,
  Reworked terminals are never watched. Classic terminals are watched on the host only (its notifications are shown in the client, but cannot be system
  notifications), so install it on both sides. The
  list of strings is synchronised between them.

## Installation and updates

Add this [URL](https://raw.githubusercontent.com/RakambdaOrg/RakambdaToolbox/refs/heads/main/updatePlugins.xml) to the JetBrains [IDE Repository Settings](https://www.jetbrains.com/help/idea/managing-plugins.html#repos), then install *Rakambda Toolbox* from the Marketplace tab.

The plugin zip of each version is also attached to the [GitHub releases](https://github.com/RakambdaOrg/RakambdaToolbox/releases) and can be installed with *Install Plugin from Disk...*.

## Development

Requires JDK 21.

```bash
./gradlew build    # compile, test and build the plugin zip into build/distributions/
./gradlew runIde   # start a sandbox IDE with the plugin
```

See [AGENTS.md](AGENTS.md) for the project conventions and the release process.
