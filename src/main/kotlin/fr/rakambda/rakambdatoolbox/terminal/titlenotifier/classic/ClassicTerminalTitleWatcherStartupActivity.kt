package fr.rakambda.rakambdatoolbox.terminal.titlenotifier.classic

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.ide.productMode.IdeProductMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClassicTerminalTitleWatcherStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // In remote development, Classic terminals run on the host, which watches them; its notifications are shown in
        // the JetBrains Client. Watching their synchronised copies in the client as well would notify twice.
        if (IdeProductMode.isFrontend) {
            return
        }
        withContext(Dispatchers.EDT) {
            ClassicTerminalTitleWatcher.getInstance(project).start()
        }
    }
}
