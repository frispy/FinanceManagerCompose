package service

import factory.GenericFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import model.account.Account
import model.account.BankAccount
import model.enum.CurrencyType
import model.params.AccountCreationParams
import repository.AccountRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountServiceTest {

    private val accountRepository = mockk<AccountRepository>()
    private val accountFactory = mockk<GenericFactory<Account, AccountCreationParams>>()

    private val accountService = AccountService(accountRepository, accountFactory)

    // BL-7 POSITIVE
    @Test
    fun `createAccount with valid params returns true and saves account`() = runTest {
        // Arrange
        val params = AccountCreationParams.Bank(
            userId = "user-1",
            name = "My Bank",
            initBalance = 1000L,
            currency = CurrencyType.USD,
            note = "Test note",
            bankName = "Monobank"
        )

        val mockAccount = mockk<BankAccount>()

        every { accountFactory.create(params) } returns mockAccount
        coEvery { accountRepository.add(mockAccount) } returns Unit

        // Act
        val result = accountService.createAccount(params)

        // Assert
        assertTrue(result, "createAccount should return true on successful creation")
        // verify that db add method has been called exactly one time
        coVerify(exactly = 1) { accountRepository.add(mockAccount) }
    }

    // BL-8 NEGATIVE
    @Test
    fun `withdraw returns false when account is not found`() = runTest {
        // Arrange
        val invalidAccountId = "wrong-id"
        val amount = 500L

        // mock return false on found acccount
        coEvery { accountRepository.getById(invalidAccountId) } returns null

        // Act
        val result = accountService.withdraw(invalidAccountId, amount)

        // Assert
        assertFalse(result, "withdraw should return false if account is missing")
        // verify that balance update haven't been withdrawn in db
        coVerify(exactly = 0) { accountRepository.update(any()) }
    }
}