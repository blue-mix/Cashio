package com.bluemix.cashio.presentation.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.presentation.settings.vm.SettingsMessage
import com.bluemix.cashio.ui.theme.CashioRadius
import com.bluemix.cashio.ui.theme.CashioSpacing

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
        // Used CashioRadius.small (12dp)
        shape = RoundedCornerShape(CashioRadius.small),
        color = bg,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Used CashioSpacing.medium (12dp) and CashioSpacing.small (8dp)
                .padding(horizontal = CashioSpacing.medium, vertical = CashioSpacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = fg,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            TextButton(
                onClick = onDismiss,
                // Ensure the button touch target is still large enough but visually aligned
                contentPadding = PaddingValues(horizontal = CashioSpacing.small)
            ) {
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.labelSmall,
                    color = fg
                )
            }
        }
    }
}