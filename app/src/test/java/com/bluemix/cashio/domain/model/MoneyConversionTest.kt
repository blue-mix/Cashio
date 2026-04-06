package com.bluemix.cashio.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyConversionTest {

    @Test
    fun `toPaise converts double with 2 decimal places correctly`() {
        // Values that often fail with (d * 100).toLong()
        assertEquals(57L, Money.toPaise(0.57))
        assertEquals(58L, Money.toPaise(0.58))
        assertEquals(115L, Money.toPaise(1.15))
        assertEquals(2999L, Money.toPaise(29.99))
    }

    @Test
    fun `toPaise handles large values`() {
        assertEquals(123456789L, Money.toPaise(1234567.89))
    }

    @Test
    fun `toPaise rounds half up`() {
        // Even though input is filtered to 2 decimal places, the utility should be robust
        assertEquals(57L, Money.toPaise(0.574))
        assertEquals(58L, Money.toPaise(0.575))
    }
}
