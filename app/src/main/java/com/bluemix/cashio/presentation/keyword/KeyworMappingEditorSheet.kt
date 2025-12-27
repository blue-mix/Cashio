package com.bluemix.cashio.presentation.keyword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.presentation.keywordmapping.KeywordMappingState

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
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (state.isEditMode) "Edit Mapping" else "Add Mapping",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = state.keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Keyword") },
            placeholder = { Text("e.g., swiggy, uber, amazon") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

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
                placeholder = { Text("Select category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                shape = RoundedCornerShape(14.dp)
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

        Text(
            text = "Priority: ${state.priority}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = state.priority.toFloat(),
            onValueChange = { onPriorityChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8
        )

        state.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) { Text("Cancel") }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) {
                Text(
                    when {
                        state.isSaving && state.isEditMode -> "Saving..."
                        state.isSaving && !state.isEditMode -> "Adding..."
                        state.isEditMode -> "Save"
                        else -> "Add"
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
