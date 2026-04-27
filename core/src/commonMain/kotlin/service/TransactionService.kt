package service

import factory.TransactionFactory
import model.params.TransactionCreationParams
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

        // calculate exact amounts to withdraw and deposit based on account currencies
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

        // withdraw from source
        val withdrawn = accountService.withdraw(params.common.accountId, amountToWithdraw)
        if (!withdrawn) {
            return false
        }

        // deposit to target
        val deposited = accountService.deposit(params.targetAccountId, amountToDeposit)
        if (!deposited) {
            // rollback withdrawal if deposit fails
            accountService.deposit(params.common.accountId, amountToWithdraw)
            return false
        }

        // create the history record
        val transferRecord = transactionFactory.create(params)

        transactionRepository.add(transferRecord)
        return true
    }

    suspend fun income(params: TransactionCreationParams.Income): Boolean {
        if (params.common.amount <= 0) return false

        // get account currency
        val accountCurrency = accountService.getAccountCurrency(params.common.accountId) ?: return false

        // convert currency if the transaction currency differs from the account currency
        val amountToDeposit = currencyExchange.convert(
            amount = params.common.amount,
            from = params.common.currency,
            to = accountCurrency
        )

        // deposit
        val deposited = accountService.deposit(params.common.accountId, amountToDeposit)
        if (!deposited) {
            return false
        }

        // create the history record
        val incomeRecord = transactionFactory.create(params)

        transactionRepository.add(incomeRecord)
        return true
    }

    suspend fun expense(params: TransactionCreationParams.Expense): Boolean {
        if (params.common.amount <= 0) return false

        // get account currency
        val accountCurrency = accountService.getAccountCurrency(params.common.accountId) ?: return false

        // convert currency if the transaction currency differs from the account currency
        val amountToWithdraw = currencyExchange.convert(
            amount = params.common.amount,
            from = params.common.currency,
            to = accountCurrency
        )

        // withdraw
        val withdrawn = accountService.withdraw(params.common.accountId, amountToWithdraw)
        if (!withdrawn) {
            return false
        }

        // create the history record
        val expenseRecord = transactionFactory.create(params)

        transactionRepository.add(expenseRecord)
        return true
    }
}