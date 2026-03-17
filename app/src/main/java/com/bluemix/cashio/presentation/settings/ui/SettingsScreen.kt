//package com.bluemix.cashio.presentation.settings.ui
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.asPaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.navigationBars
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Icon
//import androidx.compose.material3.LargeTopAppBar
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.bluemix.cashio.R
//import com.bluemix.cashio.core.util.PermissionHelper
//import com.bluemix.cashio.core.util.getPermissionStatus
//import com.bluemix.cashio.presentation.common.UiState
//import com.bluemix.cashio.presentation.settings.vm.SettingsViewModel
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import org.koin.compose.viewmodel.koinViewModel
//
///**
// * Settings screen — ViewModel interaction is confined here.
// *
// * All section composables in `Components.kt` are stateless.
// */
//@Composable
//fun SettingsScreen(
//    onNavigateToKeywordMapping: () -> Unit,
//    onNavigateToCategories: () -> Unit = {},
//    viewModel: SettingsViewModel = koinViewModel()
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    // Refresh permission status on resume
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_RESUME) {
//                val status = PermissionHelper.getPermissionStatus(context)
//                viewModel.refreshPermissionStatus(
//                    smsGranted = status.smsGranted,
//                    notificationGranted = status.notificationGranted
//                )
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
//    }
//
//    val bottomInset = WindowInsets.navigationBars
//        .asPaddingValues()
//        .calculateBottomPadding()
//
//    val hasMappings = remember(state.keywordMappings) {
//        (state.keywordMappings as? UiState.Success)?.data?.isNotEmpty() == true
//    }
//
//    // ── Layout ──────────────────────────────────────────────────────────
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .statusBarsPadding()
//    ) {
//        LargeTopAppBar(
//            title = {
//                Text(
//                    text = "Settings",
//                    style = MaterialTheme.typography.displaySmall,
//                    color = MaterialTheme.colorScheme.onSurface,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = MaterialTheme.colorScheme.background
//            ),
//            modifier = Modifier.padding(horizontal = CashioPadding.screen)
//        )
//
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(CashioSpacing.default),
//            contentPadding = PaddingValues(
//                start = CashioPadding.screen,
//                end = CashioPadding.screen,
//                top = CashioSpacing.small,
//                bottom = bottomInset + CashioSpacing.massive
//            )
//        ) {
//            item { AppearanceSection(isDarkMode = state.darkModeEnabled, onDarkModeToggle = viewModel::setDarkMode) }
//            item { KeywordMappingEntryCard(hasMappings = hasMappings, onClick = onNavigateToKeywordMapping) }
//            item { CategoriesEntryCard(hasCategories = true, onClick = onNavigateToCategories) }
//            item {
//                PermissionsSection(
//                    smsGranted = state.smsPermissionGranted,
//                    notificationGranted = state.notificationAccessGranted,
//                    onSmsClick = { PermissionHelper.openAppSettings(context) },
//                    onNotificationClick = { PermissionHelper.openNotificationAccessSettings(context) }
//                )
//            }
//            item { DangerZoneSection(onClearDataClick = viewModel::showClearDataDialog) }
//            item {
//                AboutSection(
//                    onFaqClick = { /* TODO */ },
//                    onSupportClick = { /* TODO */ },
//                    onRateUsClick = { /* TODO */ }
//                )
//            }
//            state.message?.let { msg ->
//                item { SettingsMessageBanner(message = msg, onDismiss = viewModel::dismissMessage) }
//            }
//        }
//    }
//
//    // Clear data confirmation
//    if (state.showClearDataConfirmation) {
//        ClearDataConfirmationDialog(
//            isClearingData = state.isClearingData,
//            onConfirm = viewModel::confirmClearData,
//            onDismiss = viewModel::dismissClearDataDialog
//        )
//    }
//}
//
//@Composable
//private fun ClearDataConfirmationDialog(
//    isClearingData: Boolean,
//    onConfirm: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        icon = {
//            Icon(
//                painter = painterResource(R.drawable.delete),
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.error,
//                modifier = Modifier.size(32.dp)
//            )
//        },
//        title = { Text("Clear All Data?", color = MaterialTheme.colorScheme.error) },
//        text = {
//            Text(
//                text = "This will permanently delete:\n\n" +
//                        "• All transactions\n" +
//                        "• All keyword mappings\n" +
//                        "• All custom categories\n\n" +
//                        "This action cannot be undone.",
//                style = MaterialTheme.typography.bodyMedium
//            )
//        },
//        confirmButton = {
//            TextButton(onClick = onConfirm, enabled = !isClearingData) {
//                Text(
//                    text = if (isClearingData) "Clearing..." else "Clear Data",
//                    color = MaterialTheme.colorScheme.error,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss, enabled = !isClearingData) { Text("Cancel") }
//        }
//    )
//}
package com.bluemix.cashio.presentation.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.R
import com.bluemix.cashio.core.util.PermissionHelper
import com.bluemix.cashio.core.util.getPermissionStatus
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.settings.vm.SettingsViewModel
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings screen — ViewModel interaction is confined here.
 *
 * All section composables in `Components.kt` are stateless.
 */
@Composable
fun SettingsScreen(
    onNavigateToKeywordMapping: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh permission status on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val status = PermissionHelper.getPermissionStatus(context)
                viewModel.refreshPermissionStatus(
                    smsGranted = status.smsGranted,
                    notificationGranted = status.notificationGranted
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val bottomInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    val hasMappings = remember(state.keywordMappings) {
        (state.keywordMappings as? UiState.Success)?.data?.isNotEmpty() == true
    }

    // ── Layout ──────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        LargeTopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.padding(horizontal = CashioPadding.screen)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.md),
            contentPadding = PaddingValues(
                start = CashioPadding.screen,
                end = CashioPadding.screen,
                top = CashioSpacing.xs,
                bottom = bottomInset + CashioSpacing.xl
            )
        ) {
            item { AppearanceSection(isDarkMode = state.darkModeEnabled, onDarkModeToggle = viewModel::setDarkMode) }
            item { KeywordMappingEntryCard(hasMappings = hasMappings, onClick = onNavigateToKeywordMapping) }
            item { CategoriesEntryCard(hasCategories = true, onClick = onNavigateToCategories) }
            item {
                PermissionsSection(
                    smsGranted = state.smsPermissionGranted,
                    notificationGranted = state.notificationAccessGranted,
                    onSmsClick = { PermissionHelper.openAppSettings(context) },
                    onNotificationClick = { PermissionHelper.openNotificationAccessSettings(context) }
                )
            }
            item { DangerZoneSection(onClearDataClick = viewModel::showClearDataDialog) }
            item {
                AboutSection(
                    onFaqClick = { /* TODO */ },
                    onSupportClick = { /* TODO */ },
                    onRateUsClick = { /* TODO */ }
                )
            }
            state.message?.let { msg ->
                item { SettingsMessageBanner(message = msg, onDismiss = viewModel::dismissMessage) }
            }
        }
    }

    // Clear data confirmation
    if (state.showClearDataConfirmation) {
        ClearDataConfirmationDialog(
            isClearingData = state.isClearingData,
            onConfirm = viewModel::confirmClearData,
            onDismiss = viewModel::dismissClearDataDialog
        )
    }
}

@Composable
private fun ClearDataConfirmationDialog(
    isClearingData: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.delete),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Clear All Data?", color = MaterialTheme.colorScheme.error) },
        text = {
            Text(
                text = "This will permanently delete:\n\n" +
                        "• All transactions\n" +
                        "• All keyword mappings\n" +
                        "• All custom categories\n\n" +
                        "This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isClearingData) {
                Text(
                    text = if (isClearingData) "Clearing..." else "Clear Data",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isClearingData) { Text("Cancel") }
        }
    )
}