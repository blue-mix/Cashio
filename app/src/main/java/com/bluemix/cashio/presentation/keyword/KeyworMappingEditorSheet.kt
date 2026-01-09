package com.bluemix.cashio.presentation.keyword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.ui.components.defaults.CashioRadius
import com.bluemix.cashio.ui.components.defaults.CashioSpacing

/**
 * Bottom sheet form for creating or editing a Keyword Mapping rule.
 *
 * @param state The current UI state containing the form data (keyword, priority, etc.).
 * @param categories List of available categories for the dropdown.
 * @param onKeywordChange Callback for keyword text input.
 * @param onCategoryChange Callback when a category is selected.
 * @param onPriorityChange Callback for priority slider adjustment.
 * @param onSave Callback when the Save/Add button is clicked.
 * @param onCancel Callback when the Cancel button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingEditorSheet(
    state: KeywordMappingState,
    categories: List<Category>,
    onKeywordChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var categoryExpanded by remember(state.isSheetOpen) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CashioSpacing.default)
            .padding(bottom = CashioSpacing.huge),
        verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
    ) {
        Text(
            text = if (state.isEditMode) "Edit Mapping" else "Add Mapping",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // 1. Keyword Input
        OutlinedTextField(
            value = state.keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Keyword") },
            placeholder = { Text("e.g., swiggy, uber, amazon") },
            singleLine = true,
            shape = RoundedCornerShape(CashioRadius.small)
        )

        // 2. Category Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            val selectedName = categories.firstOrNull { it.id == state.categoryId }?.name.orEmpty()

            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                shape = RoundedCornerShape(CashioRadius.small)
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text("${cat.icon}  ${cat.name}") },
                        onClick = {
                            onCategoryChange(cat.id)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // 3. Priority Slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Priority: ${state.priority}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }

        Slider(
            value = state.priority.toFloat(),
            onValueChange = { onPriorityChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
        )

        // Error Message
        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(CashioRadius.mediumSmall)
            ) { Text("Cancel") }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving,
                shape = RoundedCornerShape(CashioRadius.mediumSmall)
            ) {
                Text(if (state.isSaving) "Saving..." else if (state.isEditMode) "Save" else "Add")
            }
        }
    }
}