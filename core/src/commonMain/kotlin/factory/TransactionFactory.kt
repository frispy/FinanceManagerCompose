package factory

import kotlin.time.Clock
import kotlin.time.Instant
import model.params.TransactionCreationParams
import model.transaction.Transaction
import model.transaction.TransactionBase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TransactionFactory(
    private val clock: Clock = Clock.System // Use the Kotlinx System clock
) {
    @OptIn(ExperimentalUuidApi::class)
    fun create(params: TransactionCreationParams): Transaction {
        // Capture the current time using Kotlinx Datetime
        val now: Instant = clock.now()

        fun createBase(c: TransactionCreationParams.Common) = TransactionBase(
            id = Uuid.random().toString(),
            userId = c.userId,
            accountId = c.accountId,
            amount = c.amount,
            currency = c.currency,
            date = c.date.ifBlank { clock.now().toString() },
            categoryId = c.categoryId,
            note = c.note
        )

        return when (params) {
            is TransactionCreationParams.Expense ->
                Transaction.Expense(createBase(params.common))

            is TransactionCreationParams.Income ->
                Transaction.Income(createBase(params.common))

            is TransactionCreationParams.Transfer ->
                Transaction.Transfer(createBase(params.common), params.targetAccountId)
        }
    }
}