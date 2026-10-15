package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TerminalTitleWatcherStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        withContext(Dispatchers.EDT) {
            TerminalTitleWatcher.getInstance(project).start()
        }
    }
}
