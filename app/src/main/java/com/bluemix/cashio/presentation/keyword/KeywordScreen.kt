
package com.bluemix.cashio.presentation.keywordmapping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.bluemix.cashio.components.CashioCard
import com.bluemix.cashio.components.CashioTopBar
import com.bluemix.cashio.components.CashioTopBarTitle
import com.bluemix.cashio.components.TopBarAction
import com.bluemix.cashio.presentation.common.UiState
import org.koin.compose.viewmodel.koinViewModel
import com.bluemix.cashio.R
import com.bluemix.cashio.components.TopBarIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordMappingScreen(
    onNavigateBack: () -> Unit,
    viewModel: KeywordMappingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val mappings = remember(state.mappings, state.query) {
        val data = (state.mappings as? UiState.Success)?.data.orEmpty()
        val q = state.query.trim().lowercase()
        if (q.isBlank()) data else data.filter { it.keyword.lowercase().contains(q) }
    }

    Scaffold(
        topBar = {
            CashioTopBar(
                title = CashioTopBarTitle.Text("Keyword Mapping"),
                leadingAction = TopBarAction(icon = TopBarIcon.Vector(Icons.Default.Close),onNavigateBack) ,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddSheet() }) {
                Icon(Icons.Default.Add, contentDescription = "Add mapping")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(painter = painterResource(R.drawable.search), contentDescription = null,  Modifier.size(20.dp)) },
                placeholder = { Text("Search keywords…") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(12.dp))

            when (state.mappings) {
                is UiState.Loading, UiState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    val msg = (state.mappings as UiState.Error).message
                    EmptyOrErrorCard(
                        title = "Couldn’t load mappings",
                        subtitle = msg,
                        actionText = "Retry",
                        onAction = viewModel::load
                    )
                }

                is UiState.Success -> {
                    if (mappings.isEmpty()) {
                        EmptyOrErrorCard(
                            title = if (state.query.isBlank()) "No mappings yet" else "No matches",
                            subtitle = if (state.query.isBlank())
                                "Add keywords like “swiggy”, “uber”, “amazon” to auto-categorize."
                            else "Try a different search term.",
                            actionText = if (state.query.isBlank()) "Add Mapping" else "Clear Search",
                            onAction = {
                                if (state.query.isBlank()) viewModel.openAddSheet()
                                else viewModel.onQueryChange("")
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(mappings, key = { it.id }) { mapping ->
                                MappingRow(
                                    keyword = mapping.keyword,
                                    categoryId = mapping.categoryId,
                                    priority = mapping.priority,
                                    categoryNameProvider = { catId ->
                                        val cats = (state.categories as? UiState.Success)?.data.orEmpty()
                                        cats.firstOrNull { it.id == catId }?.name ?: catId
                                    },
                                    onEdit = { viewModel.openEditSheet(mapping) },
                                    onDelete = { viewModel.requestDelete(mapping) }
                                )
                            }
                        }
                    }
                }
            }

            if (state.operationMessage != null || state.errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                MessageBanner(
                    message = state.errorMessage ?: state.operationMessage.orEmpty(),
                    isError = state.errorMessage != null,
                    onDismiss = viewModel::clearMessages
                )
            }
        }
    }

    state.confirmDelete?.let { target ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Delete mapping?") },
            text = { Text("Keyword “${target.keyword}” will stop auto-categorizing.") },
            confirmButton = { TextButton(onClick = viewModel::deleteConfirmed) { Text("Delete") } },
            dismissButton = { TextButton(onClick = viewModel::dismissDelete) { Text("Cancel") } }
        )
    }

    if (state.isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeSheet,
            sheetState = sheetState
        ) {
            MappingEditorSheet(
                state = state,
                onKeywordChange = viewModel::setKeyword,
                onCategoryChange = viewModel::setCategoryId,
                onPriorityChange = viewModel::setPriority,
                onSave = viewModel::save,
                onCancel = viewModel::closeSheet
            )
        }
    }
}

@Composable
private fun MappingRow(
    keyword: String,
    categoryId: String,
    priority: Int,
    categoryNameProvider: (String) -> String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit,
        padding = PaddingValues(14.dp),
        cornerRadius = 16.dp,
        showBorder = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    keyword,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Category: ${categoryNameProvider(categoryId)}  •  Priority: $priority",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onEdit) {
                Icon(painter = painterResource(R.drawable.edit), contentDescription = "Edit",  Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(painter = painterResource(R.drawable.delete), contentDescription = "Delete" , Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MappingEditorSheet(
    state: KeywordMappingState,
    onKeywordChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val categories = (state.categories as? UiState.Success)?.data.orEmpty()
    var categoryExpanded by remember { mutableStateOf(false) }

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
            val selectedName = categories.firstOrNull { it.id == state.categoryId }?.name ?: ""

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
                modifier = Modifier.weight(1f)
            ) { Text(if (state.isEditMode) "Save" else "Add") }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EmptyOrErrorCard(
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(22.dp),
        cornerRadius = 16.dp,
        showBorder = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Button(onClick = onAction) { Text(actionText) }
        }
    }
}

@Composable
private fun MessageBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                message,
                modifier = Modifier.weight(1f),
                color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}
