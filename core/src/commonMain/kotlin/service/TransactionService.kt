package service

import factory.TransactionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.OperationResult
import model.params.TransactionCreationParams
import model.transaction.Transaction
import repository.TransactionRepository
import repository.TransactionRunner

class TransactionService(
    private val transactionRunner: TransactionRunner,
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService,
    private val transactionFactory: TransactionFactory,
    private val currencyExchange: CurrencyExchangeService
) {

    fun getUserTransactionsFlow(userId: String): Flow<List<Transaction>> {
        return transactionRepository.getAllTransactionsFlow().map { list ->
            list.filter { it.base.userId == userId }
        }
    }

    // get flow of transactions of only specific account
    fun getAccountTransactionsFlow(accountId: String): Flow<List<Transaction>> {
        return transactionRepository.getAllTransactionsFlow().map { list ->
            list.filter { it.accountId == accountId || (it is Transaction.Transfer && it.targetAccountId == accountId) }
        }
    }

    suspend fun transfer(params: TransactionCreationParams.Transfer): OperationResult<Unit> {
        if (params.common.amount <= 0) return OperationResult.Error("Amount must be greater than zero.")

        return try {
            transactionRunner.execute {
                val sourceCurrency = accountService.getAccountCurrency(params.common.accountId)
                    ?: throw IllegalArgumentException("Source account not found")
                val targetCurrency = accountService.getAccountCurrency(params.targetAccountId)
                    ?: throw IllegalArgumentException("Target account not found")

                val amountToWithdraw = currencyExchange.convert(
                    amount = params.common.amount,
                    from = params.common.currency,
                    to = sourceCurrency
                )

                val amountToDeposit = currencyExchange.convert(
                    amount = params.common.amount,
                    from = params.common.currency,
                    to = targetCurrency
                )

                if (!accountService.withdraw(params.common.accountId, amountToWithdraw)) {
                    throw IllegalStateException("Failed to withdraw from source due to insufficient funds.")
                }

                if (!accountService.deposit(params.targetAccountId, amountToDeposit)) {
                    throw IllegalStateException("Failed to deposit to target account.")
                }

                val transferRecord = transactionFactory.create(params)
                transactionRepository.add(transferRecord)
            }
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "An unexpected error occurred during transfer.")
        }
    }

    suspend fun income(params: TransactionCreationParams.Income): OperationResult<Unit> {
        if (params.common.amount <= 0) return OperationResult.Error("Amount must be greater than zero.")

        return try {
            transactionRunner.execute {
                val accountCurrency = accountService.getAccountCurrency(params.common.accountId)
                    ?: throw IllegalArgumentException("Account not found")

                val amountToDeposit = currencyExchange.convert(
                    amount = params.common.amount,
                    from = params.common.currency,
                    to = accountCurrency
                )

                // deposit money
                if (!accountService.deposit(params.common.accountId, amountToDeposit)) {
                    throw IllegalStateException("Failed to deposit")
                }

                // create and save history record
                val incomeRecord = transactionFactory.create(params)
                transactionRepository.add(incomeRecord)
            }
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "An unexpected error occurred during transfer.")
        }
    }

    suspend fun expense(params: TransactionCreationParams.Expense): OperationResult<Unit> {
        if (params.common.amount <= 0) return OperationResult.Error("Amount must be greater than zero.")

        return try {
            transactionRunner.execute {
                val accountCurrency = accountService.getAccountCurrency(params.common.accountId)
                    ?: throw IllegalArgumentException("Account not found")

                val amountToWithdraw = currencyExchange.convert(
                    amount = params.common.amount,
                    from = params.common.currency,
                    to = accountCurrency
                )

                // withdraw money
                if (!accountService.withdraw(params.common.accountId, amountToWithdraw)) {
                    throw IllegalStateException("Failed to withdraw")
                }
                // create and save history record
                val expenseRecord = transactionFactory.create(params)
                transactionRepository.add(expenseRecord)
            }
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "An unexpected error occurred during transfer.")
        }
    }

    suspend fun deleteTransactionRecord(transactionId: String): OperationResult<Unit> {
        return try {
            transactionRunner.execute {
                val transaction = transactionRepository.getById(transactionId)
                    ?: throw IllegalArgumentException("Transaction not found")

                // revert the financial impact based on type
                when (transaction) {
                    is Transaction.Income -> revertIncome(transaction)
                    is Transaction.Expense -> revertExpense(transaction)
                    is Transaction.Transfer -> revertTransfer(transaction)
                }

                // remove the record
                transactionRepository.delete(transactionId)
            }
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e.message ?: "An unexpected error occurred during transfer.")
        }
    }

    private suspend fun revertIncome(transaction: Transaction.Income) {
        val accountCurrency = accountService.getAccountCurrency(transaction.accountId)
            ?: throw IllegalArgumentException("Account missing")

        val amountToWithdraw = currencyExchange.convert(
            transaction.amount,
            transaction.currency,
            accountCurrency
        )

        if (!accountService.withdraw(transaction.accountId, amountToWithdraw)) {
            throw IllegalStateException("Failed to revert income: withdraw failed")
        }
    }

    private suspend fun revertExpense(transaction: Transaction.Expense) {
        val accountCurrency = accountService.getAccountCurrency(transaction.accountId)
            ?: throw IllegalArgumentException("Account missing")

        val amountToDeposit = currencyExchange.convert(
            transaction.amount,
            transaction.currency,
            accountCurrency
        )

        if (!accountService.deposit(transaction.accountId, amountToDeposit)) {
            throw IllegalStateException("Failed to revert expense: deposit failed")
        }
    }

    private suspend fun revertTransfer(transaction: Transaction.Transfer) {
        val sourceCurrency = accountService.getAccountCurrency(transaction.accountId)
            ?: throw IllegalArgumentException("Source missing")
        val targetCurrency = accountService.getAccountCurrency(transaction.targetAccountId)
            ?: throw IllegalArgumentException("Target missing")

        val amountToRefundSource = currencyExchange.convert(
            transaction.amount,
            transaction.currency,
            sourceCurrency
        )
        val amountToWithdrawTarget = currencyExchange.convert(
            transaction.amount,
            transaction.currency,
            targetCurrency
        )

        // withdraw from the destination first
        if (!accountService.withdraw(transaction.targetAccountId, amountToWithdrawTarget)) {
            throw IllegalStateException("Failed to revert transfer: withdraw from target failed")
        }

        // refund the source
        if (!accountService.deposit(transaction.accountId, amountToRefundSource)) {
            throw IllegalStateException("Failed to revert transfer: deposit to source failed")
        }
    }
}
