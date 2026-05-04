package service

import model.enum.CurrencyType
import model.transaction.Transaction
import service.analytics.AnalyticsStrategy

class AnalyticsService(
    private val currencyExchangeService: CurrencyExchangeService
) {
    fun <T> analyze(
        transactions: List<Transaction>,
        strategy: AnalyticsStrategy<T>,
        baseCurrency: CurrencyType = CurrencyType.USD // default currency for report
    ): T {
        return strategy.execute(transactions, baseCurrency, currencyExchangeService)
    }
}