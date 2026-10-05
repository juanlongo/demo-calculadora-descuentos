# Calculadora de descuentos

Aplicación Kotlin/JVM con una página web para calcular descuentos de una tienda y copiar el total final calculado.

## Ejecutar

```bash
./gradlew run
```

Abrí `http://localhost:8080` en el navegador.

## Verificar

```bash
./gradlew test
```

## Reglas

- El precio unitario debe ser mayor que cero.
- La cantidad debe ser un entero positivo.
- `WELCOME10` descuenta 10%.
- `BULK20` descuenta 20% desde 5 unidades.
- Un cupón inválido produce un error claro.
