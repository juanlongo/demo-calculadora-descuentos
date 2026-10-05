package com.tienda.discounts

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DiscountCalculatorTest {
    @Test
    fun `calculates subtotal without coupon`() {
        assertEquals(BigDecimal("59.97"), DiscountCalculator.calculate(BigDecimal("19.99"), 3))
    }

    @Test
    fun `applies welcome coupon`() {
        assertEquals(BigDecimal("90.00"), DiscountCalculator.calculate(BigDecimal("100"), 1, "WELCOME10"))
    }

    @Test
    fun `applies bulk coupon for five or more items`() {
        assertEquals(BigDecimal("400.00"), DiscountCalculator.calculate(BigDecimal("100"), 5, "BULK20"))
    }

    @Test
    fun `rejects bulk coupon below five items`() {
        val error = assertFailsWith<IllegalArgumentException> {
            DiscountCalculator.calculate(BigDecimal("100"), 4, "BULK20")
        }
        assertTrue(error.message.orEmpty().contains("mínima de 5"))
    }

    @Test
    fun `rejects unknown coupon`() {
        val error = assertFailsWith<IllegalArgumentException> {
            DiscountCalculator.calculate(BigDecimal("100"), 1, "INVALID")
        }
        assertTrue(error.message.orEmpty().contains("no es válido"))
    }

    @Test
    fun `rejects non-positive unit price`() {
        assertFailsWith<IllegalArgumentException> {
            DiscountCalculator.calculate(BigDecimal.ZERO, 1)
        }
    }

    @Test
    fun `rejects non-positive quantity`() {
        assertFailsWith<IllegalArgumentException> {
            DiscountCalculator.calculate(BigDecimal("100"), 0)
        }
    }

    @Test
    fun `never returns a negative total`() {
        assertTrue(DiscountCalculator.calculate(BigDecimal("0.01"), Int.MAX_VALUE, "WELCOME10") >= BigDecimal.ZERO)
    }
}
