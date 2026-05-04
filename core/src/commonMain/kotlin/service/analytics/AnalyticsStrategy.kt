package service.analytics

import model.enum.CurrencyType
import model.transaction.Transaction
import service.CurrencyExchangeService

interface AnalyticsStrategy<T> {
    fun execute(
        transactions: List<Transaction>,
        baseCurrency: CurrencyType,
        exchangeService: CurrencyExchangeService
    ): T
}