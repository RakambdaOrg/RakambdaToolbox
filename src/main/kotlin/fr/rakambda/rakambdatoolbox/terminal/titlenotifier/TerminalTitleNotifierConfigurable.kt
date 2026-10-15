package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.rows
import fr.rakambda.rakambdatoolbox.RakambdaToolboxBundle

class TerminalTitleNotifierConfigurable :
    BoundConfigurable(RakambdaToolboxBundle.message("settings.terminal.title.notifier.display.name")) {

    private val settings = TerminalTitleNotifierSettings.getInstance()

    private var patternsText: String
        get() = settings.patterns.joinToString("\n")
        set(value) {
            settings.patterns = TerminalTitleNotifierSettings.parsePatterns(value)
        }

    override fun createPanel(): DialogPanel = panel {
        row {
            label(RakambdaToolboxBundle.message("settings.terminal.title.notifier.patterns.label"))
        }
        row {
            textArea()
                .bindText(::patternsText)
                .rows(8)
                .align(Align.FILL)
                .comment(RakambdaToolboxBundle.message("settings.terminal.title.notifier.patterns.comment"))
        }.resizableRow()
    }
}
