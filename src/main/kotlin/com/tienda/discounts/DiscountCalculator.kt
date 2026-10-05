package com.tienda.discounts

import java.math.BigDecimal
import java.math.RoundingMode

object DiscountCalculator {
    private val welcomeDiscount = BigDecimal("0.10")
    private val bulkDiscount = BigDecimal("0.20")

    fun calculate(unitPrice: BigDecimal, quantity: Int, couponCode: String? = null): BigDecimal {
        require(unitPrice > BigDecimal.ZERO) { "El precio unitario debe ser mayor que cero." }
        require(quantity > 0) { "La cantidad debe ser un entero positivo." }

        val discount = when (couponCode.normalized()) {
            "" -> BigDecimal.ZERO
            "WELCOME10" -> welcomeDiscount
            "BULK20" -> {
                require(quantity >= 5) {
                    "El cupón BULK20 requiere una cantidad mínima de 5 unidades."
                }
                bulkDiscount
            }
            else -> throw IllegalArgumentException("El cupón '$couponCode' no es válido.")
        }

        return unitPrice
            .multiply(BigDecimal.valueOf(quantity.toLong()))
            .multiply(BigDecimal.ONE.subtract(discount))
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP)
    }

    fun discountPercentage(couponCode: String?): Int = when (couponCode.normalized()) {
        "" -> 0
        "WELCOME10" -> 10
        "BULK20" -> 20
        else -> 0
    }

    fun discountAmount(unitPrice: BigDecimal, quantity: Int, couponCode: String?): BigDecimal {
        val subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
        return subtotal.subtract(calculate(unitPrice, quantity, couponCode)).setScale(2, RoundingMode.HALF_UP)
    }

    private fun String?.normalized(): String = this?.trim()?.uppercase().orEmpty()
}
