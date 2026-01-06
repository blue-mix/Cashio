package com.bluemix.cashio.presentation.categories

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.ui.components.cards.StateCard
import com.bluemix.cashio.ui.components.cards.StateCardAction
import com.bluemix.cashio.ui.components.cards.StateCardVariant
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.components.defaults.TopBarAction
import com.bluemix.cashio.ui.components.defaults.TopBarIcon
import com.bluemix.cashio.ui.theme.CashioPadding
import com.bluemix.cashio.ui.theme.CashioRadius
import com.bluemix.cashio.ui.theme.CashioShapes
import com.bluemix.cashio.ui.theme.CashioSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * The main screen for managing transaction categories.
 *
 * Displays a searchable list of categories, allows deletion via long-press,
 * and opens an editor sheet for creating or modifying categories.
 *
 * @param onNavigateBack Callback to pop the current screen from the backstack.
 * @param viewModel The state holder for category operations.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg.text)
        viewModel.clearMessage()
    }

    var pendingDelete by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CashioTopBar(
                title = CashioTopBarTitle.Text("Categories"),
                leadingAction = TopBarAction(
                    icon = TopBarIcon.Vector(Icons.Default.ArrowBack),
                    onClick = onNavigateBack
                ),
                modifier = Modifier.padding(horizontal = CashioPadding.screen)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddCategory,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(CashioRadius.mediumSmall)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val ui = state.categories) {
                UiState.Idle, UiState.Loading -> {
                    StateCard(variant = StateCardVariant.LOADING, animated = true)
                }

                is UiState.Error -> {
                    StateCard(
                        variant = StateCardVariant.ERROR,
                        title = "Failed to load",
                        message = ui.message,
                        action = StateCardAction("Retry", viewModel::loadCategories),
                        modifier = Modifier.padding(CashioPadding.screen)
                    )
                }

                is UiState.Success -> {
                    val query = state.query.trim().lowercase()
                    val items = if (query.isBlank()) ui.data else ui.data.filter {
                        it.name.lowercase().contains(query) || it.icon.contains(query)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
                        contentPadding = PaddingValues(
                            start = CashioPadding.screen,
                            end = CashioPadding.screen,
                            top = CashioSpacing.small,
                            bottom = bottomInset + 96.dp
                        )
                    ) {
                        item {
                            OutlinedTextField(
                                value = state.query,
                                onValueChange = viewModel::setQuery,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(CashioRadius.small),
                                placeholder = { Text("Search category…") }
                            )
                        }

                        if (items.isEmpty()) {
                            item {
                                StateCard(
                                    variant = StateCardVariant.EMPTY,
                                    title = if (state.query.isBlank()) "No categories yet" else "No matches",
                                    message = if (state.query.isBlank()) "Add your first category using the button below." else "Try a different keyword.",
                                    emoji = if (state.query.isBlank()) "📁" else "🔍"
                                )
                            }
                        } else {
                            items(items, key = { it.id }) { category ->
                                CategoryRow(
                                    category = category,
                                    onClick = { viewModel.openEditCategory(category) },
                                    onLongPress = { pendingDelete = category.id }
                                )
                            }
                        }
                    }
                }
            }

            if (state.isDeleting) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(CashioPadding.screen)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(CashioRadius.medium),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(CashioSpacing.mediumLarge),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Deleting…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    pendingDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { if (!state.isDeleting) pendingDelete = null },
            title = { Text("Delete category?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(id, false); pendingDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    state.editor?.let { editor ->
        CategoryEditorSheet(
            editor = editor,
            onDismiss = viewModel::dismissEditor,
            onNameChange = viewModel::updateEditorName,
            onIconChange = viewModel::updateEditorIcon,
            onColorChange = viewModel::updateEditorColor,
            onSave = viewModel::saveCategory
        )
    }
}

/**
 * Renders a single category row item.
 *
 * @param category The category data to display.
 * @param onClick Action to trigger when the card is tapped (usually edit).
 * @param onLongPress Action to trigger when the card is long-pressed (usually delete).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryRow(category: Category, onClick: () -> Unit, onLongPress: () -> Unit) {
    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = CashioShapes.card,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = onClick, onLongClick = onLongPress),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
            ) {
                Surface(
                    shape = RoundedCornerShape(CashioRadius.pill),
                    color = category.color.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = category.icon,
                        modifier = Modifier.padding(
                            horizontal = CashioSpacing.medium,
                            vertical = CashioSpacing.small
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(category.color)
                )
            }
        }
    )
}

/**
 * A Modal Bottom Sheet used for creating or editing a category.
 *
 * Provides inputs for Name, Icon, and Color selection.
 *
 * @param editor The current state of the editor logic.
 * @param onDismiss Callback when the sheet should be closed.
 * @param onNameChange Callback when text input changes.
 * @param onIconChange Callback when icon/emoji input changes.
 * @param onColorChange Callback when the color toggle button is clicked.
 * @param onSave Callback when the save/add button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditorSheet(
    editor: CategoryEditorState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (Color) -> Unit,
    onSave: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = CashioRadius.large, topEnd = CashioRadius.large)
    ) {
        Column(
            modifier = Modifier
                .padding(CashioPadding.screen)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
        ) {
            Text(
                text = if (editor.mode == EditorMode.ADD) "New Category" else "Edit Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = editor.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                isError = editor.fieldError != null,
                supportingText = { editor.fieldError?.let { Text(it) } },
                shape = RoundedCornerShape(CashioRadius.small)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)) {
                OutlinedTextField(
                    value = editor.icon,
                    onValueChange = onIconChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Icon") },
                    shape = RoundedCornerShape(CashioRadius.small)
                )

                OutlinedButton(
                    onClick = {
                        val palette = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF2196F3),
                            Color(0xFFFF9800),
                            Color(0xFFE91E63),
                            Color(0xFF9C27B0)
                        )
                        val next = palette[(palette.indexOf(editor.color).takeIf { it >= 0 }
                            ?: 0 + 1) % palette.size]
                        onColorChange(next)
                    },
                    shape = RoundedCornerShape(CashioRadius.mediumSmall)
                ) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(editor.color)
                    )
                    Spacer(Modifier.width(CashioSpacing.small))
                    Text("Color")
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(CashioRadius.mediumSmall),
                enabled = !editor.isSaving
            ) {
                if (editor.isSaving) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                else Text(if (editor.mode == EditorMode.ADD) "Add Category" else "Save Changes")
            }
        }
    }
}