package fr.rakambda.rakambdatoolbox.terminal.titlenotifier.classic

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.terminal.ui.TerminalWidget
import com.intellij.util.concurrency.annotations.RequiresEdt
import fr.rakambda.rakambdatoolbox.terminal.titlenotifier.TerminalTitleNotifier
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Finds the Classic terminals of the Terminal tool window. Reworked terminals do not go through
 * [TerminalToolWindowManager] and are handled by the `reworked` sub-package.
 */
@Service(Service.Level.PROJECT)
class ClassicTerminalTitleWatcher(private val project: Project) : Disposable {
    /** Watched widgets, with the disposable that removes our listener. */
    private val watched = ConcurrentHashMap<TerminalWidget, Disposable>()

    @RequiresEdt
    fun start() {
        val manager = TerminalToolWindowManager.getInstance(project)
        manager.addNewTerminalSetupHandler(::watch, this)
        // Terminals restored before this service started are not reported by the setup handler.
        manager.terminalWidgets.forEach(::watch)
    }

    private fun watch(widget: TerminalWidget) {
        val listenerDisposable = Disposer.newDisposable("RakambdaToolbox classic terminal title watcher")
        if (watched.putIfAbsent(widget, listenerDisposable) != null) {
            Disposer.dispose(listenerDisposable)
            return
        }
        Disposer.register(listenerDisposable) { watched.remove(widget) }
        // Disposed with the widget when the terminal is closed, or by dispose() when the project or plugin goes away.
        if (!Disposer.tryRegister(widget, listenerDisposable)) {
            Disposer.dispose(listenerDisposable)
            return
        }

        val widgetRef = WeakReference(widget)
        TerminalTitleNotifier.getInstance(project).watch(widget.terminalTitle, listenerDisposable, "classic terminal") {
            showTerminal(widgetRef.get())
        }
    }

    private fun showTerminal(widget: TerminalWidget?) {
        val toolWindow = TerminalToolWindowManager.getInstance(project).toolWindow ?: return
        val content = widget?.let { toolWindow.contentManager.contents.firstOrNull { TerminalToolWindowManager.findWidgetByContent(it) == widget } }
        toolWindow.activate {
            if (content != null) {
                toolWindow.contentManager.setSelectedContent(content, true)
            }
        }
    }

    override fun dispose() {
        watched.values.toList().forEach(Disposer::dispose)
    }

    companion object {
        fun getInstance(project: Project): ClassicTerminalTitleWatcher = project.service()
    }
}
