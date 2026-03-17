//package com.bluemix.cashio.presentation.common
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ColumnScope
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.bluemix.cashio.ui.defaults.CashioCard
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.defaults.CashioShapes
//
///**
// * Standard section card wrapper used across the app.
// *
// * Provides consistent padding, corner radius, and border
// * from the design-token layer.
// *
// * @param modifier External modifier for sizing / placement.
// * @param content Slot for arbitrary card content.
// */
//@Composable
//fun SectionCard(
//    modifier: Modifier = Modifier,
//    content: @Composable ColumnScope.() -> Unit
//) {
//    CashioCard(
//        modifier = modifier.fillMaxWidth(),
//        padding = PaddingValues(CashioPadding.card),
//        cornerRadius = CashioShapes.card,
//        showBorder = true,
//        content = { Column(content = content) }
//    )
//}
package com.bluemix.cashio.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioShapes

/**
 * Standard section card wrapper — Modern-Flat variant.
 *
 * - `surfaceContainerLow` fill (via [CashioCard] default)
 * - 1 dp border stroke (via [CashioCard] default)
 * - 0 dp shadow / 0 dp tonal elevation
 * - 20 dp squircle corners
 * - 20 dp inner padding
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    CashioCard(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(CashioPadding.card),
        cornerRadius = CashioShapes.card,
        showBorder = true,
        content = { Column(content = content) }
    )
}