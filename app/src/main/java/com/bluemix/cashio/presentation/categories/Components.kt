//package com.bluemix.cashio.presentation.categories
//
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.combinedClickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.rememberModalBottomSheetState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.bluemix.cashio.domain.model.Category
//import com.bluemix.cashio.ui.defaults.CashioCard
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioShapes
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.theme.toComposeColor
//
///** ARGB color palette for the category editor. */
//internal val COLOR_PALETTE = listOf(
//    0xFF4CAF50L, 0xFF2196F3L, 0xFFFF9800L,
//    0xFFE91E63L, 0xFF9C27B0L, 0xFF009688L
//)
//
///* -------------------------------------------------------------------------- */
///* Category list item                                                          */
///* -------------------------------------------------------------------------- */
//
///**
// * Single row representing a [Category] — icon pill, name, colour dot.
// *
// * Stateless: no ViewModel reference.
// *
// * @param category    Domain model to render.
// * @param onClick     Tap action (edit).
// * @param onLongPress Long-press action (delete).
// */
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun CategoryRow(
//    category: Category,
//    onClick: () -> Unit,
//    onLongPress: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val categoryColor = remember(category.colorHex) {
//        category.colorHex.toComposeColor()
//    }
//
//    CashioCard(
//        modifier = modifier.fillMaxWidth(),
//        cornerRadius = CashioShapes.card,
//        content = {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .combinedClickable(
//                        onClick = onClick,
//                        onLongClick = onLongPress
//                    ),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//            ) {
//                Surface(
//                    shape = RoundedCornerShape(CashioRadius.pill),
//                    color = categoryColor.copy(alpha = 0.14f)
//                ) {
//                    Text(
//                        text = category.icon,
//                        modifier = Modifier.padding(
//                            horizontal = CashioSpacing.medium,
//                            vertical = CashioSpacing.small
//                        ),
//                        style = MaterialTheme.typography.titleLarge
//                    )
//                }
//
//                Text(
//                    text = category.name,
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier.weight(1f)
//                )
//
//                Box(
//                    modifier = Modifier
//                        .size(14.dp)
//                        .clip(CircleShape)
//                        .background(categoryColor)
//                )
//            }
//        }
//    )
//}
//
///* -------------------------------------------------------------------------- */
///* Editor bottom-sheet                                                         */
///* -------------------------------------------------------------------------- */
//
///**
// * Bottom sheet for creating or editing a category.
// *
// * Fully stateless — the parent screen owns [CategoryEditorState]
// * and forwards mutations through lambdas.
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CategoryEditorSheet(
//    editor: CategoryEditorState,
//    onDismiss: () -> Unit,
//    onNameChange: (String) -> Unit,
//    onIconChange: (String) -> Unit,
//    onColorChange: (Long) -> Unit,
//    onSave: () -> Unit
//) {
//    val currentColor = remember(editor.colorHex) {
//        editor.colorHex.toComposeColor()
//    }
//
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
//        shape = RoundedCornerShape(
//            topStart = CashioRadius.large,
//            topEnd = CashioRadius.large
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(CashioPadding.screen)
//                .navigationBarsPadding(),
//            verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//        ) {
//            Text(
//                text = if (editor.isEditMode) "Edit Category" else "New Category",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            OutlinedTextField(
//                value = editor.name,
//                onValueChange = onNameChange,
//                modifier = Modifier.fillMaxWidth(),
//                label = { Text("Name") },
//                shape = RoundedCornerShape(CashioRadius.small)
//            )
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//            ) {
//                OutlinedTextField(
//                    value = editor.icon,
//                    onValueChange = onIconChange,
//                    modifier = Modifier.weight(1f),
//                    label = { Text("Icon") },
//                    shape = RoundedCornerShape(CashioRadius.small)
//                )
//
//                OutlinedButton(
//                    onClick = {
//                        val idx = COLOR_PALETTE.indexOf(editor.colorHex)
//                        val next = if (idx < 0) 0 else (idx + 1) % COLOR_PALETTE.size
//                        onColorChange(COLOR_PALETTE[next])
//                    },
//                    shape = RoundedCornerShape(CashioRadius.mediumSmall)
//                ) {
//                    Box(
//                        Modifier
//                            .size(14.dp)
//                            .clip(CircleShape)
//                            .background(currentColor)
//                    )
//                    Spacer(Modifier.width(CashioSpacing.small))
//                    Text("Color")
//                }
//            }
//
//            Button(
//                onClick = onSave,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = RoundedCornerShape(CashioRadius.mediumSmall),
//                enabled = !editor.isSaving
//            ) {
//                if (editor.isSaving) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(20.dp),
//                        strokeWidth = 2.dp
//                    )
//                } else {
//                    Text(if (editor.isEditMode) "Save Changes" else "Add Category")
//                }
//            }
//        }
//    }
//}
package com.bluemix.cashio.presentation.categories

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioShapes
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.toComposeColor

internal val COLOR_PALETTE = listOf(
    0xFF4CAF50L, 0xFF2196F3L, 0xFFFF9800L,
    0xFFE91E63L, 0xFF9C27B0L, 0xFF009688L
)

/* -------------------------------------------------------------------------- */
/* Category row — flat, squircle, dense                                        */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryRow(
    category: Category,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = remember(category.colorHex) { category.colorHex.toComposeColor() }

    CashioCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = CashioShapes.card,
        padding = androidx.compose.foundation.layout.PaddingValues(CashioPadding.cardCompact),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = onClick, onLongClick = onLongPress),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
            ) {
                Surface(
                    shape = RoundedCornerShape(CashioRadius.pill),
                    color = categoryColor.copy(alpha = 0.12f),
                    tonalElevation = 0.dp, shadowElevation = 0.dp
                ) {
                    Text(
                        text = category.icon,
                        modifier = Modifier.padding(horizontal = CashioSpacing.sm, vertical = CashioSpacing.xs),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(category.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(Modifier.size(12.dp).clip(CircleShape).background(categoryColor))
            }
        }
    )
}

/* -------------------------------------------------------------------------- */
/* Editor bottom-sheet — squircle large radius (28 dp)                         */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditorSheet(
    editor: CategoryEditorState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onColorChange: (Long) -> Unit,
    onSave: () -> Unit
) {
    val currentColor = remember(editor.colorHex) { editor.colorHex.toComposeColor() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = CashioRadius.large, topEnd = CashioRadius.large)
    ) {
        Column(
            modifier = Modifier.padding(CashioPadding.screen).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
        ) {
            Text(
                if (editor.isEditMode) "Edit Category" else "New Category",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = editor.name, onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(), label = { Text("Name") },
                shape = RoundedCornerShape(CashioRadius.small)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm)) {
                OutlinedTextField(
                    value = editor.icon, onValueChange = onIconChange,
                    modifier = Modifier.weight(1f), label = { Text("Icon") },
                    shape = RoundedCornerShape(CashioRadius.small)
                )
                OutlinedButton(
                    onClick = {
                        val idx = COLOR_PALETTE.indexOf(editor.colorHex)
                        onColorChange(COLOR_PALETTE[if (idx < 0) 0 else (idx + 1) % COLOR_PALETTE.size])
                    },
                    shape = RoundedCornerShape(CashioRadius.medium)
                ) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(currentColor))
                    Spacer(Modifier.width(CashioSpacing.xs))
                    Text("Color")
                }
            }
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(CashioRadius.medium),
                enabled = !editor.isSaving,
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
            ) {
                if (editor.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text(if (editor.isEditMode) "Save Changes" else "Add Category")
            }
        }
    }
}