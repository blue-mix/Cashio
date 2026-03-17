//package com.bluemix.cashio.presentation.categories
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.asPaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.navigationBars
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.bluemix.cashio.ui.components.cards.StateCard
//import com.bluemix.cashio.ui.components.cards.StateCardVariant
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.defaults.CashioTopBar
//import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
//import com.bluemix.cashio.ui.defaults.TopBarAction
//import com.bluemix.cashio.ui.defaults.TopBarIcon
//import org.koin.compose.viewmodel.koinViewModel
//
//private object CategoriesDefaults {
//    val FabBottomPadding = 96.dp
//}
//
///**
// * Category management screen.
// *
// * ViewModel interaction is limited to this composable —
// * [CategoryRow], [CategoryEditorSheet], and [ForceDeleteDialog]
// * are fully stateless.
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CategoriesScreen(
//    onNavigateBack: () -> Unit,
//    viewModel: CategoriesViewModel = koinViewModel()
//) {
//    val state by viewModel.uiState.collectAsStateWithLifecycle()
//    val snackbarHostState = remember { SnackbarHostState() }
//    val bottomInset = WindowInsets.navigationBars
//        .asPaddingValues()
//        .calculateBottomPadding()
//
//    // Snackbar on messages
//    LaunchedEffect(state.message) {
//        val msg = state.message ?: return@LaunchedEffect
//        snackbarHostState.showSnackbar(msg)
//        viewModel.dismissMessage()
//    }
//
//    Scaffold(
//        topBar = {
//            CashioTopBar(
//                title = CashioTopBarTitle.Text("Categories"),
//                leadingAction = TopBarAction(
//                    icon = TopBarIcon.Vector(Icons.Default.ArrowBack),
//                    onClick = onNavigateBack
//                ),
//                modifier = Modifier.padding(horizontal = CashioPadding.screen)
//            )
//        },
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = viewModel::openAddCategory,
//                containerColor = MaterialTheme.colorScheme.primaryContainer,
//                shape = RoundedCornerShape(CashioRadius.mediumSmall)
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Add Category")
//            }
//        }
//    ) { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            when {
//                state.isLoading -> {
//                    StateCard(
//                        variant = StateCardVariant.LOADING,
//                        animated = true,
//                        modifier = Modifier.padding(CashioPadding.screen)
//                    )
//                }
//
//                state.categories.isEmpty() -> {
//                    StateCard(
//                        variant = StateCardVariant.EMPTY,
//                        title = "No categories yet",
//                        message = "Add your first category using the button below.",
//                        emoji = "📁",
//                        modifier = Modifier.padding(CashioPadding.screen)
//                    )
//                }
//
//                else -> {
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
//                        contentPadding = PaddingValues(
//                            start = CashioPadding.screen,
//                            end = CashioPadding.screen,
//                            top = CashioSpacing.small,
//                            bottom = bottomInset + CategoriesDefaults.FabBottomPadding
//                        )
//                    ) {
//                        items(state.categories, key = { it.id }) { category ->
//                            CategoryRow(
//                                category = category,
//                                onClick = { viewModel.openEditCategory(category) },
//                                onLongPress = { viewModel.deleteCategory(category) }
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    // Dialogs & sheets (overlays)
//    state.pendingForceDelete?.let { pending ->
//        ForceDeleteDialog(
//            categoryName = pending.categoryName,
//            onConfirm = viewModel::confirmForceDelete,
//            onDismiss = viewModel::dismissForceDeleteDialog
//        )
//    }
//
//    state.editor?.let { editor ->
//        CategoryEditorSheet(
//            editor = editor,
//            onDismiss = viewModel::closeEditor,
//            onNameChange = viewModel::setEditorName,
//            onIconChange = viewModel::setEditorIcon,
//            onColorChange = viewModel::setEditorColor,
//            onSave = viewModel::saveCategory
//        )
//    }
//}
package com.bluemix.cashio.presentation.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.ui.components.cards.StateCard
import com.bluemix.cashio.ui.components.cards.StateCardVariant
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.defaults.CashioTopBar
import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.defaults.TopBarAction
import com.bluemix.cashio.ui.defaults.TopBarIcon
import org.koin.compose.viewmodel.koinViewModel

private object CategoriesDefaults {
    val FabBottomPadding = 96.dp
}

/**
 * Category management screen.
 *
 * ViewModel interaction is limited to this composable —
 * [CategoryRow], [CategoryEditorSheet], and [ForceDeleteDialog]
 * are fully stateless.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    // Snackbar on messages
    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.dismissMessage()
    }

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
                shape = RoundedCornerShape(CashioRadius.medium)
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
            when {
                state.isLoading -> {
                    StateCard(
                        variant = StateCardVariant.LOADING,
                        animated = true,
                        modifier = Modifier.padding(CashioPadding.screen)
                    )
                }

                state.categories.isEmpty() -> {
                    StateCard(
                        variant = StateCardVariant.EMPTY,
                        title = "No categories yet",
                        message = "Add your first category using the button below.",
                        emoji = "📁",
                        modifier = Modifier.padding(CashioPadding.screen)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
                        contentPadding = PaddingValues(
                            start = CashioPadding.screen,
                            end = CashioPadding.screen,
                            top = CashioSpacing.xs,
                            bottom = bottomInset + CategoriesDefaults.FabBottomPadding
                        )
                    ) {
                        items(state.categories, key = { it.id }) { category ->
                            CategoryRow(
                                category = category,
                                onClick = { viewModel.openEditCategory(category) },
                                onLongPress = { viewModel.deleteCategory(category) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs & sheets (overlays)
    state.pendingForceDelete?.let { pending ->
        ForceDeleteDialog(
            categoryName = pending.categoryName,
            onConfirm = viewModel::confirmForceDelete,
            onDismiss = viewModel::dismissForceDeleteDialog
        )
    }

    state.editor?.let { editor ->
        CategoryEditorSheet(
            editor = editor,
            onDismiss = viewModel::closeEditor,
            onNameChange = viewModel::setEditorName,
            onIconChange = viewModel::setEditorIcon,
            onColorChange = viewModel::setEditorColor,
            onSave = viewModel::saveCategory
        )
    }
}