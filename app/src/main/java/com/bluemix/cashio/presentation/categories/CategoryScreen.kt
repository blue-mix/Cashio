
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.components.CashioCard
import com.bluemix.cashio.components.CashioTopBar
import com.bluemix.cashio.components.CashioTopBarTitle
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.presentation.common.UiState
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 16.dp
private val GridSpacing = 12.dp
private val TileRadius = 16.dp
private const val GridColumns = 4

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit = {}, // kept for future back navigation
    viewModel: CategoriesViewModel = koinViewModel()
) {
    // Prefer lifecycle-aware collection for screens.
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar messages from VM
    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = msg.text)
        viewModel.clearMessage()
    }

    // When user long-presses a tile, we keep the category here until confirmed.
    var pendingDeleteCategory by remember { mutableStateOf<Category?>(null) }

    // Bottom insets (space for bottom bar / gesture nav).
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        topBar = {
            CashioTopBar(

                title = CashioTopBarTitle.Text("Categories"),

                contentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = ScreenPadding, vertical = ScreenPadding)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val categoriesState = state.categories) {
                is UiState.Loading, UiState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    ErrorState(
                        message = categoriesState.message,
                        onRetry = viewModel::loadCategories,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ScreenPadding)
                    )
                }

                is UiState.Success -> {
                    val categories = categoriesState.data

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GridColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = ScreenPadding,
                            end = ScreenPadding,
                            top = 8.dp,
                            // Extra space for bottom bar + FAB overlap.
                            bottom = bottomInset + 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(GridSpacing),
                        horizontalArrangement = Arrangement.spacedBy(GridSpacing)
                    ) {
                        item {
                            AddCategoryTile(onClick = viewModel::openAddCategory)
                        }

                        items(
                            items = categories,
                            key = { it.id }
                        ) { category ->
                            CategoryTile(
                                category = category,
                                onClick = { viewModel.openEditCategory(category) },
                                onLongPress = { pendingDeleteCategory = category }
                            )
                        }
                    }
                }
            }

            // Small loader while delete runs (non-blocking, but visible)
            if (state.isDeleting) {
                SmallBlockingLoader(
                    text = "Deleting...",
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    /* --------------------------- Delete Confirm Dialog --------------------------- */
    pendingDeleteCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCategory = null },
            title = { Text("Delete category?") },
            text = {
                Text(
                    "This will remove “${category.name}”. " +
                            "If expenses use it, deletion may fail unless forced."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category.id, forceDelete = false)
                        pendingDeleteCategory = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCategory = null }) { Text("Cancel") }
            }
        )
    }

    /* ------------------------------ Editor Bottom Sheet ------------------------------ */
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

/* -------------------------------- Tiles -------------------------------- */

@Composable
private fun AddCategoryTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = onClick,
        cornerRadius = TileRadius,
        showBorder = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("＋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Add",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryTile(
    category: Category,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        padding = PaddingValues(0.dp), // prevent double padding
        cornerRadius = TileRadius,
        showBorder = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(onClick = onClick, onLongClick = onLongPress)
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = category.color.copy(alpha = 0.14f)
            ) {
                Text(
                    text = category.icon,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/* -------------------------------- Sheet -------------------------------- */

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScreenPadding),
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
                        val currentIndex = palette.indexOf(editor.color).coerceAtLeast(0)
                        val nextIndex = (currentIndex + 1) % palette.size
                        onColorChange(palette[nextIndex])
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    // Show selected color (previously this box had no background)
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(editor.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Color")
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !editor.isSaving
            ) {
                if (editor.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(if (editor.mode == EditorMode.ADD) "Add Category" else "Save Changes")
            }

            Spacer(Modifier.height(8.dp))
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

/* -------------------------------- States -------------------------------- */

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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("⚠️", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            OutlinedButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun SmallBlockingLoader(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(ScreenPadding)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
