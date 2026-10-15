# Rakambda Toolbox

IntelliJ plugin for my personal needs.

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
