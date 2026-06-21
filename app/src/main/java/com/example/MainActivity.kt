package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.BlogDashboardScreen
import com.example.ui.BlogDetailScreen
import com.example.ui.BlogViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AutoBloggerApp()
      }
    }
  }
}

@Composable
fun AutoBloggerApp() {
    val navController = rememberNavController()
    val viewModel: BlogViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            BlogDashboardScreen(
                viewModel = viewModel,
                onBlogClick = { blog ->
                    navController.navigate("detail/${blog.id}")
                }
            )
        }
        composable("detail/{blogId}") { backStackEntry ->
            val blogId = backStackEntry.arguments?.getString("blogId")?.toIntOrNull()
            val blogs by viewModel.uiState.collectAsState()
            val selectedBlog = blogs.find { it.id == blogId }
            
            BlogDetailScreen(
                blog = selectedBlog,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
