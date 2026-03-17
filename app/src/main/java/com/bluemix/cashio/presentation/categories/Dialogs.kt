package com.bluemix.cashio.presentation.categories

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Confirmation dialog shown when force-deleting a category that is in use. */
@Composable
fun ForceDeleteDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Category in use") },
        text = {
            Text(
                "\"$categoryName\" is used by existing transactions. " +
                        "Deleting it will reassign those transactions to the default category. Continue?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete & Reassign",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}