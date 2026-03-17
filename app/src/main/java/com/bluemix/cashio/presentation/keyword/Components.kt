//package com.bluemix.cashio.presentation.keyword
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenuDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Slider
//import androidx.compose.material3.SliderDefaults
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import com.bluemix.cashio.R
//import com.bluemix.cashio.domain.model.Category
//import com.bluemix.cashio.ui.defaults.CashioCard
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioShapes
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//
///* -------------------------------------------------------------------------- */
///* Mapping list item                                                           */
///* -------------------------------------------------------------------------- */
//
///**
// * Row representing a single keyword-mapping rule.
// *
// * Stateless — the screen provides all data and callbacks.
// */
//@Composable
//fun MappingRow(
//    keyword: String,
//    categoryLabel: String,
//    priority: Int,
//    onEdit: () -> Unit,
//    onDelete: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    CashioCard(
//        modifier = modifier.fillMaxWidth(),
//        onClick = onEdit,
//        cornerRadius = CashioShapes.card,
//        showBorder = true
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = keyword,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Spacer(Modifier.height(CashioSpacing.xxs))
//                Text(
//                    text = "Category: $categoryLabel  •  Priority: $priority",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            IconButton(onClick = onEdit) {
//                Icon(
//                    painter = painterResource(R.drawable.edit),
//                    contentDescription = "Edit",
//                    modifier = Modifier.size(20.dp),
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//            IconButton(onClick = onDelete) {
//                Icon(
//                    painter = painterResource(R.drawable.delete),
//                    contentDescription = "Delete",
//                    modifier = Modifier.size(20.dp),
//                    tint = MaterialTheme.colorScheme.error
//                )
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Editor sheet                                                                */
///* -------------------------------------------------------------------------- */
//
///**
// * Bottom-sheet form for creating or editing a keyword-mapping rule.
// *
// * Stateless — the parent owns [KeywordMappingState].
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MappingEditorSheet(
//    state: KeywordMappingState,
//    categories: List<Category>,
//    onKeywordChange: (String) -> Unit,
//    onCategoryChange: (String) -> Unit,
//    onPriorityChange: (Int) -> Unit,
//    onSave: () -> Unit,
//    onCancel: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var categoryExpanded by remember(state.isSheetOpen) { mutableStateOf(false) }
//
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = CashioSpacing.default)
//            .padding(bottom = CashioSpacing.huge),
//        verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//    ) {
//        Text(
//            text = if (state.isEditMode) "Edit Mapping" else "Add Mapping",
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.SemiBold
//        )
//
//        // Keyword
//        OutlinedTextField(
//            value = state.keyword,
//            onValueChange = onKeywordChange,
//            modifier = Modifier.fillMaxWidth(),
//            label = { Text("Keyword") },
//            placeholder = { Text("e.g., swiggy, uber, amazon") },
//            singleLine = true,
//            shape = RoundedCornerShape(CashioRadius.small)
//        )
//
//        // Category dropdown
//        ExposedDropdownMenuBox(
//            expanded = categoryExpanded,
//            onExpandedChange = { categoryExpanded = !categoryExpanded },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            val selectedName = categories.firstOrNull { it.id == state.categoryId }?.name.orEmpty()
//
//            OutlinedTextField(
//                value = selectedName,
//                onValueChange = {},
//                readOnly = true,
//                modifier = Modifier.menuAnchor().fillMaxWidth(),
//                label = { Text("Category") },
//                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
//                shape = RoundedCornerShape(CashioRadius.small)
//            )
//
//            ExposedDropdownMenu(
//                expanded = categoryExpanded,
//                onDismissRequest = { categoryExpanded = false }
//            ) {
//                categories.forEach { cat ->
//                    DropdownMenuItem(
//                        text = { Text("${cat.icon}  ${cat.name}") },
//                        onClick = {
//                            onCategoryChange(cat.id)
//                            categoryExpanded = false
//                        }
//                    )
//                }
//            }
//        }
//
//        // Priority slider
//        Text(
//            text = "Priority: ${state.priority}",
//            style = MaterialTheme.typography.bodyMedium,
//            fontWeight = FontWeight.Medium
//        )
//
//        Slider(
//            value = state.priority.toFloat(),
//            onValueChange = { onPriorityChange(it.toInt()) },
//            valueRange = 1f..10f,
//            steps = 8,
//            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
//        )
//
//        // Error
//        state.errorMessage?.let {
//            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
//        }
//
//        // Actions
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//        ) {
//            OutlinedButton(
//                onClick = onCancel,
//                modifier = Modifier.weight(1f),
//                shape = RoundedCornerShape(CashioRadius.mediumSmall)
//            ) { Text("Cancel") }
//
//            Button(
//                onClick = onSave,
//                modifier = Modifier.weight(1f),
//                enabled = !state.isSaving,
//                shape = RoundedCornerShape(CashioRadius.mediumSmall)
//            ) {
//                Text(
//                    if (state.isSaving) "Saving..."
//                    else if (state.isEditMode) "Save"
//                    else "Add"
//                )
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Message banner                                                              */
///* -------------------------------------------------------------------------- */
//
///**
// * Inline notification banner for operation results.
// */
//@Composable
//fun MessageBanner(
//    message: String,
//    isError: Boolean,
//    onDismiss: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Surface(
//        modifier = modifier,
//        shape = RoundedCornerShape(CashioRadius.small),
//        color = if (isError) MaterialTheme.colorScheme.errorContainer
//        else MaterialTheme.colorScheme.primaryContainer,
//        tonalElevation = 1.dp
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = CashioSpacing.medium, vertical = CashioSpacing.compact),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//        ) {
//            Text(
//                text = message,
//                modifier = Modifier.weight(1f),
//                color = if (isError) MaterialTheme.colorScheme.onErrorContainer
//                else MaterialTheme.colorScheme.onPrimaryContainer,
//                style = MaterialTheme.typography.bodySmall,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
//            TextButton(
//                onClick = onDismiss,
//                contentPadding = PaddingValues(horizontal = CashioSpacing.small)
//            ) {
//                Text("Dismiss", style = MaterialTheme.typography.labelLarge)
//            }
//        }
//    }
//}
package com.bluemix.cashio.presentation.keyword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioShapes
import com.bluemix.cashio.ui.defaults.CashioSpacing

/* -------------------------------------------------------------------------- */
/* Mapping list item                                                           */
/* -------------------------------------------------------------------------- */

/**
 * Row representing a single keyword-mapping rule.
 *
 * Stateless — the screen provides all data and callbacks.
 */
@Composable
fun MappingRow(
    keyword: String,
    categoryLabel: String,
    priority: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
        cornerRadius = CashioShapes.card,
        showBorder = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = keyword,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(CashioSpacing.xxs))
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

/* -------------------------------------------------------------------------- */
/* Editor sheet                                                                */
/* -------------------------------------------------------------------------- */

/**
 * Bottom-sheet form for creating or editing a keyword-mapping rule.
 *
 * Stateless — the parent owns [KeywordMappingState].
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
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryExpanded by remember(state.isSheetOpen) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CashioSpacing.md)
            .padding(bottom = CashioSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
    ) {
        Text(
            text = if (state.isEditMode) "Edit Mapping" else "Add Mapping",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // Keyword
        OutlinedTextField(
            value = state.keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Keyword") },
            placeholder = { Text("e.g., swiggy, uber, amazon") },
            singleLine = true,
            shape = RoundedCornerShape(CashioRadius.small)
        )

        // Category dropdown
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
                modifier = Modifier.menuAnchor().fillMaxWidth(),
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

        // Priority slider
        Text(
            text = "Priority: ${state.priority}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Slider(
            value = state.priority.toFloat(),
            onValueChange = { onPriorityChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
        )

        // Error
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(CashioRadius.medium)
            ) { Text("Cancel") }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving,
                shape = RoundedCornerShape(CashioRadius.medium)
            ) {
                Text(
                    if (state.isSaving) "Saving..."
                    else if (state.isEditMode) "Save"
                    else "Add"
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Message banner                                                              */
/* -------------------------------------------------------------------------- */

/**
 * Inline notification banner for operation results.
 */
@Composable
fun MessageBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CashioRadius.small),
        color = if (isError) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CashioSpacing.sm, vertical = CashioSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
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
                contentPadding = PaddingValues(horizontal = CashioSpacing.xs)
            ) {
                Text("Dismiss", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}