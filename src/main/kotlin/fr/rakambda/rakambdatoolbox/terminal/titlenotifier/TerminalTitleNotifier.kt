package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.terminal.TerminalTitle
import com.intellij.terminal.TerminalTitleListener
import com.intellij.ui.SystemNotifications
import fr.rakambda.rakambdatoolbox.RakambdaToolboxBundle

private const val NOTIFICATION_GROUP_ID = "Rakambda Toolbox: terminal title"
private const val SYSTEM_NOTIFICATION_NAME = "Terminal title matched"

private val LOG = logger<TerminalTitleNotifier>()

/**
 * Notifies the user when a watched [TerminalTitle] starts containing a string configured in
 * [TerminalTitleNotifierSettings].
 *
 * The terminals themselves are found by engine-specific watchers (`classic` and `reworked` sub-packages), which call
 * [watch] for every terminal. Disposed with the project: watchers register their per-terminal disposables under it.
 */
@Service(Service.Level.PROJECT)
class TerminalTitleNotifier(private val project: Project) : Disposable {
    /**
     * Starts watching [title] until [parentDisposable] is disposed.
     *
     * @param terminalName short description of the terminal, for the debug log only.
     * @param showTerminal brings the terminal to the front; called on the EDT from the notification action.
     */
    fun watch(title: TerminalTitle, parentDisposable: Disposable, terminalName: String, showTerminal: () -> Unit) {
        LOG.debug { "Watching $terminalName (${title.buildFullTitle()})" }

        val tracker = TitleMatchTracker()
        // Initialise with the current title without notifying: only strings appearing from now on are reported.
        tracker.update(titlesToMatch(title), TerminalTitleNotifierSettings.getInstance().patterns)

        title.addTitleListener(object : TerminalTitleListener {
            override fun onTitleChanged(terminalTitle: TerminalTitle) {
                val titles = titlesToMatch(terminalTitle)
                val newMatches = tracker.update(titles, TerminalTitleNotifierSettings.getInstance().patterns)
                LOG.debug { "Title of $terminalName changed to $titles, new matches: $newMatches" }
                if (newMatches.isNotEmpty()) {
                    notifyMatch(displayedTitle(terminalTitle), showTerminal)
                }
            }
        }, parentDisposable)
    }

    /**
     * Both the title shown in the tab (possibly renamed by the user, and shortened) and the full title set by the
     * running program are checked. The program title is checked even when the terminal settings hide it from the tab.
     */
    private fun titlesToMatch(title: TerminalTitle): List<String> = listOfNotNull(title.buildTitle(), title.applicationTitle)

    private fun displayedTitle(title: TerminalTitle): String {
        val tabTitle = title.buildFullTitle()
        val applicationTitle = title.applicationTitle
        return if (applicationTitle.isNullOrBlank() || applicationTitle == tabTitle) tabTitle else "$tabTitle: $applicationTitle"
    }

    private fun notifyMatch(terminalTitle: String, showTerminal: () -> Unit) {
        ApplicationManager.getApplication().invokeLater({
            val notificationTitle = RakambdaToolboxBundle.message("notification.terminal.title.matched.title", project.name)

            // Only shown by the platform when the IDE is not the active application and system notifications are
            // enabled (Settings | Appearance & Behavior | Notifications). Never shown by a remote development host.
            SystemNotifications.getInstance().notify(SYSTEM_NOTIFICATION_NAME, notificationTitle, terminalTitle)

            // Always posted as well: it is the only notification when the IDE is active or the OS one is unavailable,
            // and it stays in the Notifications tool window.
            NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(notificationTitle, StringUtil.escapeXmlEntities(terminalTitle), NotificationType.INFORMATION)
                .addAction(NotificationAction.createSimpleExpiring(
                    RakambdaToolboxBundle.message("notification.terminal.title.matched.action.show")
                ) {
                    showTerminal()
                })
                .notify(project)
        }, project.disposed)
    }

    override fun dispose() {
    }

    companion object {
        fun getInstance(project: Project): TerminalTitleNotifier = project.service()
    }
}
