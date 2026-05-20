package service

import model.enum.CurrencyType
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicCurrencyExchangeServiceTest {

    private val exchangeService = BasicCurrencyExchangeService()

    // AUX-1 POSITIVE
    @Test
    fun `convert calculates correct amount from USD to EUR`() {
        // Arrange
        val amountInUsd = 1000L // 1000 USD
        // 1000 / 1.0 * 0.92 = 920 EUR
        val expectedAmountInEur = 920L

        // Act
        val result = exchangeService.convert(amountInUsd, CurrencyType.USD, CurrencyType.EUR)

        // Assert
        assertEquals(expectedAmountInEur, result, "Conversion from USD to EUR is incorrect")
    }

    // AUX-2 NEGATIVE
    @Test
    fun `convert returns 0 when amount is 0`() {
        // Arrange
        val amount = 0L

        // Act
        val result = exchangeService.convert(amount, CurrencyType.EUR, CurrencyType.UAH)

        // Assert (Негативний/Крайовий сценарій)
        assertEquals(0L, result, "Conversion of 0 should always be 0")
    }
}