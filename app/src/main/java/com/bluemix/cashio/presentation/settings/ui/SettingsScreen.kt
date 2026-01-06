package com.bluemix.cashio.presentation.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.core.util.PermissionHelper
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.settings.AboutSection
import com.bluemix.cashio.presentation.settings.AppearanceSection
import com.bluemix.cashio.presentation.settings.PermissionsSection
import com.bluemix.cashio.presentation.settings.vm.SettingsViewModel
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.components.defaults.TopBarAction
import com.bluemix.cashio.ui.components.defaults.TopBarIcon
import com.bluemix.cashio.ui.theme.CashioPadding
import com.bluemix.cashio.ui.theme.CashioSpacing
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToKeywordMapping: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        CashioTopBar(
            title = CashioTopBarTitle.Text("Settings"),
            contentColor = MaterialTheme.colorScheme.onBackground,
            leadingAction = TopBarAction(
                icon = TopBarIcon.Vector(Icons.Default.ChevronLeft),
                onClick = onNavigateBack
            ),
            modifier = Modifier.padding(horizontal = CashioPadding.screen) // 16.dp -> screen gutter
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Consistent gap between different setting groups
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.default), // 16.dp -> default
            contentPadding = PaddingValues(
                start = CashioPadding.screen,
                end = CashioPadding.screen,
                top = CashioSpacing.small, // 8.dp -> small
                bottom = bottomInset + CashioSpacing.massive // Adding massive (32dp) for FAB/Safe room
            )
        ) {
            item {
                AppearanceSection(
                    isDarkMode = state.darkModeEnabled,
                    onDarkModeToggle = viewModel::setDarkMode
                )
            }

            item {
                KeywordMappingEntryCard(
                    hasMappings = (state.keywordMappings as? UiState.Success)?.data?.isNotEmpty() == true,
                    onClick = onNavigateToKeywordMapping
                )
            }

            item {
                CategoriesEntryCard(
                    hasCategories = true,
                    onClick = onNavigateToCategories
                )
            }

            item {
                PermissionsSection(
                    smsGranted = state.smsPermissionGranted,
                    notificationGranted = state.notificationAccessGranted,
                    onSmsClick = { PermissionHelper.openAppSettings(context) },
                    onNotificationClick = { PermissionHelper.openNotificationAccessSettings(context) }
                )
            }

            item {
                AboutSection(
                    onFaqClick = { /* TODO */ },
                    onSupportClick = { /* TODO */ },
                    onRateUsClick = { /* TODO */ }
                )
            }

            item {
                state.message?.let { msg ->
                    SettingsMessageBanner(
                        message = msg,
                        onDismiss = viewModel::dismissMessage
                    )
                }
            }
        }
    }
}