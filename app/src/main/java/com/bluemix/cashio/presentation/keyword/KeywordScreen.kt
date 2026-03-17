package com.bluemix.cashio.presentation.keyword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.bluemix.cashio.ui.components.cards.StateCard
import com.bluemix.cashio.ui.components.cards.StateCardAction
import com.bluemix.cashio.ui.components.cards.StateCardVariant
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.defaults.CashioTopBar
import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.defaults.TopBarAction
import com.bluemix.cashio.ui.defaults.TopBarIcon
import org.koin.compose.viewmodel.koinViewModel
import java.util.Locale

/**
 * Keyword-mapping management screen.
 *
 * ViewModel interaction is confined here.
 * All child composables are stateless.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordMappingScreen(
    onNavigateBack: () -> Unit,
    viewModel: KeywordMappingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Derived state — category lookup map
    val categoriesById: Map<String, Category> by remember(state.categories) {
        derivedStateOf {
            (state.categories as? UiState.Success)?.data
                .orEmpty()
                .associateBy { it.id }
        }
    }

    // Client-side search
    val filteredMappings by remember(state.mappings, state.query) {
        derivedStateOf {
            val all = (state.mappings as? UiState.Success)?.data.orEmpty()
            val q = state.query.trim().lowercase(Locale.ENGLISH)
            if (q.isBlank()) all
            else all.filter { it.keyword.lowercase(Locale.ENGLISH).contains(q) }
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
                modifier = Modifier.padding(horizontal = CashioPadding.screen)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddSheet,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(CashioRadius.small)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add mapping")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = CashioPadding.screen)
        ) {
            Spacer(Modifier.height(CashioSpacing.sm))

            // Search
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
                shape = RoundedCornerShape(CashioRadius.small)
            )

            Spacer(Modifier.height(CashioSpacing.sm))

            // Content
            when (val mappingsState = state.mappings) {
                is UiState.Loading, UiState.Idle -> {
                    StateCard(variant = StateCardVariant.LOADING, animated = true)
                }

                is UiState.Error -> {
                    StateCard(
                        variant = StateCardVariant.ERROR,
                        title = "Couldn't load mappings",
                        message = mappingsState.message,
                        action = StateCardAction("Retry", viewModel::load)
                    )
                }

                is UiState.Success -> {
                    if (filteredMappings.isEmpty()) {
                        val isSearchEmpty = state.query.isBlank()
                        StateCard(
                            variant = StateCardVariant.EMPTY,
                            emoji = if (isSearchEmpty) "⌨️" else "🔍",
                            title = if (isSearchEmpty) "No mappings yet" else "No matches",
                            message = if (isSearchEmpty)
                                "Add keywords like 'swiggy' or 'uber' to auto-categorize SMS."
                            else "Try a different search term.",
                            action = StateCardAction(
                                text = if (isSearchEmpty) "Add Mapping" else "Clear Search",
                                onClick = {
                                    if (isSearchEmpty) viewModel.openAddSheet()
                                    else viewModel.onQueryChange("")
                                }
                            )
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
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
        }

        // Banner overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(CashioPadding.screen),
            contentAlignment = Alignment.BottomCenter
        ) {
            val bannerMessage = state.errorMessage ?: state.operationMessage
            if (bannerMessage != null) {
                MessageBanner(
                    message = bannerMessage,
                    isError = state.errorMessage != null,
                    onDismiss = viewModel::clearMessages
                )
            }
        }
    }

    // Delete confirmation
    state.confirmDelete?.let { target ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Delete mapping?") },
            text = { Text("Keyword '${target.keyword}' will stop auto-categorizing future expenses.") },
            confirmButton = {
                TextButton(onClick = viewModel::deleteConfirmed) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) { Text("Cancel") }
            }
        )
    }

    // Editor sheet
    if (state.isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeSheet,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = CashioRadius.large, topEnd = CashioRadius.large)
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