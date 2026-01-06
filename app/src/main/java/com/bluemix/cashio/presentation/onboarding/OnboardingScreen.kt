package com.bluemix.cashio.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun OnboardingScreen(
    onNavigate: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val pages = OnboardingPage.pages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // --- Skip Button (Top End) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Hide Skip on last page
                AnimatedVisibility(
                    visible = pagerState.currentPage < pages.lastIndex,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding()
                            onNavigate()
                        }
                    ) {
                        Text(
                            text = "Skip",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Pager Content ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { index ->
                OnboardingPageContent(page = pages[index])
            }

            // --- Bottom Controls (Indicators + Buttons) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicators
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

                        // Animated width for the active dot
                        val width = if (isSelected) 32.dp else 10.dp

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .height(10.dp)
                                .width(width)
                        )
                    }
                }

                // Primary Button
                val isLastPage = pagerState.currentPage == pages.lastIndex

                Button(
                    onClick = {
                        if (isLastPage) {
                            viewModel.completeOnboarding()
                            onNavigate()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Get Started" else "Next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon Container with soft background
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// --- Data Model for Pages ---

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    companion object {
        val pages = listOf(
            OnboardingPage(
                title = "Track Your Cash",
                description = "Easily log your income and expenses manually or automatically from SMS.",
                icon = Icons.Default.Wallet
            ),
            OnboardingPage(
                title = "Smart Analytics",
                description = "Visualize your spending habits with beautiful charts and categories.",
                icon = Icons.Default.Analytics
            ),
            OnboardingPage(
                title = "Secure & Private",
                description = "Your data stays on your device. We respect your privacy with local-first storage.",
                icon = Icons.Default.Lock
            )
        )
    }
}