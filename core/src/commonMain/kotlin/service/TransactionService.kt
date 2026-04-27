package service

import factory.TransactionFactory
import model.params.TransactionCreationParams
import model.transaction.Transaction
import repository.TransactionRepository

class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService,
    private val transactionFactory: TransactionFactory,
    private val currencyExchange: CurrencyExchangeService
) {
    suspend fun transfer(params: TransactionCreationParams.Transfer): Boolean {
        if (params.common.amount <= 0) return false

        // get currencies for both accounts
        val sourceCurrency = accountService.getAccountCurrency(params.common.accountId) ?: return false
        val targetCurrency = accountService.getAccountCurrency(params.targetAccountId) ?: return false

        // calculate exact amounts
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

        // 1. withdraw from source
        val withdrawn = accountService.withdraw(params.common.accountId, amountToWithdraw)
        if (!withdrawn) {
            return false
        }

        // 2. deposit to target
        val deposited = accountService.deposit(params.targetAccountId, amountToDeposit)
        if (!deposited) {
            // rollback step 1
            accountService.deposit(params.common.accountId, amountToWithdraw)
            return false
        }

        // 3. create and save history record
        return try {
            val transferRecord = transactionFactory.create(params)
            transactionRepository.add(transferRecord)
            true
        } catch (e: Exception) {
            // full rollback if db save fails
            accountService.withdraw(params.targetAccountId, amountToDeposit)
            accountService.deposit(params.common.accountId, amountToWithdraw)
            false
        }
    }

    suspend fun income(params: TransactionCreationParams.Income): Boolean {
        if (params.common.amount <= 0) return false

        val accountCurrency = accountService.getAccountCurrency(params.common.accountId) ?: return false

        val amountToDeposit = currencyExchange.convert(
            amount = params.common.amount,
            from = params.common.currency,
            to = accountCurrency
        )

        // 1. deposit
        val deposited = accountService.deposit(params.common.accountId, amountToDeposit)
        if (!deposited) {
            return false
        }

        // 2. create and save history record
        return try {
            val incomeRecord = transactionFactory.create(params)
            transactionRepository.add(incomeRecord)
            true
        } catch (e: Exception) {
            // rollback deposit if db save fails
            accountService.withdraw(params.common.accountId, amountToDeposit)
            false
        }
    }

    suspend fun expense(params: TransactionCreationParams.Expense): Boolean {
        if (params.common.amount <= 0) return false

        val accountCurrency = accountService.getAccountCurrency(params.common.accountId) ?: return false

        val amountToWithdraw = currencyExchange.convert(
            amount = params.common.amount,
            from = params.common.currency,
            to = accountCurrency
        )

        // 1. withdraw
        val withdrawn = accountService.withdraw(params.common.accountId, amountToWithdraw)
        if (!withdrawn) {
            return false
        }

        // 2. create and save history record
        return try {
            val expenseRecord = transactionFactory.create(params)
            transactionRepository.add(expenseRecord)
            true
        } catch (e: Exception) {
            // rollback withdrawal if db save fails
            accountService.deposit(params.common.accountId, amountToWithdraw)
            false
        }
    }

    // safely revert history transaction
    suspend fun deleteTransactionRecord(transactionId: String): Boolean {
        val transaction = transactionRepository.getById(transactionId) ?: return false

        return try {
            when (transaction) {
                is Transaction.Income -> {
                    val accountCurrency = accountService.getAccountCurrency(transaction.accountId) ?: return false
                    val amountToWithdraw = currencyExchange.convert(transaction.amount, transaction.currency, accountCurrency)

                    val withdrawn = accountService.withdraw(transaction.accountId, amountToWithdraw)
                    if (!withdrawn) return false

                    try {
                        transactionRepository.delete(transactionId)
                        true
                    } catch (e: Exception) {
                        // rollback withdrawal if db delete fails
                        accountService.deposit(transaction.accountId, amountToWithdraw)
                        false
                    }
                }
                is Transaction.Expense -> {
                    val accountCurrency = accountService.getAccountCurrency(transaction.accountId) ?: return false
                    val amountToDeposit = currencyExchange.convert(transaction.amount, transaction.currency, accountCurrency)

                    val deposited = accountService.deposit(transaction.accountId, amountToDeposit)
                    if (!deposited) {
                        return false
                    }

                    try {
                        transactionRepository.delete(transactionId)
                        true
                    } catch (e: Exception) {
                        // rollback deposit if db delete fails
                        accountService.withdraw(transaction.accountId, amountToDeposit)
                        false
                    }
                }
                is Transaction.Transfer -> {
                    val sourceCurrency = accountService.getAccountCurrency(transaction.accountId) ?: return false
                    val targetCurrency = accountService.getAccountCurrency(transaction.targetAccountId) ?: return false

                    val amountToRefundSource = currencyExchange.convert(transaction.amount, transaction.currency, sourceCurrency)
                    val amountToWithdrawTarget = currencyExchange.convert(transaction.amount, transaction.currency, targetCurrency)

                    val withdrawn = accountService.withdraw(transaction.targetAccountId, amountToWithdrawTarget)
                    if (!withdrawn) return false

                    val deposited = accountService.deposit(transaction.accountId, amountToRefundSource)
                    if (!deposited) {
                        // rollback withdrawal from target
                        accountService.deposit(transaction.targetAccountId, amountToWithdrawTarget)
                        return false
                    }

                    try {
                        transactionRepository.delete(transactionId)
                        true
                    } catch (e: Exception) {
                        // rollback both balances if db delete fails
                        accountService.withdraw(transaction.accountId, amountToRefundSource)
                        accountService.deposit(transaction.targetAccountId, amountToWithdrawTarget)
                        false
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}