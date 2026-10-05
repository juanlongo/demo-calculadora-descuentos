package com.tienda.discounts

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `serves the calculator page`() = testApplication {
        application { module() }

        val response = client.get("/")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(body.contains("Calculadora de descuentos"))
        assertTrue(body.contains("id=\"clear-calculation\""))
        assertTrue(body.contains("Limpiar cálculo"))
        assertTrue(body.contains("id=\"copy-total\""))
        assertTrue(body.contains("id=\"copy-feedback\""))
        assertTrue(body.contains("role=\"status\""))
    }

    @Test
    fun `serves copy interaction script`() = testApplication {
        application { module() }

        val response = client.get("/app.js")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(body.contains("navigator.clipboard"))
        assertTrue(body.contains("copyTotalButton"))
        assertTrue(body.contains("form.addEventListener(\"reset\""))
        assertTrue(body.contains("resetCalculationUI()"))
        assertTrue(body.contains("unitPriceInput.focus()"))
        assertTrue(body.contains("Total copiado al portapapeles."))
    }

    @Test
    fun `returns calculated discount through API`() = testApplication {
        application { module() }

        val response = client.post("/api/discounts/calculate") {
            contentType(ContentType.Application.Json)
            setBody("""{"unitPrice":"100.00","quantity":5,"couponCode":"BULK20"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"total\":\"400.00\""))
    }

    @Test
    fun `returns clear API error for unknown coupon`() = testApplication {
        application { module() }

        val response = client.post("/api/discounts/calculate") {
            contentType(ContentType.Application.Json)
            setBody("""{"unitPrice":"100.00","quantity":1,"couponCode":"NOPE"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("no es válido"))
    }
}
