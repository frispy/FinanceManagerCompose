package ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import models.UserUiModel
import ui.accounts.AccountsTab
import ui.categories.CategoriesTab
import ui.dashboard.DashboardTab
import ui.login.LoginScreen
import ui.settings.SettingsTab
import ui.transactions.TransactionsTab
import ui.theme.LocalCurrentUser

class MainContainerScreen(
    private val user: UserUiModel,
    private val onThemeChange: (Boolean) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        // we capture the root navigator here before entering the tab context
        // to avoid class cast exceptions during voyager recomposition
        val rootNavigator = LocalNavigator.currentOrThrow

        // safe logout callback
        val onLogout = {
            rootNavigator.replaceAll(LoginScreen(onThemeChange))
        }

        // provide user to all tabs deeply
        CompositionLocalProvider(LocalCurrentUser provides user) {
            TabNavigator(DashboardTab) { tabNavigator ->
                // boxwithconstraints makes UI responsive
                BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    val isWide = maxWidth > 800.dp

                    if (isWide) {
                        // desktop/tablet layout: sidebar + content
                        Row(modifier = Modifier.fillMaxSize()) {
                            // pass the safe logout callback to the sidebar
                            Sidebar(tabNavigator, onLogout)
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                CurrentTab()
                            }
                        }
                    } else {
                        // mobile layout: content + bottombar
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                CurrentTab()
                            }
                            // pass the safe logout callback to the bottom bar
                            BottomNavigationBar(tabNavigator, onLogout)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Sidebar(tabNavigator: TabNavigator, onLogout: () -> Unit) {
        Column(
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colors.surface)
                .padding(vertical = 32.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "FinManager",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp, start = 16.dp)
            )

            // tab links
            SidebarItem("Dashboard", DashboardTab, tabNavigator)
            SidebarItem("Transactions", TransactionsTab, tabNavigator)
            SidebarItem("Accounts", AccountsTab, tabNavigator)
            SidebarItem("Categories", CategoriesTab, tabNavigator)

            // disabled item per instructions
            Text(
                text = "Analytics",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // settings custom tab requires theme callback and logout callback
            val settingsTab = SettingsTab(onThemeChange, onLogout)
            SidebarItem("Settings", settingsTab, tabNavigator)

            // logout functionality using the passed callback
            Text(
                text = "Logout",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            )
        }
    }

    @Composable
    private fun SidebarItem(title: String, tab: Tab, tabNavigator: TabNavigator) {
        val isSelected = tabNavigator.current == tab
        val bgColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
        val textColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .clickable { tabNavigator.current = tab }
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text(text = title, color = textColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }

    @Composable
    private fun BottomNavigationBar(tabNavigator: TabNavigator, onLogout: () -> Unit) {
        BottomNavigation(
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface
        ) {
            BottomNavigationItem(
                selected = tabNavigator.current == DashboardTab,
                onClick = { tabNavigator.current = DashboardTab },
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("Dash") }
            )
            BottomNavigationItem(
                selected = tabNavigator.current == TransactionsTab,
                onClick = { tabNavigator.current = TransactionsTab },
                icon = { Icon(Icons.Default.List, contentDescription = null) },
                label = { Text("Trans") }
            )
            BottomNavigationItem(
                selected = tabNavigator.current == AccountsTab,
                onClick = { tabNavigator.current = AccountsTab },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                label = { Text("Accounts") }
            )
            BottomNavigationItem(
                selected = tabNavigator.current == CategoriesTab,
                onClick = { tabNavigator.current = CategoriesTab },
                icon = { Icon(Icons.Default.Category, contentDescription = null) },
                label = { Text("Cat") }
            )
            BottomNavigationItem(
                selected = false,
                onClick = { onLogout() }, // using safe callback
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                label = { Text("Exit") }
            )
        }
    }
}