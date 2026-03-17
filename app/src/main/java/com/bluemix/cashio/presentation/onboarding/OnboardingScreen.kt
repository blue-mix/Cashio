//package com.bluemix.cashio.presentation.onboarding
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.spring
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.pager.HorizontalPager
//import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.launch
//import org.koin.compose.viewmodel.koinViewModel
//
///**
// * Onboarding flow — three-page pager with skip / next / get-started.
// *
// * ViewModel interaction (marking onboarding complete) happens here only.
// */
//@Composable
//fun OnboardingScreen(
//    onNavigate: () -> Unit,
//    viewModel: OnboardingViewModel = koinViewModel()
//) {
//    val pages = OnboardingPage.pages
//    val pagerState = rememberPagerState(pageCount = { pages.size })
//    val scope = rememberCoroutineScope()
//    val isLastPage = pagerState.currentPage == pages.lastIndex
//
//    val onComplete = remember(viewModel, onNavigate) {
//        {
//            viewModel.completeOnboarding()
//            onNavigate()
//        }
//    }
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .statusBarsPadding()
//                .navigationBarsPadding()
//        ) {
//            // Skip
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.End
//            ) {
//                AnimatedVisibility(
//                    visible = !isLastPage,
//                    enter = fadeIn(),
//                    exit = fadeOut()
//                ) {
//                    TextButton(onClick = onComplete) {
//                        Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
//                    }
//                }
//            }
//
//            // Pages
//            HorizontalPager(
//                state = pagerState,
//                modifier = Modifier.weight(1f)
//            ) { index ->
//                OnboardingPageContent(page = pages[index])
//            }
//
//            // Bottom controls
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(24.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Indicators
//                Row(
//                    modifier = Modifier.padding(bottom = 32.dp),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    repeat(pages.size) { iteration ->
//                        val selected = pagerState.currentPage == iteration
//                        Box(
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .clip(CircleShape)
//                                .background(
//                                    if (selected) MaterialTheme.colorScheme.primary
//                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
//                                )
//                                .height(10.dp)
//                                .width(if (selected) 32.dp else 10.dp)
//                        )
//                    }
//                }
//
//                // Action button
//                Button(
//                    onClick = {
//                        if (isLastPage) {
//                            onComplete()
//                        } else {
//                            scope.launch {
//                                pagerState.animateScrollToPage(
//                                    pagerState.currentPage + 1,
//                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
//                                )
//                            }
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    Text(
//                        text = if (isLastPage) "Get Started" else "Next",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(Modifier.width(8.dp))
//                    Icon(
//                        imageVector = if (isLastPage) Icons.Default.Check
//                        else Icons.AutoMirrored.Filled.ArrowForward,
//                        contentDescription = null
//                    )
//                }
//            }
//        }
//    }
//}

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
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
    val isLastPage = pagerState.currentPage == pages.lastIndex

    val onComplete = remember(viewModel, onNavigate) {
        { viewModel.completeOnboarding(); onNavigate() }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(CashioSpacing.md), horizontalArrangement = Arrangement.End) {
                AnimatedVisibility(!isLastPage, enter = fadeIn(), exit = fadeOut()) {
                    TextButton(onClick = onComplete) {
                        Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                OnboardingPageContent(page = pages[index])
            }

            Column(
                Modifier.fillMaxWidth().padding(CashioSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.padding(bottom = CashioSpacing.xl), horizontalArrangement = Arrangement.Center) {
                    repeat(pages.size) { i ->
                        val selected = pagerState.currentPage == i
                        Box(
                            Modifier.padding(horizontal = CashioSpacing.xxs)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                )
                                .height(8.dp)
                                .width(if (selected) 28.dp else 8.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isLastPage) onComplete()
                        else scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1,
                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(CashioRadius.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                ) {
                    Text(
                        if (isLastPage) "Get Started" else "Next",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(CashioSpacing.xs))
                    Icon(
                        if (isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        null
                    )
                }
            }
        }
    }
}