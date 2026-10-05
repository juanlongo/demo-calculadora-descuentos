package com.tienda.discounts

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

@Serializable
data class DiscountRequest(
    val unitPrice: String,
    val quantity: Int,
    val couponCode: String? = null,
)

@Serializable
data class DiscountResponse(
    val subtotal: String,
    val discountPercentage: Int,
    val discountAmount: String,
    val total: String,
)

@Serializable
data class ErrorResponse(val error: String)

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        post("/api/discounts/calculate") {
            val request = call.receive<DiscountRequest>()
            try {
                val unitPrice = request.unitPrice.toBigDecimal()
                val subtotal = unitPrice
                    .multiply(BigDecimal.valueOf(request.quantity.toLong()))
                    .setScale(2, RoundingMode.HALF_UP)
                val total = DiscountCalculator.calculate(unitPrice, request.quantity, request.couponCode)

                call.respond(
                    DiscountResponse(
                        subtotal = subtotal.toPlainString(),
                        discountPercentage = DiscountCalculator.discountPercentage(request.couponCode),
                        discountAmount = DiscountCalculator
                            .discountAmount(unitPrice, request.quantity, request.couponCode)
                            .toPlainString(),
                        total = total.toPlainString(),
                    ),
                )
            } catch (_: NumberFormatException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("El precio unitario debe ser un número válido."),
                )
            } catch (error: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Datos inválidos."))
            }
        }

        get("/health") {
            call.respondText("ok", ContentType.Text.Plain)
        }

        staticResources("/", "static")
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}
