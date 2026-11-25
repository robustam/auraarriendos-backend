# AuraArriendos — Backend (API REST)

Backend del sitio **AuraArriendos** (DSY1104 – Desarrollo Fullstack II, DUOC UC).
API REST hecha con **Spring Boot 3.3 + Java 21 + MySQL**, documentada con **Swagger** y consumida por el frontend React
([proyectoauraarriendos](https://github.com/robustam/proyectoauraarriendos)).

## Tecnologías

| Herramienta | Para qué se usa |
|---|---|
| Spring Boot 3.3.5 / Java 21 | Base del proyecto |
| Spring Web | Controladores REST (`/api/v1/...` y `/api/v2/...`) |
| Spring Data JPA + MySQL | Persistencia en base de datos |
| Bean Validation | Validación de los datos que llegan a la API |
| Spring HATEOAS | Versión v2 de la API con enlaces de navegación |
| springdoc-openapi (Swagger) | Documentación en `/doc/swagger-ui.html` |
| Spring Security + JWT (jjwt) | Login, roles y protección de rutas |
| BCrypt | Contraseñas encriptadas |
| Docker + Docker Compose | 3 contenedores: frontend, backend y base de datos |
| DataFaker | Datos de ejemplo al iniciar en perfil `dev` |
| JUnit 5 + Mockito + H2 | Pruebas unitarias y de integración |
| Lombok | Menos código repetido en las entidades |

## Modelo de datos

```
CATEGORIA 1 ──── * PRODUCTO 1 ──── * DETALLE_BOLETA * ──── 1 BOLETA * ──── 1 USUARIO
```

- **USUARIO**: run, nombre, apellidos, correo, clave (BCrypt), rol (`ADMIN`, `VENDEDOR`, `CLIENTE`), región, comuna, dirección.
- **CATEGORIA**: id legible (`casa`, `departamento`), nombre, descripción, imagen.
- **PRODUCTO** (propiedad en arriendo): precio, precio oferta, stock, alerta de stock, categoría, ubicación, estado de publicación.
- **BOLETA**: número correlativo, fecha, estado (`pagada` / `rechazada`), datos del cliente, dirección, total.
- **DETALLE_BOLETA**: cada propiedad reservada en la boleta, con el precio cobrado.

## Estructura del proyecto

```
src/main/java/cl/duoc/auraarriendos
├── model/           Entidades JPA (tablas)
├── repository/      Acceso a datos (JpaRepository)
├── service/         Lógica de negocio
├── controller/v1/   API REST JSON
├── controller/v2/   API REST con HATEOAS (HAL+JSON)
├── assemblers/      Construyen los enlaces HATEOAS
├── dto/             Lo que entra y sale de la API
├── exception/       Errores con formato JSON único
├── security/        JWT, filtro, reglas por rol y login
├── config/          Swagger, CORS y carga de datos
└── util/            Validaciones (RUN, correo, tarjeta)
```

## Reglas de negocio principales

- El **total de la boleta lo calcula el servidor** con los precios de la base de datos (incluye oferta).
- Solo se puede reservar una propiedad **publicada, no arrendada y con stock**. Una boleta **pagada descuenta stock**; una rechazada no.
- La fecha de inicio del arriendo debe ser **desde hoy**. La tarjeta se valida con **Luhn** y vencimiento `MM/AA`.
- Pago simulado: `4242 4242 4242 4242` se aprueba; `4000 0000 0000 0002` la rechaza el banco.
- El precio de oferta debe ser **menor** que el precio normal.
- No se puede eliminar: una categoría con propiedades, una propiedad que aparece en boletas, ni un usuario con boletas (**409**).
- RUN con dígito verificador (módulo 11) y correos solo `@duoc.cl`, `@profesor.duoc.cl` o `@gmail.com`.
- Las publicaciones de clientes quedan **pendientes** hasta que el administrador las aprueba o rechaza (con motivo).

## Cómo ejecutar (local)

Requisitos: **Java 21** y **MySQL** (XAMPP, Laragon o MySQL Workbench) corriendo en el puerto 3306.

```bash
# La base de datos auraarriendos_dev se crea sola. Usuario y clave por defecto: root / root
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run

# Si tu MySQL usa otra clave:
DB_USER=root DB_PASSWORD=miClave ./mvnw spring-boot:run
```

- API: http://localhost:8080/api/v1/productos
- Swagger: http://localhost:8080/doc/swagger-ui.html

Al iniciar por primera vez se cargan los datos del sitio y datos de ejemplo de DataFaker.

| Usuario | Correo | Contraseña |
|---|---|---|
| Administrador | admin@duoc.cl | admin123 |
| Vendedor | vendedor@gmail.com | vende123 |
| Cliente | cliente@gmail.com | cliente123 |

## Seguridad (JWT + roles)

1. `POST /api/v1/auth/login` con `{"correo","clave"}` devuelve:
   - `accessToken`: dura **15 minutos**. Se envía en cada petición: `Authorization: Bearer <token>`.
   - `refreshToken`: dura **7 días**. Sirve para pedir tokens nuevos en `POST /api/v1/auth/refresh`.
2. **Verificación**: el filtro `JwtAuthFilter` revisa la firma y la expiración del token en cada petición.
3. **Renovación con rotación**: cada refresh token se puede usar **una sola vez**. Si alguien reutiliza uno viejo
   (posible robo), se cierran todas las sesiones de ese usuario.
4. `POST /api/v1/auth/logout` revoca el refresh token.

| Acción | Público | Cliente | Vendedor | Administrador |
|---|:---:|:---:|:---:|:---:|
| Ver catálogo y categorías | ✔ | ✔ | ✔ | ✔ |
| Registrarse / iniciar sesión | ✔ | | | |
| Comprar (POST boletas), ver **sus** boletas | | ✔ | | ✔ |
| Publicar su propiedad, ver **sus** publicaciones | | ✔ | | ✔ |
| Ver todos los productos y todas las boletas | | | ✔ | ✔ |
| Crear, editar y eliminar productos, categorías, usuarios | | | | ✔ |

Sin token → **401**. Con token pero sin el rol necesario → **403**.
En Swagger usa el botón **Authorize** y pega el `accessToken`.

## Docker (3 contenedores)

Deja las dos carpetas una al lado de la otra:

```
mi-carpeta/
├── auraarriendos-backend/     (este repositorio, tiene docker-compose.yml)
└── proyectoauraarriendos/     (repositorio del frontend)
```

```bash
cd auraarriendos-backend
cp .env.example .env            # y cambia las claves
docker compose up -d --build
```

| Contenedor | Imagen | URL |
|---|---|---|
| `aura-frontend` | nginx:alpine (React compilado) | http://localhost:3000 |
| `aura-backend` | eclipse-temurin:21-jre-alpine | http://localhost:8080/doc/swagger-ui.html |
| `aura-db` | mysql:8.4 | puerto interno 3306 |

Las claves (base de datos y JWT) **no están en el código**: vienen del archivo `.env`, que no se sube a GitHub.
Para detener: `docker compose down` (agrega `-v` para borrar también los datos).

## Pruebas

```bash
./mvnw test
```

Las pruebas usan H2 en memoria, así que no necesitan MySQL. Ver [TESTING_PLAN.md](TESTING_PLAN.md).

## Endpoints (resumen)

| Recurso | v1 (JSON) | v2 (HATEOAS) |
|---|---|---|
| Productos | `/api/v1/productos` | `/api/v2/productos` |
| Categorías | `/api/v1/categorias` | `/api/v2/categorias` |
| Usuarios | `/api/v1/usuarios` | — |
| Boletas | `/api/v1/boletas` | `/api/v2/boletas` |

El detalle de cada endpoint (entradas, respuestas y códigos) está en Swagger.

## Equipo

Roberto Bustamante · Sebastián Reyes · Renato Navarrete
