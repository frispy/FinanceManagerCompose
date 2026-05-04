package service.analytics

import model.analytics.TimeAnalyticItem
import model.enum.CurrencyType
import model.transaction.Transaction
import service.CurrencyExchangeService

class TimeLineStrategy : AnalyticsStrategy<List<TimeAnalyticItem>> {

    override fun execute(
        transactions: List<Transaction>,
        baseCurrency: CurrencyType,
        exchangeService: CurrencyExchangeService
    ): List<TimeAnalyticItem> {
        // ignore transfers
        val validTransactions = transactions.filter { it !is Transaction.Transfer }

        // group by months
        // if days needed use tx.base.date.take(10)
        val groupedByPeriod = validTransactions.groupBy { tx ->
            tx.base.date.take(7)
        }

        return groupedByPeriod.map { (period, txList) ->
            var income = 0L
            var expense = 0L

            txList.forEach { tx ->
                val converted = exchangeService.convert(tx.amount, tx.currency, baseCurrency)
                when (tx) {
                    is Transaction.Income -> income += converted
                    is Transaction.Expense -> expense += converted
                    else -> {}
                }
            }

            TimeAnalyticItem(
                periodLabel = period,
                totalIncome = income,
                totalExpense = expense
            )
        }.sortedBy { it.periodLabel }
    }
}