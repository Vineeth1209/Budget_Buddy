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
                    // Navigate to LoginActivity after the splash screen is complete
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish() // Close MainActivity so it’s removed from the back stack
                })
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var isSplashVisible by remember { mutableStateOf(true) }

    // Delay to keep splash screen visible for 3 seconds, then call onSplashComplete
    LaunchedEffect(Unit) {
        delay(9000) // 3-second delay
        isSplashVisible = false
        onSplashComplete()
    }

    if (isSplashVisible) {
        SplashContent()
    }
}

@Composable
fun SplashContent() {
    // Load the background image from the drawable folder
    val background: Painter = painterResource(id = R.drawable.background)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp), // Remove any padding
        contentAlignment = Alignment.Center
    ) {
        // Set the background image with content scaling and full size
        Image(
            painter = background,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Crop the image to fill the screen
        )

        // Semi-transparent overlay to decrease contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)) // 40% opacity black overlay
        )

        // Logo Image
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo's resource ID
            contentDescription = "Logo",
            modifier = Modifier
                .size(350.dp) // Set desired size for the logo
                .padding(bottom = 15.dp),
            contentScale = ContentScale.Fit // Scale the logo proportionally
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
