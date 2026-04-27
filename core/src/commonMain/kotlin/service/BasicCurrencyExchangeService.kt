package service

import model.enum.CurrencyType
import kotlin.math.roundToLong

class BasicCurrencyExchangeService : CurrencyExchangeService {

    private val exchangeRates = mapOf(
        CurrencyType.USD to 1.0,    // basic currency
        CurrencyType.EUR to 0.92,
        CurrencyType.UAH to 43.5,
    )

    override fun convert(amount: Long, from: CurrencyType, to: CurrencyType): Long {
        // if same currency -> return same value
        if (from == to) return amount

        val rateFrom = exchangeRates[from]
            ?: throw IllegalArgumentException("Unknown Exchange rate for: $from")
        val rateTo = exchangeRates[to]
            ?: throw IllegalArgumentException("Unknown Exchange rate for: $to")

        // 1. transfer to basic currency (USD): amount / rateFrom
        // 2. transfer from basic to the one we transfer to: * rateTo
        val convertedAmount = (amount / rateFrom) * rateTo

        return convertedAmount.roundToLong()
    }
}