package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/**
 * Global (application level) list of strings that trigger a notification when they appear in a terminal title.
 */
@Service(Service.Level.APP)
@State(name = "TerminalTitleNotifier", storages = [Storage("rakambdatoolbox.xml")])
class TerminalTitleNotifierSettings : PersistentStateComponent<TerminalTitleNotifierSettings.Settings> {
    class Settings {
        var patterns: MutableList<String> = mutableListOf()
    }

    // Replaced as a whole and never mutated once published, so it can be read from any thread.
    @Volatile
    private var settings = Settings()

    var patterns: List<String>
        get() = settings.patterns
        set(value) {
            settings = Settings().apply { patterns = value.toMutableList() }
        }

    override fun getState(): Settings = settings

    override fun loadState(state: Settings) {
        settings = state
    }

    companion object {
        fun getInstance(): TerminalTitleNotifierSettings = service()

        /** Converts the text typed in the settings (one pattern per line) to the stored list. */
        fun parsePatterns(text: String): List<String> =
            text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.distinct().toList()
    }
}
