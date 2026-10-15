import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.21"
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        testFramework(TestFrameworkType.Platform)

        // Terminal title notifier
        bundledPlugin("org.jetbrains.plugins.terminal")
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }

        changeNotes = """
            <h3>2.0.1</h3>
            <ul>
                <li>Terminal title notifier: the list of strings is synchronised between the remote development host and the JetBrains Client, so Reworked terminals (watched on the client) use it too.</li>
            </ul>
            <h3>2.0.0</h3>
            <ul>
                <li>Requires IDE 2026.2 or newer.</li>
                <li>Terminal title notifier: supports the Reworked terminal engine, including in remote development (JetBrains Client).</li>
            </ul>
            <h3>1.0.0</h3>
            <ul>
                <li>Terminal title notifier: notifies when a terminal title contains one of the configured strings (Settings | Tools | Terminal Title Notifier).</li>
            </ul>
        """.trimIndent()
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}
