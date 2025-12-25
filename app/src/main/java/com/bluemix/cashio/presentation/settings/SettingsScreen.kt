
package com.bluemix.cashio.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.R
import com.bluemix.cashio.components.CashioCard   // ✅ IMPORTANT: missing before
import com.bluemix.cashio.components.CashioIcon
import com.bluemix.cashio.components.CashioTopBar
import com.bluemix.cashio.components.CashioTopBarTitle
import com.bluemix.cashio.core.util.PermissionHelper
import com.bluemix.cashio.presentation.common.UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToKeywordMapping: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh permission status on enter + every time user comes back from Settings screens.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val status = PermissionHelper.getPermissionStatus(context)
                viewModel.refreshPermissionStatus(
                    smsGranted = status.smsGranted,
                    notificationGranted = status.notificationGranted
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            CashioTopBar(
                title = CashioTopBarTitle.Text("Settings"),
                contentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = navBarPadding + 72.dp
                )
            ) {
                item {
                    AppearanceSection(
                        isDarkMode = state.darkModeEnabled,
                        onDarkModeToggle = { enabled ->
                            viewModel.setDarkMode(enabled)
                        }
                    )
                }

                item {
                    PermissionsSection(
                        smsGranted = state.smsPermissionGranted,
                        notificationGranted = state.notificationAccessGranted,
                        onSmsClick = {
                            PermissionHelper.openAppSettings(context)
                        },
                        onNotificationClick = {
                            PermissionHelper.openNotificationAccessSettings(context)
                        }
                    )
                }

                item {
                    KeywordMappingEntryCard(
                        hasMappings = (state.keywordMappings as? UiState.Success)
                            ?.data
                            ?.isNotEmpty() == true,
                        onClick = onNavigateToKeywordMapping
                    )
                }

                item {
                    AboutSection(
                        onFaqClick = { /* TODO */ },
                        onSupportClick = { /* TODO */ },
                        onRateUsClick = { /* TODO */ }
                    )
                }

                item {
                    state.message?.let { msg ->
                        SettingsMessageBanner(
                            message = msg,
                            onDismiss = { viewModel.dismissMessage() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionsSection(
    smsGranted: Boolean,
    notificationGranted: Boolean,
    onSmsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Data Sources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            PermissionRow(
                icon =  CashioIcon.Drawable(R.drawable.sms),
                title = "SMS Parsing",
                description = "Reads transaction SMS for auto-detecting expenses.",
                enabled = smsGranted,
                onRowClick = onSmsClick
            )

            PermissionRow(
                icon = CashioIcon.Drawable(R.drawable.notificationbell),
                title = "Notification Access",
                description = "Reads UPI/banking notifications for real-time tracking.",
                enabled = notificationGranted,
                onRowClick = onNotificationClick
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: CashioIcon,
    title: String,
    description: String,
    enabled: Boolean,
    onRowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onRowClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CashioIcon(
                        icon = icon,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Switch(
            checked = enabled,
            onCheckedChange = { onRowClick() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun AppearanceSection(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CashioIcon(
                            icon = CashioIcon.Drawable(R.drawable.pallete),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isDarkMode) "Dark mode is ON" else "Light mode is ON",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isDarkMode,
                onCheckedChange = onDarkModeToggle
            )
        }

    }
}

@Composable
private fun KeywordMappingEntryCard(
    hasMappings: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Keyword Mapping",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (hasMappings) "Manage keywords mapped to categories."
                    else "No mappings yet. Configure auto-categorization rules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon( painter = painterResource(R.drawable.chevron), contentDescription = null)
        }
    }
}

@Composable
private fun AboutSection(
    onFaqClick: () -> Unit,
    onSupportClick: () -> Unit,
    onRateUsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SettingsLinkRow("FAQs", "Answers to common questions.", onFaqClick)
            SettingsLinkRow("Support Us", "Share feedback or report issues.", onSupportClick)
            SettingsLinkRow("Rate on Play Store", "Tell others what you think.", onRateUsClick)
        }
    }
}

@Composable
private fun SettingsLinkRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            painter = painterResource(R.drawable.chevron),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsMessageBanner(
    message: SettingsMessage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bg, fg, text) = when (message) {
        is SettingsMessage.Error -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            message.text
        )

        is SettingsMessage.Success -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            message.text
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = fg,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            TextButton(onClick = onDismiss) {
                Text("Dismiss", style = MaterialTheme.typography.labelSmall, color = fg)
            }
        }
    }
}
