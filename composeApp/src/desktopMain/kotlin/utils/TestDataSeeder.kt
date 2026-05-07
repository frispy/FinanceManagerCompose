// --- ./composeApp/src/desktopMain/kotlin/utils/TestDataSeeder.kt ---
package utils

import AppDependencies
import kotlinx.coroutines.flow.first
import model.enum.CurrencyType
import model.enum.TransactionType
import model.params.AccountCreationParams
import model.params.TransactionCreationParams
import model.params.UserCreationParams

object TestDataSeeder {
    suspend fun seed() {
        val userService = AppDependencies.userService
        val categoryService = AppDependencies.categoryService
        val accountService = AppDependencies.accountService
        val transactionService = AppDependencies.transactionService
        val categoryRepository = AppDependencies.categoryRepository
        val accountRepository = AppDependencies.accountRepository

        if (userService.login("tester", "tester") != null) {
            return
        }

        userService.register(UserCreationParams(login = "tester", pass = "tester"))
        val user = userService.login("tester", "tester") ?: return

        val categoriesData = listOf(
            Triple("Groceries", TransactionType.EXPENSE, "ShoppingCart"),
            Triple("Salary", TransactionType.INCOME, "AccountBalanceWallet"),
            Triple("Utilities", TransactionType.EXPENSE, "Build"),
            Triple("Rent", TransactionType.EXPENSE, "Home"),
            Triple("Entertainment", TransactionType.EXPENSE, "Computer"),
            Triple("Dining", TransactionType.EXPENSE, "Fastfood"),
            Triple("Transport", TransactionType.EXPENSE, "Commute"),
            Triple("Healthcare", TransactionType.EXPENSE, "LocalHospital"),
            Triple("Education", TransactionType.EXPENSE, "School"),
            Triple("Shopping", TransactionType.EXPENSE, "CardGiftcard"),
            Triple("Gifts", TransactionType.EXPENSE, "CardGiftcard"),
            Triple("Investments", TransactionType.EXPENSE, "AccountBalanceWallet"),
            Triple("Freelance", TransactionType.INCOME, "Computer"),
            Triple("Subscriptions", TransactionType.EXPENSE, "Computer"),
            Triple("Maintenance", TransactionType.EXPENSE, "Build")
        )

        for ((name, type, icon) in categoriesData) {
            categoryService.createCategory(name, type, icon)
        }

        val allCats = categoryRepository.getAllCategoriesFlow().first()
        fun catId(name: String) = allCats.firstOrNull { it.name == name }?.id

        accountService.createAccount(AccountCreationParams.Bank(user.id, "Main Bank", 5000, CurrencyType.USD, "Primary checking", "Chase Bank"))
        accountService.createAccount(AccountCreationParams.Cash(user.id, "Cash Wallet", 200, CurrencyType.USD, "Pocket money", "Wallet", 50))
        accountService.createAccount(AccountCreationParams.Deposit(user.id, "Savings", 10000, CurrencyType.EUR, "Long term savings", 4.5))
        accountService.createAccount(AccountCreationParams.Bank(user.id, "Business Account", 15000, CurrencyType.UAH, "Freelance income", "Monobank"))
        accountService.createAccount(AccountCreationParams.Deposit(user.id, "Emergency Fund", 3000, CurrencyType.USD, "Rainy day fund", 2.0))

        val allAccs = accountRepository.getAllAccountsFlow().first()
        val mainBank = allAccs.firstOrNull { it.name == "Main Bank" } ?: return
        val businessAcc = allAccs.firstOrNull { it.name == "Business Account" } ?: return
        val savingsAcc = allAccs.firstOrNull { it.name == "Savings" } ?: return

        // --- MONTH 1: JANUARY ---
        transactionService.income(TransactionCreationParams.Income(TransactionCreationParams.Common(user.id, mainBank.id, 3500, CurrencyType.USD, "2026-01-05T09:00:00", catId("Salary"), "Jan Salary")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 1200, CurrencyType.USD, "2026-01-02T12:00:00", catId("Rent"), "Jan Rent")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 300, CurrencyType.USD, "2026-01-15T18:45:00", catId("Groceries"), "Jan Groceries")))

        // --- MONTH 2: FEBRUARY ---
        transactionService.income(TransactionCreationParams.Income(TransactionCreationParams.Common(user.id, mainBank.id, 3500, CurrencyType.USD, "2026-02-05T09:00:00", catId("Salary"), "Feb Salary")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 1200, CurrencyType.USD, "2026-02-02T12:00:00", catId("Rent"), "Feb Rent")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 450, CurrencyType.USD, "2026-02-14T20:00:00", catId("Shopping"), "Valentine's Gifts")))

        // --- MONTH 3: MARCH ---
        transactionService.income(TransactionCreationParams.Income(TransactionCreationParams.Common(user.id, mainBank.id, 3500, CurrencyType.USD, "2026-03-05T09:00:00", catId("Salary"), "Mar Salary")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 1200, CurrencyType.USD, "2026-03-02T12:00:00", catId("Rent"), "Mar Rent")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 150, CurrencyType.USD, "2026-03-20T11:30:00", catId("Utilities"), "Heat Bill")))

        // --- MONTH 4: APRIL ---
        transactionService.income(TransactionCreationParams.Income(TransactionCreationParams.Common(user.id, mainBank.id, 3500, CurrencyType.USD, "2026-04-05T09:00:00", catId("Salary"), "Apr Salary")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 1200, CurrencyType.USD, "2026-04-02T12:00:00", catId("Rent"), "Apr Rent")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 500, CurrencyType.USD, "2026-04-10T15:00:00", catId("Entertainment"), "Gaming PC Parts")))

        // --- MONTH 5: MAY (CURRENT) ---
        transactionService.income(TransactionCreationParams.Income(TransactionCreationParams.Common(user.id, mainBank.id, 3500, CurrencyType.USD, "2026-05-05T09:00:00", catId("Salary"), "May Salary")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 1200, CurrencyType.USD, "2026-05-02T12:00:00", catId("Rent"), "May Rent")))
        transactionService.expense(TransactionCreationParams.Expense(TransactionCreationParams.Common(user.id, mainBank.id, 100, CurrencyType.USD, "2026-05-07T13:15:00", catId("Dining"), "Lunch Out")))
    }
}