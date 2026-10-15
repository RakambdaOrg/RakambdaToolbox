package fr.rakambda.rakambdatoolbox.terminal.titlenotifier.classic

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClassicTerminalTitleWatcherStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        withContext(Dispatchers.EDT) {
            ClassicTerminalTitleWatcher.getInstance(project).start()
        }
    }
}
