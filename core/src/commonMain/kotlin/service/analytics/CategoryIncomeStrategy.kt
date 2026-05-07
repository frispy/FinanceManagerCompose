package service.analytics

import model.analytics.CategoryAnalyticItem
import model.enum.CurrencyType
import model.transaction.Transaction
import model.transaction.TransactionCategory
import service.CurrencyExchangeService

class CategoryIncomeStrategy(
    private val categories: List<TransactionCategory>
) : AnalyticsStrategy<List<CategoryAnalyticItem>> {

    override fun execute(
        transactions: List<Transaction>,
        baseCurrency: CurrencyType,
        exchangeService: CurrencyExchangeService
    ): List<CategoryAnalyticItem> {
        // take income only
        val incomes = transactions.filterIsInstance<Transaction.Income>()

        if (incomes.isEmpty()) return emptyList()

        // convert all to base currency
        val convertedIncomes = incomes.map { tx ->
            val convertedAmount = exchangeService.convert(tx.amount, tx.currency, baseCurrency)
            tx to convertedAmount
        }

        val totalIncomes = convertedIncomes.sumOf { it.second }.coerceAtLeast(1L) 

        // group by category
        val grouped = convertedIncomes.groupBy { it.first.base.categoryId }

        return grouped.map { (categoryId, txList) ->
            val category = categories.find { it.id == categoryId }
            val sum = txList.sumOf { it.second }
            val percentage = sum.toFloat() / totalIncomes.toFloat()

            CategoryAnalyticItem(
                categoryId = categoryId,
                categoryName = category?.name ?: "Uncategorized",
                iconName = category?.iconName ?: "Category",
                totalAmount = sum,
                percentage = percentage
            )
        }.sortedByDescending { it.totalAmount }  // from bigger to smallest
    }
}