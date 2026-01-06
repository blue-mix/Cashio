package com.bluemix.cashio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.presentation.settings.ui.PermissionRow
import com.bluemix.cashio.presentation.settings.ui.SettingsLinkRow
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioIcon
import com.bluemix.cashio.ui.theme.CashioSpacing

@Composable
fun PermissionsSection(
    smsGranted: Boolean,
    notificationGranted: Boolean,
    onSmsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.default)) {
            Text(
                "Data Sources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            PermissionRow(
                icon = CashioIcon.Drawable(R.drawable.sms),
                title = "SMS Parsing",
                description = "Reads transaction SMS for auto-detecting expenses.",
                granted = smsGranted,
                onClick = onSmsClick
            )

            PermissionRow(
                icon = CashioIcon.Drawable(R.drawable.notificationbell),
                title = "Notification Access",
                description = "Reads banking notifications for real-time tracking.",
                granted = notificationGranted,
                onClick = onNotificationClick
            )
        }
    }
}

@Composable
fun AppearanceSection(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        CashioIcon(
                            icon = CashioIcon.Drawable(R.drawable.pallete),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xs)) {
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

            Switch(checked = isDarkMode, onCheckedChange = onDarkModeToggle)
        }
    }
}

@Composable
fun AboutSection(
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
