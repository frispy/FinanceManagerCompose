package ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// utility object to map string names from db to actual compose material icons
object IconMapper {
    // a small predefined set of icons for user to pick from
    val availableIcons = listOf(
        "Home", "ShoppingCart", "Fastfood", "Commute", "LocalHospital",
        "School", "FitnessCenter", "AccountBalanceWallet", "CardGiftcard",
        "Flight", "Build", "Computer"
    )

    fun getIconByName(name: String): ImageVector {
        return when (name) {
            "Home" -> Icons.Default.Home
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Fastfood" -> Icons.Default.Fastfood
            "Commute" -> Icons.Default.Commute
            "LocalHospital" -> Icons.Default.LocalHospital
            "School" -> Icons.Default.School
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "Flight" -> Icons.Default.Flight
            "Build" -> Icons.Default.Build
            "Computer" -> Icons.Default.Computer
            else -> Icons.Default.Category // default fallback
        }
    }
}