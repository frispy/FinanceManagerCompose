// --- ./core/src/commonMain/kotlin/service/analytics/CategoryExpenseStrategy.kt ---
package service.analytics

import model.analytics.CategoryAnalyticItem
import model.enum.CurrencyType
import model.transaction.Transaction
import model.transaction.TransactionCategory
import service.CurrencyExchangeService

class CategoryExpenseStrategy(
    private val categories: List<TransactionCategory>
) : AnalyticsStrategy<List<CategoryAnalyticItem>> {

    override fun execute(
        transactions: List<Transaction>,
        baseCurrency: CurrencyType,
        exchangeService: CurrencyExchangeService
    ): List<CategoryAnalyticItem> {
        // take only expenses
        val expenses = transactions.filterIsInstance<Transaction.Expense>()

        if (expenses.isEmpty()) return emptyList()

        // convert all expenses to base currency
        val convertedExpenses = expenses.map { tx ->
            val convertedAmount = exchangeService.convert(tx.amount, tx.currency, baseCurrency)
            tx to convertedAmount
        }

        val totalExpenses = convertedExpenses.sumOf { it.second }.coerceAtLeast(1L)

        // group by categories
        val grouped = convertedExpenses.groupBy { it.first.base.categoryId }

        return grouped.map { (categoryId, txList) ->
            val category = categories.find { it.id == categoryId }
            val sum = txList.sumOf { it.second }
            val percentage = sum.toFloat() / totalExpenses.toFloat()

            CategoryAnalyticItem(
                categoryId = categoryId,
                categoryName = category?.name ?: "Uncategorized",
                iconName = category?.iconName ?: "Category",
                totalAmount = sum,
                percentage = percentage
            )
        }.sortedByDescending { it.totalAmount } // from bigger to smallest
    }
}