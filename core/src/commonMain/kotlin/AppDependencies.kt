import data.AppDatabase
import factory.AccountFactory
import factory.TransactionFactory
import factory.UserFactory
import repository.*
import service.*

object AppDependencies {

    private lateinit var database: AppDatabase

    fun init(db: AppDatabase) {
        database = db
    }

    // --- DAO  ---
    private val userDao by lazy { database.userDao() }
    private val accountDao by lazy { database.accountDao() }
    private val categoryDao by lazy { database.categoryDao() }
    private val transactionDao by lazy { database.transactionDao() }

    // --- Repositories ---
    val userRepository: UserRepository by lazy { UserRepositoryImpl(userDao) }
    val accountRepository: AccountRepository by lazy { AccountRepositoryImpl(accountDao) }
    val categoryRepository: CategoryRepository by lazy { CategoryRepositoryImpl(categoryDao) }
    val transactionRepository: TransactionRepository by lazy { TransactionRepositoryImpl(transactionDao) }
    // --- Factories ---
    private val userFactory by lazy { UserFactory() }
    private val accountFactory by lazy { AccountFactory() }
    private val transactionFactory by lazy { TransactionFactory() }

    // --- Services ---
    val currencyExchangeService: CurrencyExchangeService by lazy { BasicCurrencyExchangeService() }

    val userService: UserService by lazy {
        UserService(
            userRepository = userRepository,
            userFactory = userFactory
        )
    }

    val accountService: AccountService by lazy {
        AccountService(
            accountRepository = accountRepository,
            accountFactory = accountFactory
        )
    }

    val categoryService: CategoryService by lazy {
        CategoryService(categoryRepository)
    }

    val transactionService: TransactionService by lazy {
        TransactionService(
            transactionRepository = transactionRepository,
            accountService = accountService,
            transactionFactory = transactionFactory,
            currencyExchange = currencyExchangeService
        )
    }
}