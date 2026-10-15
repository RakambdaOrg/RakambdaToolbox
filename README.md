# Rakambda Toolbox

IntelliJ plugin for my personal needs.

## Features

- **Terminal title notifier**: sends a notification (system notification when the IDE is in the background, IDE
  notification otherwise) as soon as the title of a terminal of the Terminal tool window contains one of the
  configured strings. Configure them in *Settings | Tools | Terminal Title Notifier*. To match the title set by the
  program running in the terminal, enable the advanced setting
  `terminal.show.application.title` (*Settings | Advanced Settings*, Terminal section).

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
