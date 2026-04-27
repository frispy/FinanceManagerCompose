package service

import model.enum.CurrencyType

interface CurrencyExchangeService {
    fun convert(amount: Long, from: CurrencyType, to: CurrencyType): Long
}