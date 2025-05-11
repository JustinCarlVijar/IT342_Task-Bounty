package com.example.bountymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.bountymobile.ui.theme.BountyMobileTheme
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BountyMobileTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier
    ) {
        composable("welcome") {
            WelcomeScreen(
                onEnterClick = { navController.navigate("login") }
            )
        }
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("main") }
            )
        }
        composable("register") {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = { username, jwtToken ->
                    navController.navigate("verify?username=$username&jwtToken=$jwtToken") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable(
            "verify?username={username}&jwtToken={jwtToken}",
            arguments = listOf(
                navArgument("username") { defaultValue = "" },
                navArgument("jwtToken") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val jwtToken = backStackEntry.arguments?.getString("jwtToken") ?: ""
            Verify(navController = navController, username = username, jwtToken = jwtToken)
        }
        composable("main") {
            MainScreen(navController = navController)
        }
        composable("profile") {
            Profile(navController = navController)
        }
        composable("create") {
            Create(navController = navController)
        }
        composable("bounty") {
            BountyScreen(navController = navController)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable(
            route = "payment_success",
            deepLinks = listOf(navDeepLink {
                uriPattern = "bountymobile://payment_success"
            })
        ) {
            BountyScreen(navController = navController)
        }

        // ✅ Added Comments Screen
        composable(
            "comments/{bountyPostId}",
            arguments = listOf(navArgument("bountyPostId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bountyPostId = backStackEntry.arguments?.getString("bountyPostId") ?: ""
            CommentsScreen(navController = navController, bountyPostId = bountyPostId)
        }
    }
}

@Composable
fun WelcomeScreen(onEnterClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Welcome to Task Bounty",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black
                        )
                    }
                }
            }

            Button(
                onClick = onEnterClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF41644A))
            ) {
                Text(
                    text = "Enter",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    BountyMobileTheme {
        WelcomeScreen(onEnterClick = {})
    }
}