//package com.bluemix.cashio.presentation.onboarding
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Analytics
//import androidx.compose.material.icons.filled.Lock
//import androidx.compose.material.icons.filled.Wallet
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
///* -------------------------------------------------------------------------- */
///* Data model                                                                  */
///* -------------------------------------------------------------------------- */
//
//data class OnboardingPage(
//    val title: String,
//    val description: String,
//    val icon: ImageVector
//) {
//    companion object {
//        val pages = listOf(
//            OnboardingPage(
//                title = "Track Your Cash",
//                description = "Easily log your income and expenses manually or automatically from SMS.",
//                icon = Icons.Default.Wallet
//            ),
//            OnboardingPage(
//                title = "Smart Analytics",
//                description = "Visualize your spending habits with beautiful charts and categories.",
//                icon = Icons.Default.Analytics
//            ),
//            OnboardingPage(
//                title = "Secure & Private",
//                description = "Your data stays on your device. We respect your privacy with local-first storage.",
//                icon = Icons.Default.Lock
//            )
//        )
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Page content (stateless)                                                    */
///* -------------------------------------------------------------------------- */
//
///**
// * Single onboarding page layout: icon circle + title + description.
// *
// * Stateless — the parent pager controls which page is shown.
// */
//@Composable
//fun OnboardingPageContent(
//    page: OnboardingPage,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        // Concentric circle icon
//        Box(
//            modifier = Modifier
//                .size(200.dp)
//                .clip(CircleShape)
//                .background(
//                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(140.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.primaryContainer),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = page.icon,
//                    contentDescription = null,
//                    modifier = Modifier.size(64.dp),
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//
//        Spacer(Modifier.height(48.dp))
//
//        Text(
//            text = page.title,
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.onBackground,
//            textAlign = TextAlign.Center
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        Text(
//            text = page.description,
//            style = MaterialTheme.typography.bodyLarge,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            textAlign = TextAlign.Center,
//            lineHeight = 24.sp
//        )
//    }
//}
package com.bluemix.cashio.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.ui.defaults.CashioSpacing

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    companion object {
        val pages = listOf(
            OnboardingPage("Track Your Cash", "Easily log your income and expenses manually or automatically from SMS.", Icons.Default.Wallet),
            OnboardingPage("Smart Analytics", "Visualize your spending habits with beautiful charts and categories.", Icons.Default.Analytics),
            OnboardingPage("Secure & Private", "Your data stays on your device. We respect your privacy with local-first storage.", Icons.Default.Lock)
        )
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = CashioSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(180.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(page.icon, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(CashioSpacing.xxl))
        Text(
            page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(CashioSpacing.md))
        Text(
            page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}