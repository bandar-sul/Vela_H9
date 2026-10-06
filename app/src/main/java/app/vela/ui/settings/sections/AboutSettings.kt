package app.vela.ui.settings.sections

import app.vela.ui.icons.Sym

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.vela.BuildConfig
import app.vela.R
import app.vela.ui.DpadRingBox // D-pad-only operation (docs/dpad.md)
import app.vela.ui.Onboarding
import app.vela.ui.map.MapViewModel
import app.vela.ui.settings.GroupDivider
import app.vela.ui.settings.Hint
import app.vela.ui.settings.PageIntro
import app.vela.ui.settings.SelectableRow
import app.vela.ui.settings.SettingsGroup
import app.vela.ui.settings.SettingsScaffold
import app.vela.ui.settings.ToggleRow
import app.vela.ui.dpadHighlight
import app.vela.ui.dpadRowSibling

/** About sub-screen: the project blurb, support/donate, version + the in-app updater. */
@Composable
internal fun AboutSettingsScreen(vm: MapViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("vela_settings", android.content.Context.MODE_PRIVATE) }
    SettingsScaffold(stringResource(R.string.settings_about), onBack) { topRow ->
        Spacer(Modifier.height(4.dp))
        PageIntro(stringResource(R.string.settings_about_hint))
        Spacer(Modifier.height(8.dp))

        SettingsGroup(title = stringResource(R.string.h9_about_title)) {
            androidx.compose.foundation.layout.Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    stringResource(R.string.h9_developer_credit),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.h9_social_credit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        SettingsGroup(title = stringResource(R.string.settings_support)) {
        androidx.compose.foundation.layout.Column(Modifier.padding(horizontal = 16.dp)) {
        Hint(stringResource(R.string.settings_support_hint))
        Spacer(Modifier.height(4.dp))
        FilledTonalButton(
            // The top focusable control: Back routes its DOWN here, UP from here goes back to Back.
            modifier = topRow.dpadHighlight(androidx.compose.foundation.shape.CircleShape),
            onClick = {
Onboarding.openDonate(context)
            },
        ) {
            Icon(Sym.Favorite, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.settings_support_button))
        }
        }
        }

        Spacer(Modifier.height(8.dp))
        // Where the map comes from (issue #302): the ODbL credit belongs in About as well as on
        // the map, and people asked where the streets come from when they differ from Google.
        SettingsGroup(title = stringResource(R.string.settings_map_data)) {
        androidx.compose.foundation.layout.Column(Modifier.padding(horizontal = 16.dp)) {
        Hint(stringResource(R.string.settings_map_data_hint))
        Spacer(Modifier.height(4.dp))
        FilledTonalButton(
            modifier = Modifier.dpadHighlight(androidx.compose.foundation.shape.CircleShape),
            onClick = {
                runCatching {
                    context.startActivity(
                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.openstreetmap.org/copyright")),
                    )
                }
            },
        ) {
            Text(stringResource(R.string.settings_map_data_button))
        }
        Spacer(Modifier.height(4.dp))
        FilledTonalButton(
            modifier = Modifier.dpadHighlight(androidx.compose.foundation.shape.CircleShape),
            onClick = {
                runCatching {
                    context.startActivity(
                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://cdla.dev/permissive-2-0/")),
                    )
                }
            },
        ) {
            Text(stringResource(R.string.settings_map_data_overture_button))
        }
        }
        }

        Spacer(Modifier.height(8.dp))
        SettingsGroup(title = stringResource(R.string.settings_version)) {
        // Tap to copy - bug reports need the exact version, and typing it from the screen
        // is error-prone (issue #58's one keeper suggestion).
        val versionLine = stringResource(R.string.settings_version_line, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        val versionCopied = stringResource(R.string.settings_version_copied)
        val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
        Text(
            versionLine,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .dpadHighlight()
                .clickable {
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(versionLine))
                    android.widget.Toast.makeText(context, versionCopied, android.widget.Toast.LENGTH_SHORT).show()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
        GroupDivider()
        // Who Android says installed this build. Android Auto only lists a navigation app whose
        // install source is Play, which King Installer and AAEnabler set on purpose, and a
        // self-update quietly resets; this row says whether that setup is still in place, so it
        // can be checked BEFORE an update and after one (user 2026-09-22, ahead of a GrapheneOS
        // install).
        val installer = remember { app.vela.update.InstallSource.installingPackage(context) }
        // Who STARTED the install is its own record, and the one newer Android Auto reads; shown
        // when it differs, so a setup that set only one of the two can be seen for what it is.
        val initiator = remember { app.vela.update.InstallSource.initiatingPackage(context) }
        val installerLine = when {
            app.vela.update.InstallSource.setForCar(context) -> stringResource(R.string.settings_installer_play, installer ?: initiator ?: "")
            installer.isNullOrBlank() -> stringResource(R.string.settings_installer_none)
            else -> stringResource(R.string.settings_installer_other, installer)
        } + if (!initiator.isNullOrBlank() && initiator != installer) " " + stringResource(R.string.settings_installer_started_by, initiator) else ""
        Text(
            installerLine,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        GroupDivider()
        // The release notes of this build, on demand (they also show once after an update).
        Text(
            stringResource(R.string.settings_whatsnew),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .dpadHighlight()
                .clickable { app.vela.ui.WhatsNew.show(context, force = true) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Hint(stringResource(R.string.settings_whatsnew_hint))
        ToggleRow(
            label = stringResource(R.string.whatsnew_show_after_updates),
            checked = app.vela.ui.WhatsNew.enabled.value,
            onCheckedChange = { app.vela.ui.WhatsNew.setEnabled(context, it) },
        )
        GroupDivider()
        // H9 edition: upstream Vela self-update controls are intentionally omitted.
        // Car-specific builds are updated through the H9 release channel only.
        }
        // Breathing room under the last control so the button doesn't sit right on the
        // gesture bar at the end of the scroll.
        Spacer(Modifier.height(56.dp))
    }
}
