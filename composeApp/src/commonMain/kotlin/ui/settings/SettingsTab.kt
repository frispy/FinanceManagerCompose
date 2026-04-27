package ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ui.theme.LocalCurrentUser
import viewmodel.SettingsScreenModel

// taking both theme and logout callbacks in constructor
// to keep navigation safe from context leaks
class SettingsTab(
    private val onThemeChange: (Boolean) -> Unit,
    private val onLogout: () -> Unit
) : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 4u, title = "Settings")

    @Composable
    override fun Content() {
        val user = LocalCurrentUser.current
        val screenModel = rememberScreenModel {
            SettingsScreenModel(
                AppDependencies.accountRepository,
                AppDependencies.transactionRepository,
                AppDependencies.categoryRepository
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("Settings", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            Text("Theme Selection", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onThemeChange(false) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)) {
                    Text("Default Gray", color = Color.White)
                }
                Button(onClick = { onThemeChange(true) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1976D2))) {
                    Text("Ocean Blue", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text("Danger Zone", style = MaterialTheme.typography.h6, color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Warning: Core DAO does not support user deletion. This will only clear your accounts and transactions data.",
                color = Color.Gray,
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    screenModel.clearUserData(user.id)
                    // after wiping data, execute safe logout callback
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Clear All My Data", color = Color.White)
            }
        }
    }
}