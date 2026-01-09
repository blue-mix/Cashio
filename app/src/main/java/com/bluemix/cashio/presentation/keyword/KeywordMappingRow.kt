package com.bluemix.cashio.presentation.keyword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioShapes
import com.bluemix.cashio.ui.components.defaults.CashioSpacing

/**
 * A list item representing a single keyword mapping rule.
 *
 * Displays the keyword, its target category, and priority level.
 * Includes actions to Edit or Delete the rule.
 *
 * @param keyword The trigger keyword string.
 * @param categoryLabel The name of the category this keyword maps to.
 * @param priority The resolution priority (1-10).
 * @param onEdit Callback when the edit action is triggered.
 * @param onDelete Callback when the delete action is triggered.
 */
@Composable
fun MappingRow(
    keyword: String,
    categoryLabel: String,
    priority: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit,
        cornerRadius = CashioShapes.card,
        showBorder = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = keyword,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(CashioSpacing.xxs)) // 2dp
                Text(
                    text = "Category: $categoryLabel  •  Priority: $priority",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    painter = painterResource(R.drawable.edit),
                    contentDescription = "Edit",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}