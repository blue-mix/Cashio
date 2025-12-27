package com.bluemix.cashio.presentation.categories

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.components.defaults.TopBarAction
import com.bluemix.cashio.ui.components.defaults.TopBarIcon
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 16.dp
private val RowRadius = 16.dp

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
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = ScreenPadding)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddCategory) {
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
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    ErrorState(
                        message = ui.message,
                        onRetry = viewModel::loadCategories,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ScreenPadding)
                    )
                }

                is UiState.Success -> {
                    val query = state.query.trim().lowercase()
                    val items = if (query.isBlank()) ui.data else ui.data.filter {
                        it.name.lowercase().contains(query) || it.icon.contains(query)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            start = ScreenPadding,
                            end = ScreenPadding,
                            top = 8.dp,
                            bottom = bottomInset + 96.dp
                        )
                    ) {
                        item {
                            OutlinedTextField(
                                value = state.query,
                                onValueChange = viewModel::setQuery,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                placeholder = { Text("Search category…") }
                            )
                        }

                        if (items.isEmpty()) {
                            item {
                                EmptyCard(
                                    title = if (state.query.isBlank()) "No categories yet" else "No matches",
                                    subtitle = if (state.query.isBlank())
                                        "Add your first category using the + button."
                                    else "Try a different keyword."
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
                        .padding(ScreenPadding)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Deleting…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    // Delete confirm
    pendingDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { if (!state.isDeleting) pendingDelete = null },
            title = { Text("Delete category?") },
            text = { Text("This action can’t be undone. If expenses use it, deletion may fail unless forced.") },
            confirmButton = {
                TextButton(
                    enabled = !state.isDeleting,
                    onClick = {
                        viewModel.deleteCategory(id, forceDelete = false)
                        pendingDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(
                    enabled = !state.isDeleting,
                    onClick = { pendingDelete = null }
                ) { Text("Cancel") }
            }
        )
    }

    // Editor bottom sheet
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryRow(
    category: Category,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(14.dp),
        cornerRadius = RowRadius,
        showBorder = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongPress),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = category.color.copy(alpha = 0.14f)
            ) {
                Text(
                    text = category.icon,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Surface(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = category.color,
                content = {}
            )
        }
    }
}

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(modifier = Modifier.padding(ScreenPadding)) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editor.icon,
                        onValueChange = onIconChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Icon") },
                        singleLine = true
                    )

                    OutlinedButton(
                        onClick = {
                            val palette = sampleCategoryColors
                            val currentIndex = palette.indexOf(editor.color).takeIf { it >= 0 } ?: 0
                            val next = palette[(currentIndex + 1) % palette.size]
                            onColorChange(next)
                        }
                    ) {
                        Surface(
                            modifier = Modifier.size(14.dp),
                            shape = RoundedCornerShape(99.dp),
                            color = editor.color,
                            content = {}
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Color")
                    }
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !editor.isSaving
                ) {
                    if (editor.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.size(10.dp))
                    }
                    Text(if (editor.mode == EditorMode.ADD) "Add Category" else "Save Changes")
                }

                Spacer(Modifier.size(8.dp))
            }
        }
    }
}

private val sampleCategoryColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFF9800),
    Color(0xFFE91E63),
    Color(0xFF9C27B0)
)

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("⚠️", style = MaterialTheme.typography.headlineMedium)
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
            OutlinedButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
