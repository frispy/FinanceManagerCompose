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

        // verify if the test user already exists to prevent duplicate seeding on every run
        if (userService.login("tester", "tester") != null) {
            return
        }

        // 1. generate the test user
        userService.register(UserCreationParams(login = "tester", pass = "tester"))
        val user = userService.login("tester", "tester") ?: return

        // 2. dynamically create 15 essential categories
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

        // fetch created categories to map their auto-generated UUIDs
        val allCats = categoryRepository.getAllCategoriesFlow().first()
        fun catId(name: String) = allCats.firstOrNull { it.name == name }?.id

        // 3. create 5 distinct accounts
        accountService.createAccount(AccountCreationParams.Bank(user.id, "Main Bank", 5000, CurrencyType.USD, "Primary checking", "Chase Bank"))
        accountService.createAccount(AccountCreationParams.Cash(user.id, "Cash Wallet", 200, CurrencyType.USD, "Pocket money", "Wallet", 50))
        accountService.createAccount(AccountCreationParams.Deposit(user.id, "Savings", 10000, CurrencyType.EUR, "Long term savings", 4.5))
        accountService.createAccount(AccountCreationParams.Bank(user.id, "Business Account", 15000, CurrencyType.UAH, "Freelance income", "Monobank"))
        accountService.createAccount(AccountCreationParams.Deposit(user.id, "Emergency Fund", 3000, CurrencyType.USD, "Rainy day fund", 2.0))

        val allAccs = accountRepository.getAllAccountsFlow().first()
        val mainBank = allAccs.firstOrNull { it.name == "Main Bank" } ?: return
        val businessAcc = allAccs.firstOrNull { it.name == "Business Account" } ?: return
        val savingsAcc = allAccs.firstOrNull { it.name == "Savings" } ?: return

        // 4. seed initial transactions to populate the dashboard history
        transactionService.income(
            TransactionCreationParams.Income(
                TransactionCreationParams.Common(user.id, mainBank.id, 3000, CurrencyType.USD, "", catId("Salary"), "Monthly Salary")
            )
        )
        transactionService.income(
            TransactionCreationParams.Income(
                TransactionCreationParams.Common(user.id, businessAcc.id, 8000, CurrencyType.UAH, "", catId("Freelance"), "Upwork Project")
            )
        )
        transactionService.expense(
            TransactionCreationParams.Expense(
                TransactionCreationParams.Common(user.id, mainBank.id, 150, CurrencyType.USD, "", catId("Groceries"), "Walmart Run")
            )
        )
        transactionService.expense(
            TransactionCreationParams.Expense(
                TransactionCreationParams.Common(user.id, mainBank.id, 1200, CurrencyType.USD, "", catId("Rent"), "Apartment Rent")
            )
        )
        transactionService.transfer(
            TransactionCreationParams.Transfer(
                common = TransactionCreationParams.Common(user.id, mainBank.id, 500, CurrencyType.USD, "", null, "To Savings"),
                targetAccountId = savingsAcc.id
            )
        )
    }
}