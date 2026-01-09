package com.bluemix.cashio.presentation.keyword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.bluemix.cashio.ui.components.defaults.CashioRadius
import com.bluemix.cashio.ui.components.defaults.CashioSpacing

/**
 * An inline notification banner used to display operation results (Success/Error).
 *
 * Designed to appear at the bottom of the screen (e.g., above a FAB or keyboard)
 * to provide context without blocking the main UI flow.
 *
 * @param message The text content to display.
 * @param isError If true, applies Error styling (Red); otherwise, applies Primary styling (Brand color).
 * @param onDismiss Callback triggered when the dismiss action is clicked.
 */
@Composable
fun MessageBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(CashioRadius.small),
        color = if (isError) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CashioSpacing.medium, vertical = CashioSpacing.compact),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = CashioSpacing.small)
            ) {
                Text("Dismiss", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}