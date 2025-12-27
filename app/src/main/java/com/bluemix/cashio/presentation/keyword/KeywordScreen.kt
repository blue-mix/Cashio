package com.bluemix.cashio.presentation.keyword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.R
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.keywordmapping.KeywordMappingViewModel
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.components.defaults.TopBarAction
import com.bluemix.cashio.ui.components.defaults.TopBarIcon
import org.koin.compose.viewmodel.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordMappingScreen(
    onNavigateBack: () -> Unit,
    viewModel: KeywordMappingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categoriesById: Map<String, Category> by remember(state.categories) {
        derivedStateOf {
            (state.categories as? UiState.Success)?.data
                .orEmpty()
                .associateBy { it.id }
        }
    }

    val filteredMappings by remember(state.mappings, state.query) {
        derivedStateOf {
            val all = (state.mappings as? UiState.Success)?.data.orEmpty()
            val q = state.query.trim().lowercase(Locale.ENGLISH)
            if (q.isBlank()) all else all.filter { it.keyword.lowercase(Locale.ENGLISH).contains(q) }
        }
    }

    Scaffold(
        topBar = {
            CashioTopBar(
                title = CashioTopBarTitle.Text("Keyword Mapping"),
                leadingAction = TopBarAction(
                    icon = TopBarIcon.Vector(Icons.Default.Close),
                    onClick = onNavigateBack
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddSheet) {
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
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                placeholder = { Text("Search keywords…") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(12.dp))

            when (val mappingsState = state.mappings) {
                is UiState.Loading, UiState.Idle -> {
                    androidx.compose.foundation.layout.Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                is UiState.Error -> {
                    EmptyOrErrorCard(
                        title = "Couldn’t load mappings",
                        subtitle = mappingsState.message,
                        actionText = "Retry",
                        onAction = viewModel::load
                    )
                }

                is UiState.Success -> {
                    if (filteredMappings.isEmpty()) {
                        val isSearchEmpty = state.query.isBlank()
                        EmptyOrErrorCard(
                            title = if (isSearchEmpty) "No mappings yet" else "No matches",
                            subtitle = if (isSearchEmpty)
                                "Add keywords like “swiggy”, “uber”, “amazon” to auto-categorize."
                            else
                                "Try a different search term.",
                            actionText = if (isSearchEmpty) "Add Mapping" else "Clear Search",
                            onAction = {
                                if (isSearchEmpty) viewModel.openAddSheet()
                                else viewModel.onQueryChange("")
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 96.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredMappings, key = { it.id }) { mapping ->
                                MappingRow(
                                    keyword = mapping.keyword,
                                    categoryLabel = categoriesById[mapping.categoryId]?.name
                                        ?: mapping.categoryId,
                                    priority = mapping.priority,
                                    onEdit = { viewModel.openEditSheet(mapping) },
                                    onDelete = { viewModel.requestDelete(mapping) }
                                )
                            }
                        }
                    }
                }
            }

            val bannerMessage = state.errorMessage ?: state.operationMessage
            if (bannerMessage != null) {
                Spacer(Modifier.height(8.dp))
                MessageBanner(
                    message = bannerMessage,
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
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = viewModel::closeSheet,
            sheetState = sheetState
        ) {
            MappingEditorSheet(
                state = state,
                categories = (state.categories as? UiState.Success)?.data.orEmpty(),
                onKeywordChange = viewModel::setKeyword,
                onCategoryChange = viewModel::setCategoryId,
                onPriorityChange = viewModel::setPriority,
                onSave = viewModel::save,
                onCancel = viewModel::closeSheet
            )
        }
    }
}
