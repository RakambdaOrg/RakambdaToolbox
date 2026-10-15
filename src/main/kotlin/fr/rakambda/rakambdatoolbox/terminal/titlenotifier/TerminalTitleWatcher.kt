package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.terminal.TerminalTitle
import com.intellij.terminal.TerminalTitleListener
import com.intellij.terminal.ui.TerminalWidget
import com.intellij.ui.SystemNotifications
import com.intellij.util.concurrency.annotations.RequiresEdt
import fr.rakambda.rakambdatoolbox.RakambdaToolboxBundle
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

private const val NOTIFICATION_GROUP_ID = "Rakambda Toolbox: terminal title"
private const val SYSTEM_NOTIFICATION_NAME = "Terminal title matched"

/**
 * Watches the titles of the terminals of the Terminal tool window and notifies the user when one of them starts
 * containing a string configured in [TerminalTitleNotifierSettings].
 */
@Service(Service.Level.PROJECT)
class TerminalTitleWatcher(private val project: Project) : Disposable {
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
        val listenerDisposable = Disposer.newDisposable("RakambdaToolbox terminal title watcher")
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

        val tracker = TitleMatchTracker()
        val title = widget.terminalTitle
        // Initialise with the current title without notifying: only strings appearing from now on are reported.
        tracker.update(titlesToMatch(title), TerminalTitleNotifierSettings.getInstance().patterns)

        val widgetRef = WeakReference(widget)
        title.addTitleListener(object : TerminalTitleListener {
            override fun onTitleChanged(terminalTitle: TerminalTitle) {
                val newMatches = tracker.update(titlesToMatch(terminalTitle), TerminalTitleNotifierSettings.getInstance().patterns)
                if (newMatches.isNotEmpty()) {
                    notifyMatch(displayedTitle(terminalTitle), widgetRef)
                }
            }
        }, listenerDisposable)
    }

    /**
     * Both the title shown in the tab (possibly renamed by the user, and shortened) and the full title set by the
     * running program are checked.
     */
    private fun titlesToMatch(title: TerminalTitle): List<String> = listOfNotNull(title.buildTitle(), title.applicationTitle)

    private fun displayedTitle(title: TerminalTitle): String {
        val tabTitle = title.buildFullTitle()
        val applicationTitle = title.applicationTitle
        return if (applicationTitle == null || applicationTitle == tabTitle) tabTitle else "$tabTitle: $applicationTitle"
    }

    private fun notifyMatch(terminalTitle: String, widgetRef: WeakReference<TerminalWidget>) {
        ApplicationManager.getApplication().invokeLater({
            val notificationTitle = RakambdaToolboxBundle.message("notification.terminal.title.matched.title", project.name)

            // Only shown by the platform when the IDE is not the active application and system notifications are
            // enabled (Settings | Appearance & Behavior | Notifications).
            SystemNotifications.getInstance().notify(SYSTEM_NOTIFICATION_NAME, notificationTitle, terminalTitle)

            // Always posted as well: it is the only notification when the IDE is active or the OS one is unavailable,
            // and it stays in the Notifications tool window.
            NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(notificationTitle, StringUtil.escapeXmlEntities(terminalTitle), NotificationType.INFORMATION)
                .addAction(NotificationAction.createSimpleExpiring(
                    RakambdaToolboxBundle.message("notification.terminal.title.matched.action.show")
                ) {
                    showTerminal(widgetRef.get())
                })
                .notify(project)
        }, project.disposed)
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
        fun getInstance(project: Project): TerminalTitleWatcher = project.service()
    }
}
