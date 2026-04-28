package service

import factory.TransactionFactory
import model.params.TransactionCreationParams
import model.transaction.Transaction
import repository.TransactionRepository
import repository.UnitOfWork

class TransactionService(
    private val unitOfWork: UnitOfWork,
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService,
    private val transactionFactory: TransactionFactory,
    private val currencyExchange: CurrencyExchangeService
) {
    suspend fun transfer(params: TransactionCreationParams.Transfer): Boolean {
        if (params.common.amount <= 0) return false
        return try {
            unitOfWork.execute {
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

                // if any of these fail, an exception is thrown to trigger the unit of work rollback
                if (!accountService.withdraw(params.common.accountId, amountToWithdraw)) {
                    throw IllegalStateException("Failed to withdraw from source")
                }

                if (!accountService.deposit(params.targetAccountId, amountToDeposit)) {
                    throw IllegalStateException("Failed to deposit to target")
                }

                val transferRecord = transactionFactory.create(params)
                transactionRepository.add(transferRecord)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun income(params: TransactionCreationParams.Income): Boolean {
        if (params.common.amount <= 0) return false

        return try {
            unitOfWork.execute {
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
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun expense(params: TransactionCreationParams.Expense): Boolean {
        if (params.common.amount <= 0) return false

        return try {
            unitOfWork.execute {
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
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTransactionRecord(transactionId: String): Boolean {
        return try {
            unitOfWork.execute {
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
            true
        } catch (e: Exception) {
            false
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
