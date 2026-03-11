package com.sh.mycash.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sh.mycash.R
import com.sh.mycash.ui.screens.accounts.AccountsScreen
import com.sh.mycash.ui.screens.categories.CategoriesScreen
import com.sh.mycash.ui.screens.dashboard.DashboardScreen
import com.sh.mycash.ui.screens.planning.PlanningScreen
import com.sh.mycash.ui.screens.reports.CategoryExpensesScreen
import com.sh.mycash.ui.screens.reports.CATEGORY_EXPENSES_ROUTE
import com.sh.mycash.ui.screens.reports.ReportsScreen
import com.sh.mycash.ui.screens.reports.categoryExpensesRoute
import com.sh.mycash.ui.screens.settings.SettingsScreen
import com.sh.mycash.ui.screens.transactions.TransactionsScreen

sealed class NavItem(
    val route: String,
    val titleResId: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Dashboard : NavItem("dashboard", R.string.tab_dashboard, Icons.Default.Home)
    data object Transactions : NavItem("transactions", R.string.tab_transactions, Icons.Default.ReceiptLong)
    data object Planning : NavItem("planning", R.string.tab_planning, Icons.Default.CalendarMonth)
    data object Reports : NavItem("reports", R.string.tab_reports, Icons.Default.Assessment)
    data object Settings : NavItem("settings", R.string.tab_settings, Icons.Default.Settings)
}

val bottomNavItems = listOf(
    NavItem.Dashboard,
    NavItem.Transactions,
    NavItem.Planning,
    NavItem.Reports,
    NavItem.Settings
)

object NavRoutes {
    const val ACCOUNTS = "accounts"
    const val CATEGORIES = "categories"
}

@Composable
fun MyCashNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.titleResId)) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavItem.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAccounts = { navController.navigate(NavRoutes.ACCOUNTS) },
                    onNavigateToCategories = { navController.navigate(NavRoutes.CATEGORIES) }
                )
            }
            composable(NavRoutes.ACCOUNTS) {
                AccountsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(NavRoutes.CATEGORIES) {
                CategoriesScreen(onBackClick = { navController.popBackStack() })
            }
            composable(
                route = CATEGORY_EXPENSES_ROUTE,
                arguments = listOf(
                    navArgument("subcategoryId") { type = NavType.LongType },
                    navArgument("startDate") { type = NavType.LongType },
                    navArgument("endDate") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val subcategoryId = backStackEntry.arguments?.getLong("subcategoryId") ?: 0L
                val startDate = backStackEntry.arguments?.getLong("startDate") ?: 0L
                val endDate = backStackEntry.arguments?.getLong("endDate") ?: 0L
                CategoryExpensesScreen(
                    subcategoryId = subcategoryId,
                    startDate = startDate,
                    endDate = endDate,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(NavItem.Transactions.route) {
                TransactionsScreen()
            }
            composable(NavItem.Planning.route) {
                PlanningScreen()
            }
            composable(NavItem.Reports.route) {
                ReportsScreen(
                    onCategoryExpenseClick = { subId, start, end ->
                        navController.navigate(categoryExpensesRoute(subId, start, end))
                    }
                )
            }
            composable(NavItem.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
