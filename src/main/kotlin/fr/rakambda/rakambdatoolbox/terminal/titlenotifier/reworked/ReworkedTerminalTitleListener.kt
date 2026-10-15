package fr.rakambda.rakambdatoolbox.terminal.titlenotifier.reworked

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.frontend.toolwindow.TerminalTabsManagerListener
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager
import com.intellij.terminal.frontend.view.TerminalView
import fr.rakambda.rakambdatoolbox.terminal.titlenotifier.TerminalTitleNotifier
import kotlinx.coroutines.job
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import java.lang.ref.WeakReference

/**
 * Finds the Reworked terminals of the Terminal tool window.
 *
 * Lives in the `fr.rakambda.rakambdatoolbox.terminal.reworked` content module because the Reworked terminal API is in
 * the `intellij.terminal.frontend` module, which has its own classloader. That module only runs on the frontend: in
 * the IDE itself, or in the JetBrains Client in remote development.
 */
class ReworkedTerminalTitleListener(private val project: Project) : TerminalTabsManagerListener {
    override fun terminalViewCreated(view: TerminalView) {
        val notifier = TerminalTitleNotifier.getInstance(project)
        val listenerDisposable = Disposer.newDisposable("RakambdaToolbox reworked terminal title watcher")
        // Disposed when the project or the plugin goes away...
        if (!Disposer.tryRegister(notifier, listenerDisposable)) {
            return
        }
        // ...or when the terminal session ends (immediately if it already ended).
        val completionHandle = view.coroutineScope.coroutineContext.job.invokeOnCompletion { Disposer.dispose(listenerDisposable) }
        if (!Disposer.tryRegister(listenerDisposable) { completionHandle.dispose() }) {
            completionHandle.dispose()
            return
        }

        val viewRef = WeakReference(view)
        notifier.watch(view.title, listenerDisposable, "reworked terminal") {
            showTerminal(viewRef.get())
        }
    }

    private fun showTerminal(view: TerminalView?) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID) ?: return
        val content = view?.let { TerminalToolWindowTabsManager.getInstance(project).tabs.firstOrNull { it.view == view }?.content }
        toolWindow.activate {
            content?.manager?.setSelectedContent(content, true)
        }
    }
}
