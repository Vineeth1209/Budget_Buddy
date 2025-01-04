package com.example.budgetbuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.budgetbuddy.ui.theme.BudgetBuddyTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetBuddyTheme {
                SplashScreen(onSplashComplete = {
                    navigateToConsentActivity()
                })
            }
        }
    }

    private fun navigateToConsentActivity() {
        startActivity(Intent(this, ConsentActivity::class.java))
        finish()
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var isSplashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000) // 3-second delay
        isSplashVisible = false
        onSplashComplete()
    }

    if (isSplashVisible) {
        SplashContent()
    }
}

@Composable
fun SplashContent() {
    val background: Painter = painterResource(id = R.drawable.background)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = background,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Logo image
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(350.dp)
                .padding(bottom = 15.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    BudgetBuddyTheme {
        SplashContent()
    }
}
