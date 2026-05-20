package service

import factory.GenericFactory
import factory.TransactionFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import model.OperationResult
import model.enum.CurrencyType
import model.params.TransactionCreationParams
import model.transaction.Transaction
import repository.TransactionRepository
import repository.TransactionRunner
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransactionServiceTest {

    private val transactionRunner = mockk<TransactionRunner>()
    private val transactionRepository = mockk<TransactionRepository>()
    private val transactionFactory = mockk<TransactionFactory>()
    private val currencyExchange = mockk<CurrencyExchangeService>()

    private val accountService = mockk<AccountService>()

    private val transactionService = TransactionService(
        transactionRunner = transactionRunner,
        transactionRepository = transactionRepository,
        accountService = accountService,
        transactionFactory = transactionFactory,
        currencyExchange = currencyExchange
    )

    // BL-5 POSITIVE
    @Test
    fun `transfer money with success`() = runTest {
        // Arrange
        val amount = 10000L
        val commonParams = TransactionCreationParams.Common(
            userId = "user-1",
            accountId = "source-123",
            amount = amount,
            currency = CurrencyType.USD,
            date = "2026-05-20",
            categoryId = null,
            note = "Transfer to friend"
        )
        val params = TransactionCreationParams.Transfer(
            common = commonParams,
            targetAccountId = "target-456"
        )

        val sourceCurrency = CurrencyType.USD
        val targetCurrency = CurrencyType.EUR
        val amountToWithdraw = 10000L
        val amountToDeposit = 9000L // less after conversion

        val fakeRecord = mockk<Transaction>()

        coEvery { transactionRunner.execute<Boolean>(any()) } coAnswers {
            val block = firstArg<suspend () -> Boolean>()
            block.invoke()
        }

        coEvery { accountService.getAccountCurrency("source-123") } returns sourceCurrency
        coEvery { accountService.getAccountCurrency("target-456") } returns targetCurrency

        every { currencyExchange.convert(amount, CurrencyType.USD, sourceCurrency) } returns amountToWithdraw
        every { currencyExchange.convert(amount, CurrencyType.USD, targetCurrency) } returns amountToDeposit

        coEvery { accountService.withdraw("source-123", amountToWithdraw) } returns true
        coEvery { accountService.deposit("target-456", amountToDeposit) } returns true

        every { transactionFactory.create(params) } returns fakeRecord
        coEvery { transactionRepository.add(any()) } returns Unit

        // Act
        val result = transactionService.transfer(params)

        // Assert
        assertTrue(result is OperationResult.Success, "Result should be an OperationResult.Success on sucessful transfer")

        coVerify(exactly = 1) { accountService.withdraw("source-123", amountToWithdraw) }
        coVerify(exactly = 1) { accountService.deposit("target-456", amountToDeposit) }
        coVerify(exactly = 1) { transactionRepository.add(any()) }
    }

    // BL-6 NEGATIVE
    @Test
    fun `transfer money fails due to insufficient funds`() = runTest {
        // Arrange
        val amount = 50000L
        val commonParams = TransactionCreationParams.Common(
            userId = "user-1",
            accountId = "source-123",
            amount = amount,
            currency = CurrencyType.USD,
            date = "2026-05-20",
            categoryId = null,
            note = "Transfer"
        )
        val params = TransactionCreationParams.Transfer(common = commonParams, targetAccountId = "target-456")

        coEvery { transactionRunner.execute<Boolean>(any()) } coAnswers {
            firstArg<suspend () -> Boolean>().invoke()
        }

        coEvery { accountService.getAccountCurrency(any()) } returns CurrencyType.USD
        every { currencyExchange.convert(any(), any(), any()) } returns amount

        // Mock that account has no money (false is returned on that condition)
        coEvery { accountService.withdraw("source-123", amount) } returns false

        // Act
        val result = transactionService.transfer(params)

        // Assert
        assertTrue(result is OperationResult.Error, "Result should be an OperationResult.Error on failed transfer due to insufficient funds")

        // Deposit and history records should not be updated/added
        coVerify(exactly = 0) { accountService.deposit(any(), any()) }
        coVerify(exactly = 0) { transactionRepository.add(any()) }
    }
}