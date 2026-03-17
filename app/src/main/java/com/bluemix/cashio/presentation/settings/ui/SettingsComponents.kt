//package com.bluemix.cashio.presentation.settings.ui
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Switch
//import androidx.compose.material3.SwitchDefaults
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import com.bluemix.cashio.R
//import com.bluemix.cashio.presentation.settings.vm.SettingsMessage
//import com.bluemix.cashio.ui.defaults.CashioCard
//import com.bluemix.cashio.ui.defaults.CashioIcon
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//
///* -------------------------------------------------------------------------- */
///* Permissions                                                                 */
///* -------------------------------------------------------------------------- */
//
//@Composable
//fun PermissionsSection(
//    smsGranted: Boolean,
//    notificationGranted: Boolean,
//    onSmsClick: () -> Unit,
//    onNotificationClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    CashioCard(modifier = modifier.fillMaxWidth()) {
//        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.default)) {
//            SectionLabel("Data Sources")
//
//            PermissionRow(
//                icon = CashioIcon.Drawable(R.drawable.sms),
//                title = "SMS Parsing",
//                description = "Reads transaction SMS for auto-detecting expenses.",
//                granted = smsGranted,
//                onClick = onSmsClick
//            )
//            PermissionRow(
//                icon = CashioIcon.Drawable(R.drawable.notificationbell),
//                title = "Notification Access",
//                description = "Reads banking notifications for real-time tracking.",
//                granted = notificationGranted,
//                onClick = onNotificationClick
//            )
//        }
//    }
//}
//
//@Composable
//private fun PermissionRow(
//    icon: CashioIcon,
//    title: String,
//    description: String,
//    granted: Boolean,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .heightIn(min = 64.dp)
//            .clip(RoundedCornerShape(CashioRadius.small))
//            .clickable(onClick = onClick)
//            .padding(
//                horizontal = CashioSpacing.small,
//                vertical = CashioSpacing.compact
//            ),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Row(
//            modifier = Modifier.weight(1f),
//            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconBadge(icon = icon)
//            Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.SemiBold
//                )
//                Text(
//                    text = description,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
//        Switch(
//            checked = granted,
//            onCheckedChange = null,
//            colors = SwitchDefaults.colors(
//                checkedThumbColor = MaterialTheme.colorScheme.onPrimary
//            )
//        )
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Navigation entry cards                                                      */
///* -------------------------------------------------------------------------- */
//
//@Composable
//fun CategoriesEntryCard(
//    hasCategories: Boolean,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    NavigationCard(
//        title = "Categories",
//        description = if (hasCategories) "Manage category list, icons, and colors."
//        else "No categories found.",
//        onClick = onClick,
//        modifier = modifier
//    )
//}
//
//@Composable
//fun KeywordMappingEntryCard(
//    hasMappings: Boolean,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    NavigationCard(
//        title = "Keyword Mapping",
//        description = if (hasMappings) "Manage auto-categorization rules."
//        else "No mappings yet.",
//        onClick = onClick,
//        modifier = modifier
//    )
//}
//
//@Composable
//private fun NavigationCard(
//    title: String,
//    description: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    CashioCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xs)) {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold
//                )
//                Text(
//                    text = description,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//            Icon(
//                painter = painterResource(R.drawable.chevron),
//                contentDescription = "Navigate",
//                tint = MaterialTheme.colorScheme.outline
//            )
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Appearance                                                                  */
///* -------------------------------------------------------------------------- */
//
//@Composable
//fun AppearanceSection(
//    isDarkMode: Boolean,
//    onDarkModeToggle: (Boolean) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    CashioCard(modifier = modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconBadge(icon = CashioIcon.Drawable(R.drawable.pallete))
//
//                Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xs)) {
//                    Text(
//                        text = "Appearance",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                    Text(
//                        text = if (isDarkMode) "Dark mode is ON" else "Light mode is ON",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//            Switch(checked = isDarkMode, onCheckedChange = onDarkModeToggle)
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* About                                                                       */
///* -------------------------------------------------------------------------- */
//
//@Composable
//fun AboutSection(
//    onFaqClick: () -> Unit,
//    onSupportClick: () -> Unit,
//    onRateUsClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    CashioCard(modifier = modifier.fillMaxWidth()) {
//        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium)) {
//            SectionLabel("About")
//            SettingsLinkRow("FAQs", "Answers to common questions.", onFaqClick)
//            SettingsLinkRow("Support Us", "Share feedback or report issues.", onSupportClick)
//            SettingsLinkRow("Rate on Play Store", "Tell others what you think.", onRateUsClick)
//        }
//    }
//}
//
//@Composable
//fun SettingsLinkRow(
//    title: String,
//    subtitle: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(CashioRadius.small))
//            .clickable(onClick = onClick)
//            .padding(horizontal = CashioSpacing.small, vertical = CashioSpacing.compact),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = FontWeight.SemiBold
//            )
//            Text(
//                text = subtitle,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//        Icon(
//            painter = painterResource(R.drawable.chevron),
//            contentDescription = null,
//            tint = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Danger zone                                                                 */
///* -------------------------------------------------------------------------- */
//
//@Composable
//fun DangerZoneSection(
//    onClearDataClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    CashioCard(modifier = modifier.fillMaxWidth()) {
//        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium)) {
//            Text(
//                text = "Danger Zone",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.colorScheme.error
//            )
//
//            Surface(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(CashioRadius.small))
//                    .clickable(onClick = onClearDataClick),
//                shape = RoundedCornerShape(CashioRadius.small),
//                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(
//                            horizontal = CashioSpacing.medium,
//                            vertical = CashioSpacing.default
//                        ),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column(
//                        modifier = Modifier.weight(1f),
//                        verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)
//                    ) {
//                        Text(
//                            text = "Clear All Data",
//                            style = MaterialTheme.typography.bodyLarge,
//                            fontWeight = FontWeight.SemiBold,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                        Text(
//                            text = "Delete all transactions, categories, and settings",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                    Icon(
//                        painter = painterResource(R.drawable.delete),
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Message banner                                                              */
///* -------------------------------------------------------------------------- */
//
//@Composable
//fun SettingsMessageBanner(
//    message: SettingsMessage,
//    onDismiss: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val (bg, fg, text) = when (message) {
//        is SettingsMessage.Error -> Triple(
//            MaterialTheme.colorScheme.errorContainer,
//            MaterialTheme.colorScheme.onErrorContainer,
//            message.text
//        )
//        is SettingsMessage.Success -> Triple(
//            MaterialTheme.colorScheme.primaryContainer,
//            MaterialTheme.colorScheme.onPrimaryContainer,
//            message.text
//        )
//    }
//
//    Surface(
//        modifier = modifier
//            .fillMaxWidth()
//            .heightIn(min = 44.dp),
//        shape = RoundedCornerShape(CashioRadius.small),
//        color = bg,
//        tonalElevation = 1.dp
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = CashioSpacing.medium, vertical = CashioSpacing.small),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = text,
//                modifier = Modifier.weight(1f),
//                style = MaterialTheme.typography.bodySmall,
//                color = fg,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//            TextButton(
//                onClick = onDismiss,
//                contentPadding = PaddingValues(horizontal = CashioSpacing.small)
//            ) {
//                Text("Dismiss", style = MaterialTheme.typography.labelSmall, color = fg)
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Shared primitives                                                           */
///* -------------------------------------------------------------------------- */
//
///** Circular icon badge used in permission rows and appearance section. */
//@Composable
//private fun IconBadge(
//    icon: CashioIcon,
//    modifier: Modifier = Modifier
//) {
//    Surface(
//        modifier = modifier,
//        shape = CircleShape,
//        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
//    ) {
//        Box(
//            modifier = Modifier.size(48.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            CashioIcon(
//                icon = icon,
//                tint = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//    }
//}
//
///** Reusable bold section label inside cards. */
//@Composable
//private fun SectionLabel(text: String) {
//    Text(
//        text = text,
//        style = MaterialTheme.typography.titleMedium,
//        fontWeight = FontWeight.SemiBold
//    )
//}

package com.bluemix.cashio.presentation.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.presentation.common.pressScale
import com.bluemix.cashio.presentation.settings.vm.SettingsMessage
import com.bluemix.cashio.ui.defaults.CashioBorder
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioIcon
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing

/* -------------------------------------------------------------------------- */
/* Permissions                                                                 */
/* -------------------------------------------------------------------------- */

@Composable
fun PermissionsSection(
    smsGranted: Boolean,
    notificationGranted: Boolean,
    onSmsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.md)) {
            SectionLabel("Data Sources")
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
private fun PermissionRow(
    icon: CashioIcon,
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(CashioRadius.small))
            .pressScale(onClick = onClick)
            .padding(horizontal = CashioSpacing.xs, vertical = CashioSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = icon)
            Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
        }
        Switch(
            checked = granted,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary)
        )
    }
}

/* -------------------------------------------------------------------------- */
/* Navigation entry cards                                                      */
/* -------------------------------------------------------------------------- */

@Composable
fun CategoriesEntryCard(
    hasCategories: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationCard(
        title = "Categories",
        description = if (hasCategories) "Manage category list, icons, and colors." else "No categories found.",
        onClick = onClick, modifier = modifier
    )
}

@Composable
fun KeywordMappingEntryCard(
    hasMappings: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationCard(
        title = "Keyword Mapping",
        description = if (hasMappings) "Manage auto-categorization rules." else "No mappings yet.",
        onClick = onClick, modifier = modifier
    )
}

@Composable
private fun NavigationCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painterResource(R.drawable.chevron),
                "Navigate",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Appearance                                                                  */
/* -------------------------------------------------------------------------- */

@Composable
fun AppearanceSection(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(
                horizontalArrangement =Arrangement.spacedBy(CashioSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(icon = CashioIcon.Drawable(R.drawable.pallete))
                Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (isDarkMode) "Dark mode is ON" else "Light mode is ON",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(checked = isDarkMode, onCheckedChange = onDarkModeToggle)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* About                                                                       */
/* -------------------------------------------------------------------------- */

@Composable
fun AboutSection(
    onFaqClick: () -> Unit,
    onSupportClick: () -> Unit,
    onRateUsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm)) {
            SectionLabel("About")
            SettingsLinkRow("FAQs", "Answers to common questions.", onFaqClick)
            SettingsLinkRow("Support Us", "Share feedback or report issues.", onSupportClick)
            SettingsLinkRow("Rate on Play Store", "Tell others what you think.", onRateUsClick)
        }
    }
}

@Composable
fun SettingsLinkRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CashioRadius.small))
            .pressScale(onClick = onClick)
            .padding(horizontal = CashioSpacing.xs, vertical = CashioSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
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
            painterResource(R.drawable.chevron),
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* -------------------------------------------------------------------------- */
/* Danger zone                                                                 */
/* -------------------------------------------------------------------------- */

@Composable
fun DangerZoneSection(onClearDataClick: () -> Unit, modifier: Modifier = Modifier) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm)) {
            Text(
                "Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CashioRadius.small))
                    .pressScale(onClick = onClearDataClick),
                shape = RoundedCornerShape(CashioRadius.small),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                border = CashioBorder.stroke(),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CashioSpacing.md, vertical = CashioSpacing.sm),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)
                    ) {
                        Text(
                            "Clear All Data",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Delete all transactions, categories, and settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        painterResource(R.drawable.delete),
                        null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Message banner — flat, zero elevation                                       */
/* -------------------------------------------------------------------------- */

@Composable
fun SettingsMessageBanner(
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
        shape = RoundedCornerShape(CashioRadius.small),
        color = bg,
        border = CashioBorder.stroke(),
        tonalElevation = 0.dp, shadowElevation = 0.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = CashioSpacing.sm, vertical = CashioSpacing.xs),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Text(
                text,
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = fg,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = CashioSpacing.xs)
            ) {
                Text("Dismiss", style = MaterialTheme.typography.labelSmall, color = fg)
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Shared primitives                                                           */
/* -------------------------------------------------------------------------- */

/** Flat icon badge — surfaceContainerLow fill, border, no shadow. */
@Composable
private fun IconBadge(icon: CashioIcon, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        tonalElevation = 0.dp, shadowElevation = 0.dp
    ) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            CashioIcon(
                icon = icon,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}