//package com.bluemix.cashio.presentation.add
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.expandVertically
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.shrinkVertically
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CalendarToday
//import androidx.compose.material.icons.filled.ExpandLess
//import androidx.compose.material.icons.filled.ExpandMore
//import androidx.compose.material.icons.filled.Schedule
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.bluemix.cashio.domain.model.Category
//import com.bluemix.cashio.presentation.common.SectionCard
//import com.bluemix.cashio.presentation.common.UiState
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.utils.toDateLabel
//import com.bluemix.cashio.ui.utils.toTimeLabel
//import java.time.LocalDateTime
//
///* -------------------------------------------------------------------------- */
///* Category section                                                            */
///* -------------------------------------------------------------------------- */
//
///**
// * Card wrapping category header ("Category" + "Manage" link)
// * and the [CategoryChipRow].
// *
// * Handles all three [UiState] branches internally so the
// * parent screen stays clean.
// *
// * @param header Optional slot to override the default title row.
// */
//@Composable
//fun CategorySection(
//    categoriesState: UiState<List<Category>>,
//    selectedCategoryId: String?,
//    onCategorySelected: (Category) -> Unit,
//    onManageCategories: () -> Unit,
//    modifier: Modifier = Modifier,
//    header: @Composable (() -> Unit)? = null
//) {
//    SectionCard(modifier = modifier) {
//        // Header slot — default shows "Category" + "Manage" button
//        if (header != null) {
//            header()
//        } else {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                SectionTitle("Category")
//                TextButton(onClick = onManageCategories) { Text("Manage") }
//            }
//        }
//
//        Spacer(Modifier.height(CashioSpacing.medium))
//
//        when (categoriesState) {
//            UiState.Loading -> {
//                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator()
//                }
//            }
//
//            is UiState.Success -> {
//                CategoryChipRow(
//                    categories = categoriesState.data,
//                    selectedCategoryId = selectedCategoryId,
//                    onCategorySelected = onCategorySelected
//                )
//            }
//
//            is UiState.Error -> {
//                Text(
//                    text = categoriesState.message,
//                    color = MaterialTheme.colorScheme.error
//                )
//            }
//
//            else -> Unit
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* More-details collapsible section                                            */
///* -------------------------------------------------------------------------- */
//
///**
// * Collapsible card holding date, time, and note fields.
// *
// * @param expanded Whether the inner content is visible.
// * @param onToggleExpanded Toggle callback.
// * @param dateSlot / timeSlot / noteSlot  Slot overrides for the
// *   three detail rows — sensible defaults are provided.
// */
//@Composable
//fun MoreDetailsSection(
//    expanded: Boolean,
//    onToggleExpanded: () -> Unit,
//    date: LocalDateTime,
//    note: String,
//    onDateClick: () -> Unit,
//    onTimeClick: () -> Unit,
//    onNoteChange: (String) -> Unit,
//    modifier: Modifier = Modifier,
//    dateSlot: (@Composable () -> Unit)? = null,
//    timeSlot: (@Composable () -> Unit)? = null,
//    noteSlot: (@Composable () -> Unit)? = null
//) {
//    SectionCard(modifier = modifier) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable(onClick = onToggleExpanded),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            SectionTitle("More Details")
//            Icon(
//                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
//                contentDescription = if (expanded) "Collapse" else "Expand",
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//
//        AnimatedVisibility(
//            visible = expanded,
//            enter = fadeIn() + expandVertically(),
//            exit = fadeOut() + shrinkVertically()
//        ) {
//            Column(modifier = Modifier.padding(top = CashioSpacing.medium)) {
//                dateSlot?.invoke() ?: DetailPickerRow(
//                    label = "Date",
//                    value = date.toDateLabel(),
//                    icon = Icons.Default.CalendarToday,
//                    onClick = onDateClick
//                )
//                Spacer(Modifier.height(CashioSpacing.medium))
//
//                timeSlot?.invoke() ?: DetailPickerRow(
//                    label = "Time",
//                    value = date.toTimeLabel(),
//                    icon = Icons.Default.Schedule,
//                    onClick = onTimeClick
//                )
//                Spacer(Modifier.height(CashioSpacing.medium))
//
//                noteSlot?.invoke() ?: NoteField(note = note, onNoteChange = onNoteChange)
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Shared primitives                                                           */
///* -------------------------------------------------------------------------- */
//
///** Consistent section-title text used across cards. */
//@Composable
//fun SectionTitle(text: String, modifier: Modifier = Modifier) {
//    Text(
//        text = text,
//        style = MaterialTheme.typography.titleMedium,
//        fontWeight = FontWeight.SemiBold,
//        modifier = modifier
//    )
//}
//
///**
// * Tappable row showing an icon + label on the left and a
// * value string on the right. Used for Date / Time pickers.
// */
//@Composable
//fun DetailPickerRow(
//    label: String,
//    value: String,
//    icon: ImageVector,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Surface(
//        modifier = modifier,
//        shape = RoundedCornerShape(CashioRadius.small),
//        color = MaterialTheme.colorScheme.surfaceVariant,
//        onClick = onClick
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(CashioSpacing.mediumLarge),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.compact),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = null,
//                    modifier = Modifier.size(20.dp)
//                )
//                Text(
//                    text = label,
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//            Text(
//                text = value,
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Save / submit button                                                        */
///* -------------------------------------------------------------------------- */
//
///**
// * Full-width primary action button with loading state.
// *
// * @param text Label shown when not loading.
// * @param enabled Whether the button is tappable.
// * @param isLoading If true, a spinner replaces the label.
// */
//@Composable
//fun SaveButton(
//    text: String,
//    enabled: Boolean,
//    isLoading: Boolean,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Button(
//        onClick = onClick,
//        enabled = enabled,
//        modifier = modifier
//            .fillMaxWidth()
//            .height(AddExpenseDefaults.SaveButtonHeight),
//        shape = RoundedCornerShape(CashioRadius.mediumSmall),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = MaterialTheme.colorScheme.primary
//        )
//    ) {
//        if (isLoading) {
//            CircularProgressIndicator(
//                modifier = Modifier.size(20.dp),
//                strokeWidth = 2.dp
//            )
//        } else {
//            Text(text = text, fontWeight = FontWeight.SemiBold)
//        }
//    }
//}

package com.bluemix.cashio.presentation.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.presentation.common.SectionCard
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.common.pressScale
import com.bluemix.cashio.ui.defaults.CashioBorder
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.utils.toDateLabel
import com.bluemix.cashio.ui.utils.toTimeLabel
import java.time.LocalDateTime

/* -------------------------------------------------------------------------- */
/* Category section                                                            */
/* -------------------------------------------------------------------------- */

@Composable
fun CategorySection(
    categoriesState: UiState<List<Category>>,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    onManageCategories: () -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null
) {
    SectionCard(modifier = modifier) {
        if (header != null) {
            header()
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Category")
                TextButton(onClick = onManageCategories) { Text("Manage") }
            }
        }

        Spacer(Modifier.height(CashioSpacing.sm))

        when (categoriesState) {
            UiState.Loading -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            }
            is UiState.Success -> {
                CategoryChipRow(
                    categories = categoriesState.data,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = onCategorySelected
                )
            }
            is UiState.Error -> {
                Text(categoriesState.message, color = MaterialTheme.colorScheme.error)
            }
            else -> Unit
        }
    }
}

/* -------------------------------------------------------------------------- */
/* More-details collapsible section                                            */
/* -------------------------------------------------------------------------- */

@Composable
fun MoreDetailsSection(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    date: LocalDateTime,
    note: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateSlot: (@Composable () -> Unit)? = null,
    timeSlot: (@Composable () -> Unit)? = null,
    noteSlot: (@Composable () -> Unit)? = null
) {
    SectionCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressScale(onClick = onToggleExpanded),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("More Details")
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = CashioSpacing.sm)) {
                dateSlot?.invoke() ?: DetailPickerRow(
                    label = "Date",
                    value = date.toDateLabel(),
                    icon = Icons.Default.CalendarToday,
                    onClick = onDateClick
                )
                Spacer(Modifier.height(CashioSpacing.sm))
                timeSlot?.invoke() ?: DetailPickerRow(
                    label = "Time",
                    value = date.toTimeLabel(),
                    icon = Icons.Default.Schedule,
                    onClick = onTimeClick
                )
                Spacer(Modifier.height(CashioSpacing.sm))
                noteSlot?.invoke() ?: NoteField(note = note, onNoteChange = onNoteChange)
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Shared primitives                                                           */
/* -------------------------------------------------------------------------- */

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

/**
 * Flat picker row — surfaceContainerLow fill + border stroke, no shadow.
 * Press-scale interaction instead of ripple.
 */
@Composable
fun DetailPickerRow(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.pressScale(onClick = onClick),
        shape = RoundedCornerShape(CashioRadius.small),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = CashioBorder.stroke(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CashioSpacing.md,
                    vertical = CashioSpacing.sm
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Save button — squircle, primary fill, press-scale                           */
/* -------------------------------------------------------------------------- */

@Composable
fun SaveButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(AddExpenseDefaults.SaveButtonHeight),
        shape = RoundedCornerShape(CashioRadius.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text = text, fontWeight = FontWeight.SemiBold)
        }
    }
}