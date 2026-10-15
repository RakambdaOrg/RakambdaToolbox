package fr.rakambda.rakambdatoolbox.terminal.titlenotifier

import com.intellij.ide.settings.RemoteSettingInfo
import com.intellij.ide.settings.RemoteSettingInfo.Direction.InitialFromFrontend
import com.intellij.ide.settings.RemoteSettingInfoProvider

/**
 * Synchronises [TerminalTitleNotifierSettings] between the remote development host and the JetBrains Client.
 *
 * The initial value comes from the client, like the Terminal plugin's own settings: it is the side that persists across
 * hosts (a dev container can be recreated). Later changes are sent both ways.
 */
class TerminalTitleNotifierRemoteSettingsInfoProvider : RemoteSettingInfoProvider {
    override fun getRemoteSettingsInfo(): Map<String, RemoteSettingInfo> = mapOf(
        TerminalTitleNotifierSettings.COMPONENT_NAME to RemoteSettingInfo(InitialFromFrontend),
    )
}
