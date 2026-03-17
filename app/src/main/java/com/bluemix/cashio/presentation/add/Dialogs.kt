package com.bluemix.cashio.presentation.add

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import com.bluemix.cashio.ui.utils.toEpochMillis
import com.bluemix.cashio.ui.utils.toLocalDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/* -------------------------------------------------------------------------- */
/* Date picker                                                                 */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashioDatePickerDialog(
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateTime.toEpochMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        datePickerState.selectedDateMillis?.toLocalDate()
                            ?: LocalDate.now()
                    )
                }
            ) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/* -------------------------------------------------------------------------- */
/* Time picker                                                                 */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashioTimePickerDialog(
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select time") },
        text = { TimePicker(state = timeState) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime.of(timeState.hour, timeState.minute))
                }
            ) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}