# Plan de pruebas — Backend AuraArriendos

## Objetivo
Verificar que la lógica de negocio, la API REST y la conexión con la base de datos funcionan correctamente
antes de integrar con el frontend.

## Herramientas
- **JUnit 5**: estructura de las pruebas, con `@DisplayName` en español.
- **Mockito**: reemplaza los repositorios por objetos simulados en las pruebas unitarias.
- **MockMvc**: llama a los endpoints sin levantar un servidor real.
- **H2**: base de datos en memoria (modo MySQL) para las pruebas de integración.

Todas las pruebas siguen el formato **Given – When – Then** (Dado – Cuando – Entonces).

## Tipos de prueba

| Tipo | Clase | Qué verifica |
|---|---|---|
| Unitaria | `ValidacionesTest` | RUN módulo 11, dominios de correo, Luhn, vencimiento, ids legibles |
| Unitaria | `PagoServiceTest` | Pasarela simulada: aprobada, rechazada, monto inválido |
| Unitaria | `ProductoServiceTest` | Id correlativo, oferta < precio, publicación pendiente, rechazo con motivo, no eliminar con boletas |
| Unitaria | `CategoriaServiceTest` | Id sin tildes, nombre duplicado (409), no eliminar con propiedades |
| Unitaria | `UsuarioServiceTest` | BCrypt, RUN y correo válidos, duplicados, clave en blanco se conserva, no eliminar con boletas |
| Unitaria | `BoletaServiceTest` | Pago aprobado (oferta y stock), rechazado (stock igual), sin stock, fecha pasada, tarjeta inválida, duplicados |
| Controlador | `ProductoControllerTest` | Códigos HTTP 200, 201 + Location, 400 con errores por campo, 404 |
| Integración | `FlujoCompraIntegrationTest` | Compra completa API → servicio → H2, 409 por API, enlaces HATEOAS, Swagger |
| Integración | `DataLoaderIntegrationTest` | Carga inicial, roles, BCrypt y datos DataFaker válidos |

## Cómo ejecutar

```bash
./mvnw test
```

Resultado esperado: **BUILD SUCCESS**, 0 fallas.

## Pruebas manuales (Swagger / Postman)

1. `GET /api/v1/productos` → 200 con el catálogo.
2. `POST /api/v1/categorias` con `{"nombre":"Oficina"}` → 201.
3. `DELETE /api/v1/categorias/casa` → 409 (tiene propiedades).
4. `POST /api/v1/boletas` con la tarjeta `4242 4242 4242 4242` → 201, `estado: pagada`.
5. Repetir con `4000 0000 0000 0002` → 201, `estado: rechazada`.
6. `GET /api/v2/productos/PR-1001` → respuesta con `_links`.
